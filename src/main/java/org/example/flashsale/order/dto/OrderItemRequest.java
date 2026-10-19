package org.example.flashsale.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderItemRequest {

    @NotNull(message = "Product Variant Id is Required")
    private UUID productVariantId;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

//    @NotNull(message = "Unit Price is required")
//    private BigDecimal unitPrice;
//
//    @NotNull(message = "Line Total is required")
//    private BigDecimal lineTotal;

}
