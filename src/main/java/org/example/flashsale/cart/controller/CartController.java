package org.example.flashsale.cart.controller;

import org.example.flashsale.cart.dto.AddCartItemRequest;
import org.example.flashsale.cart.dto.CartItemResponse;
import org.example.flashsale.cart.dto.CartResponse;
import org.example.flashsale.cart.dto.UpdateCartItemRequest;
import org.example.flashsale.cart.service.CartService;
import org.example.flashsale.common.persistence.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(@RequestBody AddCartItemRequest request) {
        UUID id = UUID.fromString("e75b21b2-a06b-44b5-8e1f-f3006377be8f");;
        CartResponse response = cartService.addItemToCart(request, id);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(true, "Item added to the cart successfully", response)
        );
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> removeItemFromCart(@PathVariable UUID cartItemId) {
        CartItemResponse response = cartService.removeItemFromCart(cartItemId);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(true, "Item removed from the cart successfully", response)
        );
    }

    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateItemInCart(@PathVariable UUID cartItemId, @RequestBody UpdateCartItemRequest request) {
        CartItemResponse response = cartService.updateCartItemQuantity(cartItemId, request.getQuantity());

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(true, "Item updated to the cart successfully", response)
        );
    }
}