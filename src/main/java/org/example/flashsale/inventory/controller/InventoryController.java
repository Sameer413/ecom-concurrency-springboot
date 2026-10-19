package org.example.flashsale.inventory.controller;

import org.example.flashsale.common.persistence.ApiResponse;
import org.example.flashsale.inventory.dto.CreateInventoryRequest;
import org.example.flashsale.inventory.dto.InventoryResponse;
import org.example.flashsale.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("{productVariantId}")
    public ResponseEntity<ApiResponse<?>> createInventoryHandler(
            @RequestBody CreateInventoryRequest request,
            @PathVariable String productVariantId
    ) {
        inventoryService.createInventory(UUID.fromString(productVariantId), request.getQuantity());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(true, "Inventory created successfully", "")
        );
    }

    @GetMapping("/get/{productVariantId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryHandler(@PathVariable String productVariantId) {
        InventoryResponse response = inventoryService.getInventory(UUID.fromString(productVariantId));
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(true, "Inventory get successfully", response)
        );
    }
}
