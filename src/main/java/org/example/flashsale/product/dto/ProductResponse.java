package org.example.flashsale.product.dto;

import org.example.flashsale.product.entity.Product;
import org.example.flashsale.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        String slug,
        String brand,
        String category,
        ProductStatus status,
        BigDecimal basePrice,
        List<ProductVariantResponse> variants
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSlug(),
                product.getBrand(),
                product.getCategory(),
                product.getStatus(),
                product.getBasePrice(),
                product.getVariants()
                        .stream()
                        .map(ProductVariantResponse::from)
                        .toList()
        );
    }
}
