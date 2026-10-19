package org.example.flashsale.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.flashsale.cart.entity.CartItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private UUID id;
    private UUID variantId;
    private String variantName;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;   // quantity * unitPrice
    private Instant createdAt;
    private Instant updatedAt;

    public static CartItemResponse from(CartItem item) {
        CartItemResponse res = new CartItemResponse();
        res.id = item.getId();
        res.variantId = item.getProductVariant().getId();
        res.variantName = item.getProductVariant().getSku(); // adjust to your field
        res.quantity = item.getQuantity();
        res.unitPrice = item.getUnitPrice();
        res.lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return res;
    }
}
