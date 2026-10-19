package org.example.flashsale.inventory.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.flashsale.common.persistence.BaseEntity;
import org.example.flashsale.product.entity.Product;
import org.example.flashsale.product.entity.ProductStatus;
import org.example.flashsale.product.entity.ProductVariant;


@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "variant_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_inventory_product_variant")
    )
    private ProductVariant productVariant;

    @Column(name = "quantity_on_hand", nullable = false)
    @Builder.Default
    private Integer quantityOnHand = 0;

    @Column(name = "quantity_reserved", nullable = false)
    @Builder.Default
    private Integer quantityReserved = 0;

    @Column(name = "quantity_sold")
    @Builder.Default
    private Integer quantitySold = 0;

    @Transient
    public Integer getAvailableQuantity() {
        return quantityOnHand - quantityReserved;
    }
}
