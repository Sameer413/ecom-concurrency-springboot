package org.example.flashsale.inventory.service;

import org.example.flashsale.common.exception.ResourceNotFoundException;
import org.example.flashsale.inventory.dto.InventoryResponse;
import org.example.flashsale.inventory.entity.Inventory;
import org.example.flashsale.inventory.repository.InventoryRepository;
import org.example.flashsale.product.entity.ProductVariant;
import org.example.flashsale.product.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductVariantRepository productVariantRepository;
    public InventoryService(InventoryRepository inventoryRepository, ProductVariantRepository productVariantRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public void createInventory(UUID productVariantId, int quantity) {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId).orElseThrow(
                () -> new ResourceNotFoundException("Product variant not found with id: " + productVariantId)
        );

        Inventory inventory = Inventory.builder()
                .productVariant(productVariant)
                .quantityOnHand(quantity)
                .build();

        inventoryRepository.save(inventory);

    }

    public InventoryResponse getInventory(UUID productVariantId) {
        Inventory inventory = inventoryRepository.findByProductVariantId(productVariantId).orElseThrow(
                () -> new ResourceNotFoundException("Inventory not found with id: " + productVariantId)
        );

        return InventoryResponse.from(inventory);
    }

    public Map<UUID, InventoryResponse> getInventory(Collection<UUID> productVariantIds) {
        return inventoryRepository.findByProductVariantIdIn(productVariantIds)
                .stream()
                .collect(Collectors.toMap(
                        inv -> inv.getProductVariant().getId(),
                        InventoryResponse::from
                ));
    }

    /**
     * Reserves stock for a variant using a pessimistic write lock.
     * Prevents overselling across concurrent transactions.
     */
    @Transactional
    public Inventory reserveStock(UUID variantId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductVariantIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory Not Found"));

        int availableQuantity = inventory.getAvailableQuantity();

        if(availableQuantity < quantity) {
            throw new ResourceNotFoundException("Insufficient Stock");
        }

        inventory.setQuantityReserved(
                inventory.getQuantityReserved() + quantity
        );

        return inventoryRepository.save(inventory);
    }

    /**
     * Releases reserved stock back to the available pool (cancellation or expiration).
     */
    public void releaseStock(UUID variantId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductVariantIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory Not Found"));

        inventory.setQuantityReserved(Math.max(0, inventory.getQuantityReserved() - quantity));

        inventoryRepository.save(inventory);
    }

    /**
     * Deducts sold stock upon successful payment completion.
     * Decrements both quantityReserved and quantityOnHand.
     */
    public void deductSoldStock(UUID variantId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductVariantIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory Not Found"));

        inventory.setQuantityReserved(Math.max(0, inventory.getQuantityReserved() - quantity));
        inventory.setQuantityOnHand(Math.max(0, inventory.getQuantityOnHand() - quantity));
        inventory.setQuantitySold(Math.max(0, inventory.getQuantitySold() + quantity));

        inventoryRepository.save(inventory);
    }

    /**
     * Reverts sold stock back to quantityOnHand upon cancelling a PAID order.
     */
    @Transactional
    public void revertSoldStock(UUID variantId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductVariantIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory Not Found for variant: " + variantId));
        // Decrement sold stock and return items to on-hand inventory
        inventory.setQuantitySold(Math.max(0, inventory.getQuantitySold() - quantity));
        inventory.setQuantityOnHand(inventory.getQuantityOnHand() + quantity);
        inventoryRepository.save(inventory);
    }


    public int deleteInventories(Collection<UUID> productVariantIds) {
        return inventoryRepository.deleteAllByIds(productVariantIds);
    }
}
