package org.example.flashsale.cart.entity;

import jakarta.persistence.*;
import org.example.flashsale.common.persistence.BaseEntity;
import lombok.*;
import org.example.flashsale.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

//    As of now only One Cart for each user
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_cart_user")
    )
    private User user;

//    @Column(name = "session_id", length = 100)
//    private String sessionId;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 20)
//    @Builder.Default
//    private CartStatus status = CartStatus.OPEN;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
}
