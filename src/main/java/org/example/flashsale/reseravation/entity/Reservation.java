package org.example.flashsale.reseravation.entity;

import jakarta.persistence.*;
import org.example.flashsale.cart.entity.Cart;
import org.example.flashsale.common.persistence.BaseEntity;
import lombok.*;
import org.example.flashsale.order.entity.Order;
import org.example.flashsale.product.entity.ProductVariant;

import java.time.Instant;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "variant_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_reservation_variant")
    )
    private ProductVariant productVariant;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(
//            name = "cart_id",
//            foreignKey = @ForeignKey(name = "fk_reservation_cart")
//    )
//    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            foreignKey = @ForeignKey(name = "fk_reservation_order")
    )
    private Order order;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.ACTIVE;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
