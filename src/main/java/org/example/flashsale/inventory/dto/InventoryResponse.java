package org.example.flashsale.inventory.dto;


import org.example.flashsale.inventory.entity.Inventory;
import org.example.flashsale.product.dto.ProductVariantResponse;

import java.util.UUID;


public record InventoryResponse (
    UUID id,
    Integer quantityOnHand,
    Integer quantityReserved,
    Integer availableQuantity
) {
    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getQuantityOnHand(),
                inventory.getQuantityReserved(),
                inventory.getAvailableQuantity()
        );
    }
}