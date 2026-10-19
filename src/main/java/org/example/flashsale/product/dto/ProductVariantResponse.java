package org.example.flashsale.product.dto;

import org.example.flashsale.inventory.dto.InventoryResponse;
import org.example.flashsale.inventory.entity.Inventory;
import org.example.flashsale.product.entity.ProductVariant;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record ProductVariantResponse(
        UUID id,
        String sku,
        Map<String, String> attributes,
        BigDecimal price,
        Integer weightGrams,
        Boolean isActive,
        InventoryResponse inventory

) {
    public static ProductVariantResponse from(ProductVariant variant) {
        return from(variant, null);
    }

    public static ProductVariantResponse from(ProductVariant variant, InventoryResponse inventory) {
        return new ProductVariantResponse(
                variant.getId(),
                variant.getSku(),
                variant.getAttributes(),
                variant.getPrice(),
                variant.getWeightGrams(),
                variant.getIsActive(),
                inventory
        );
    }

}