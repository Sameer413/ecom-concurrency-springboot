package org.example.flashsale.product.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.flashsale.common.persistence.BaseEntity;
import org.example.flashsale.user.entity.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 160, unique = true)
    private String slug;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(
            nullable = false,
            name = "created_by",
            foreignKey = @ForeignKey(name = "fk_product_user")
    )
    private User createdBy;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();
}
