package org.example.flashsale.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.flashsale.common.persistence.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(
        name = "product_variants"
//        indexes = {
////                @Index(name = "idx_product_variant_product_id", columnList = "product_id")
//        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_variant_product")
    )
    private Product product;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> attributes;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
