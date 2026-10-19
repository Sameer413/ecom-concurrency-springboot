package org.example.flashsale.product.repository;

import org.example.flashsale.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

//@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    List<ProductVariant> findByProductId(UUID productId);

    @Modifying
    @Query("DELETE FROM ProductVariant v WHERE v.id IN :variantIds")
    int deleteAllByIds(@Param("variantIds") Collection<UUID> variantIds);
}
