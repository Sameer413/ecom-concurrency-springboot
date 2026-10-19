package org.example.flashsale.reseravation.service;

import org.example.flashsale.inventory.service.InventoryService;
import org.example.flashsale.order.entity.Order;
import org.example.flashsale.order.entity.OrderStatus;
import org.example.flashsale.order.repository.OrderRepository;
import org.example.flashsale.product.entity.ProductVariant;
import org.example.flashsale.reseravation.entity.Reservation;
import org.example.flashsale.reseravation.entity.ReservationStatus;
import org.example.flashsale.reseravation.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final InventoryService inventoryService;
    private final Duration reservationTtl;
    private final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final OrderRepository orderRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            InventoryService inventoryService,
            OrderRepository orderRepository,
            @Value("${app.reservation.ttl-minutes:10}") long ttlMinutes) {
        this.reservationRepository = reservationRepository;
        this.inventoryService = inventoryService;
        this.reservationTtl = Duration.ofMinutes(ttlMinutes);
        this.orderRepository = orderRepository;
    }

    /**
     * Reserves stock via InventoryService and creates an ACTIVE Reservation entity.
     */
    @Transactional
    public Reservation createReservation(Order order, ProductVariant variant, int quantity) {
//        Reserve the stock in inventory lock the row so other transaction can not be overwrite or prevent stock overselling
//        create Order
//        create a reservation and push bg job which release the stock if not purchased

        // 1. Delegate stock reservation to InventoryService (which acquires pessimistic lock)
        inventoryService.reserveStock(variant.getId(), quantity);

        // 2. Persist Reservation Record
        Instant expiresAt = Instant.now().plus(reservationTtl);
        Reservation reservation = Reservation.builder()
                .expiresAt(expiresAt)
                .order(order)
                .productVariant(variant)
                .quantity(quantity)
                .status(ReservationStatus.ACTIVE)
                .build();

        return reservationRepository.save(reservation);
    }

    /**
     * Fulfills reservation on successful payment.
     * Decrements both quantityReserved and quantityOnHand via InventoryService.
     */
    @Transactional
    public void fulfillReservation(UUID orderId) {
        List<Reservation> reservations = reservationRepository.findAllByOrderId(orderId);

        if (reservations.isEmpty()) {
            throw new IllegalStateException("No active reservation found for order: " + orderId);
        }
        Instant now = Instant.now();

        for(Reservation reservation : reservations) {
            if(!reservation.getStatus().equals(ReservationStatus.ACTIVE) || reservation.getExpiresAt().isBefore(now)) {
//                continue;
                throw new IllegalStateException("Reservation has expired or is no longer active. Cannot confirm order.");
            }

            inventoryService.deductSoldStock(
                    reservation.getProductVariant().getId(),
                    reservation.getQuantity()
            );
//            inventoryService.releaseStock(reservation.getProductVariant().getId(), reservation.getQuantity());
            reservation.setStatus(ReservationStatus.FULFILLED);
        }
    }

    /**
     * Releases reserved stock back to available pool upon order cancellation.
     */
    @Transactional
    public void releaseReservation(UUID orderId) {
        List<Reservation> reservations = reservationRepository.findAllByOrderId(orderId);

        for(Reservation reservation : reservations) {
            if(reservation.getStatus().equals(ReservationStatus.ACTIVE)) {
                inventoryService.releaseStock(reservation.getProductVariant().getId(), reservation.getQuantity());
                reservation.setStatus(ReservationStatus.RELEASED);
            }
        }
    }


    /**
     * Periodic cleanup job: releases stock for expired active reservations
     * and cancels the associated pending orders.
     */
   @Transactional
   @Scheduled(fixedRate = 60000)
   public void expireReservationsBatch() {
       Instant now = Instant.now();
       List<Reservation> expiredReservations = reservationRepository.findExpiredActiveReservations(ReservationStatus.ACTIVE, now);

       if (expiredReservations.isEmpty()) {
           return;
       }

       log.info("Processing {} expired reservations.", expiredReservations.size());
       Set<Order> ordersToCancel = new HashSet<>();

       for (Reservation reservation : expiredReservations) {
           try {
               inventoryService.releaseStock(reservation.getProductVariant().getId(), reservation.getQuantity());
               reservation.setStatus(ReservationStatus.EXPIRED);

               Order order = reservation.getOrder();
               if (order != null && order.getStatus() == OrderStatus.PENDING) {
                   ordersToCancel.add(order);
               }
           } catch (Exception ex) {
               log.error("Failed to expire reservation id: {}", reservation.getId(), ex);
           }
       }

       for (Order order : ordersToCancel) {
           order.setStatus(OrderStatus.CANCELLED);
           orderRepository.save(order);
           log.info("Cancelled expired order id: {}", order.getId());
       }
   }
}
