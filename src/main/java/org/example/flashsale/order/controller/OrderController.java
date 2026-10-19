package org.example.flashsale.order.controller;

import jakarta.validation.Valid;
import org.example.flashsale.auth.entity.UserPrincipal;
import org.example.flashsale.common.persistence.ApiResponse;
import org.example.flashsale.order.dto.CreateOrderRequest;
import org.example.flashsale.order.dto.OrderAddressRequest;
import org.example.flashsale.order.dto.OrderResponse;
import org.example.flashsale.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/initiate-order")
    public ResponseEntity<ApiResponse<OrderResponse>> initiateOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        OrderResponse orderResponse = orderService.createOrder(request, userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(true, "Order created successfully", orderResponse)
        );
    }

    @PutMapping("/shipping-address/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> setShippingAddress(
            @Valid @RequestBody OrderAddressRequest request,
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        OrderResponse response = orderService.saveOrderAddress(request, orderId, userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(true, "Order updated successfully", response)
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable UUID orderId) {
        OrderResponse response = orderService.getOrderDetailByOrderId(orderId);

        return ResponseEntity.status(HttpStatus.OK).body(
                new  ApiResponse<>(true, "Order retrieved successfully", response)
        );
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        OrderResponse response = orderService.confirmOrder(orderId, userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(true, "Order confirmed successfully", response)
        );
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrderHandler(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        OrderResponse response = orderService.cancelOrder(orderId, userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.OK).body(
                new  ApiResponse<>(true, "Order cancelled successfully", response)
        );
    }
}
