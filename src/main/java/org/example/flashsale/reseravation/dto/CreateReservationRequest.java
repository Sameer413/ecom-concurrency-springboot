package org.example.flashsale.reseravation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateReservationRequest {
    @NotNull(message = "variantId is required")
    private UUID variantId;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;
}
