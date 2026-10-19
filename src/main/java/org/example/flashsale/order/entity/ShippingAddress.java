package org.example.flashsale.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.flashsale.common.persistence.BaseEntity;
import org.example.flashsale.user.entity.User;

@Entity
@Table(name = "shipping_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress extends BaseEntity {

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_address_order")
    )
    Order order;

    private String fullName;

    private String phone;

    @Column(length = 50)
    private String label;

    @Column(nullable = false, length = 255)
    private String line1;

    @Column(length = 255)
    private String line2;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(nullable = false, length = 2)
    private String country;
}