package org.example.flashsale.cart.entity;

import jakarta.persistence.*;
import org.example.flashsale.common.persistence.BaseEntity;
import lombok.*;
import org.example.flashsale.product.entity.ProductVariant;

import java.math.BigDecimal;

@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cart_variant",
                        columnNames = {"cart_id", "variant_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cart_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_item_cart")
    )
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "variant_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_item_variant")
    )
    private ProductVariant productVariant;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;
}