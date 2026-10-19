package org.example.flashsale.cart.service;

import org.example.flashsale.cart.dto.AddCartItemRequest;
import org.example.flashsale.cart.dto.CartItemResponse;
import org.example.flashsale.cart.dto.CartResponse;
import org.example.flashsale.cart.entity.Cart;
import org.example.flashsale.cart.entity.CartItem;
import org.example.flashsale.cart.repository.CartItemRepository;
import org.example.flashsale.cart.repository.CartRepository;
import org.example.flashsale.common.exception.ResourceNotFoundException;
import org.example.flashsale.product.entity.ProductVariant;
import org.example.flashsale.product.repository.ProductVariantRepository;
import org.example.flashsale.user.entity.User;
import org.example.flashsale.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ProductVariantRepository productVariantRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productVariantRepository = productVariantRepository;
    }

    private Cart createCart(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + userId)
        );

        Cart cart = Cart.builder()
                .user(user)
                .build();

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart getCart(UUID userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = createCart(userId);
            cartRepository.save(newCart);
            return newCart;
        });
    }

    public CartResponse addItemToCart(AddCartItemRequest request, UUID userId) {
        Cart cart = getCart(userId);
        ProductVariant variant = productVariantRepository.findById(request.getVariantId()).orElseThrow(
                () -> new ResourceNotFoundException("Product variant not found with id: " + request.getVariantId())
        );

        CartItem item = CartItem.builder()
                .cart(cart)
                .productVariant(variant)
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .build();

        cartItemRepository.save(item);

        return CartResponse.from(cart);
    }

    public CartItemResponse removeItemFromCart(UUID cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(
                () -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId)
        );

        cartItemRepository.delete(cartItem);

        return CartItemResponse.from(cartItem);
    }

    public CartItemResponse updateCartItemQuantity(UUID cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(
                () -> new  ResourceNotFoundException("Cart item not found with id: " + cartItemId)
        );

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return CartItemResponse.from(cartItem);
    }
}
