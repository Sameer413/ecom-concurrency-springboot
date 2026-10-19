package org.example.flashsale.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.flashsale.cart.entity.Cart;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private UUID id;
    // OneToOne: each cart belongs to exactly one user
    private UUID userId;
    private String userEmail;       // handy for display without extra call
    private List<CartItemResponse> items;
    private int totalItems;         // sum of quantities
    private BigDecimal cartTotal;   // sum of all lineTotals
    private Instant createdAt;
    private Instant updatedAt;

    public static CartResponse from(Cart cart) {
        CartResponse res = new CartResponse();
        res.id = cart.getId();
        res.userId = cart.getUser().getId();
        res.items = cart.getItems().stream()
                .map(CartItemResponse::from)
                .toList();
        res.cartTotal = res.items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return res;
    }
}
