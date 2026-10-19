package org.example.flashsale.payment.entity;

import jakarta.persistence.*;

import org.example.flashsale.common.persistence.BaseEntity;
import lombok.*;
import org.example.flashsale.order.entity.Order;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_order")
    )
    private Order order;

    @Column(length = 50)
    private String provider;

    @Column(name = "provider_ref", length = 255)
    private String providerRef;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;
}
