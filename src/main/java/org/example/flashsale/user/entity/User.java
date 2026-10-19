package org.example.flashsale.user.entity;

import jakarta.persistence.*;
import org.example.flashsale.address.entity.Address;
import org.example.flashsale.cart.entity.Cart;
import org.example.flashsale.common.persistence.BaseEntity;
import lombok.*;
import org.example.flashsale.order.entity.Order;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.CUSTOMER;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

//    As of now only One Cart for each user
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Cart cart;

    @OneToMany(
            mappedBy = "user"
    )
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
}
