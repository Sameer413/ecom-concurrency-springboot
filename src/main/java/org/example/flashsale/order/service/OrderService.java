package org.example.flashsale.order.service;


import org.example.flashsale.address.entity.Address;
import org.example.flashsale.address.service.AddressService;
import org.example.flashsale.common.exception.ResourceNotFoundException;
import org.example.flashsale.common.exception.UnauthorizedException;
import org.example.flashsale.inventory.service.InventoryService;
import org.example.flashsale.order.dto.*;
import org.example.flashsale.order.entity.Order;
import org.example.flashsale.order.entity.OrderItem;
import org.example.flashsale.order.entity.OrderStatus;
import org.example.flashsale.order.entity.ShippingAddress;
import org.example.flashsale.order.repository.OrderRepository;
import org.example.flashsale.order.repository.ShippingAddressRepository;
import org.example.flashsale.product.entity.ProductVariant;
import org.example.flashsale.product.repository.ProductVariantRepository;
import org.example.flashsale.reseravation.entity.Reservation;
import org.example.flashsale.reseravation.service.ReservationService;
import org.example.flashsale.user.entity.User;
import org.example.flashsale.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.flashsale.order.dto.OrderResponse.mapToOrderResponse;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ReservationService reservationService;
    private final UserRepository userRepository;
    private final AddressService addressService;
    private final ShippingAddressRepository shippingAddressRepository;
    private final InventoryService inventoryService;

    public OrderService(
            OrderRepository orderRepository,
            ProductVariantRepository productVariantRepository,
            ReservationService reservationService,
            UserRepository userRepository,
            AddressService addressService,
            ShippingAddressRepository shippingAddressRepository,
            InventoryService inventoryService
    ) {
        this.orderRepository = orderRepository;
        this.productVariantRepository = productVariantRepository;
        this.reservationService = reservationService;
        this.userRepository = userRepository;
        this.addressService = addressService;
        this.shippingAddressRepository = shippingAddressRepository;
        this.inventoryService = inventoryService;
    }

    public OrderResponse getOrderDetailByOrderId(UUID orderId) {
        Order order = getOrderById(orderId);


        return mapToOrderResponse(order);
    }

    private Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, UUID userId) {
        // 0. Find the user or else will throw an error
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + userId)
        );

        Map<UUID, Integer> quantityByVariantId = new LinkedHashMap<>();
        for (OrderItemRequest item : request.getItems()) {
            quantityByVariantId.merge(item.getProductVariantId(), item.getQuantity(), Integer::sum);
        }

        List<UUID> sortedVariantIds = new ArrayList<>(quantityByVariantId.keySet());
        Collections.sort(sortedVariantIds);

        Map<UUID, ProductVariant> variantMap = productVariantRepository.findAllById(sortedVariantIds)
                .stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        for (UUID variantId : sortedVariantIds) {
            if (!variantMap.containsKey(variantId)) {
                throw new ResourceNotFoundException("Product variant not found with id " + variantId);
            }
        }

        String orderNumber = "ORD-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .status(OrderStatus.PENDING)
                .placedAt(Instant.now())
                .subtotal(BigDecimal.ZERO)
                .grandTotal(BigDecimal.ZERO)
                .build();

        BigDecimal subTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();


        for (UUID variantId : sortedVariantIds) {
            ProductVariant variant = variantMap.get(variantId);
            int qty = quantityByVariantId.get(variantId);
            BigDecimal unitPrice = variant.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            orderItems.add(OrderItem.builder()
                    .order(order)
                    .productVariant(variant)
                    .quantity(qty)
                    .unitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .build());

            subTotal = subTotal.add(lineTotal);
        }

        order.setItems(orderItems);
        order.setSubtotal(subTotal);
        order.setGrandTotal(subTotal);

        Order savedOrder = orderRepository.save(order);

        Instant reservationExpiresAt = null;
        for(UUID variantId : sortedVariantIds) {
            ProductVariant variant = variantMap.get(variantId);
            int qty = quantityByVariantId.get(variantId);
            Reservation reservation = reservationService.createReservation(savedOrder, variant, qty);
            reservationExpiresAt = reservation.getExpiresAt();
        }

        return mapToOrderResponse(order, reservationExpiresAt);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId, UUID userId) {
        Order order = getOrderById(orderId);

        if(!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not allowed to save this order.");
        }

        // 1. Guard against non-cancellable terminal statuses
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled.");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Delivered orders cannot be cancelled. Please initiate a return request.");
        }
        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel an order that has already shipped.");
        }
        if (order.getStatus() == OrderStatus.REFUNDED) {
            throw new IllegalStateException("Order has already been refunded.");
        }


        // 2. Handle inventory reversal based on current state
        if (order.getStatus() == OrderStatus.PENDING) {
            // Order was only placed -> stock is only RESERVED, not yet SOLD
            reservationService.releaseReservation(order.getId());
        } else if (order.getStatus() == OrderStatus.PAID) {
            // Order was PAID -> stock was deducted and marked as SOLD
            // Revert sold stock back to on-hand inventory for each line item
            for (OrderItem item : order.getItems()) {
                inventoryService.revertSoldStock(
                        item.getProductVariant().getId(),
                        item.getQuantity()
                );
            }
            // TODO: Trigger payment refund flow here (e.g., paymentService.refund(order))
        }

        // 3. Update order status and save
        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        return mapToOrderResponse(updatedOrder);
    }


    public OrderResponse saveOrderAddress(OrderAddressRequest request, UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new ResourceNotFoundException("Order not found with id: " + orderId)
        );

        if(!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not allowed to save this order.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot change shipping address for order in status: " + order.getStatus());
        }

        Address address = addressService.findById(request.getAddressId());

        // Check if a shipping address already exists for this order to update instead of creating duplicate
        ShippingAddress shippingAddress = shippingAddressRepository.findByOrderId(orderId)
                .orElseGet(() -> ShippingAddress.builder().order(order).build());

        shippingAddress.setFullName(request.getFullName());
        shippingAddress.setPhone(request.getPhone());
        shippingAddress.setLabel(address.getLabel());
        shippingAddress.setLine1(address.getLine1());
        shippingAddress.setLine2(address.getLine2());
        shippingAddress.setCity(address.getCity());
        shippingAddress.setState(address.getState());
        shippingAddress.setPostalCode(address.getPostalCode());
        shippingAddress.setCountry(address.getCountry());

        ShippingAddress savedShippingAddress = shippingAddressRepository.save(shippingAddress);
        order.setShippingAddress(savedShippingAddress);

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse confirmOrder(UUID orderId, UUID userId) {
        Order order = getOrderById(orderId);

        if(!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not allowed to save this order.");
        }

        if(order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot confirm order in status: " + order.getStatus());
        }

        if (order.getShippingAddress() == null && shippingAddressRepository.findByOrderId(orderId).isEmpty()) {
            throw new IllegalStateException("Cannot confirm order without a shipping address");
        }

        reservationService.fulfillReservation(order.getId());

        order.setStatus(OrderStatus.PAID);
        Order updatedOrder = orderRepository.save(order);

        return mapToOrderResponse(updatedOrder);
    }

}
