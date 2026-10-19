package org.example.flashsale.reseravation.repository;

import org.example.flashsale.reseravation.entity.Reservation;
import org.example.flashsale.reseravation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findAllByOrderId(UUID orderId);

    Optional<Reservation> findByOrderIdAndStatus(UUID orderId, ReservationStatus status);

    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.productVariant
        JOIN FETCH r.order
        WHERE r.status = :status
          AND r.expiresAt <= :cutoff
    """)
    List<Reservation> findExpiredActiveReservations(
            @Param("status") ReservationStatus status,
            @Param("cutoff") Instant cutoff
    );
}
