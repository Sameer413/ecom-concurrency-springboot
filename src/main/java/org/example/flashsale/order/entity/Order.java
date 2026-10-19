package org.example.flashsale.order.entity;

import jakarta.persistence.*;
import org.example.flashsale.address.entity.Address;
import org.example.flashsale.cart.entity.Cart;
import org.example.flashsale.common.persistence.BaseEntity;
import lombok.*;
import org.example.flashsale.user.entity.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_order_user")
    )
    private User user;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(
//            name = "cart_id",
//            foreignKey = @ForeignKey(name = "fk_order_cart")
//    )
//    private Cart cart;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "shipping_address_id")
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ShippingAddress shippingAddress;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "billing_address_id")
//    private Address billingAddress;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Column(name = "tax_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxTotal = BigDecimal.ZERO;

    @Column(name = "shipping_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal shippingTotal = BigDecimal.ZERO;

    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @Column(name = "placed_at")
    private Instant placedAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
