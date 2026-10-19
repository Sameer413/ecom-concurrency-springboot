package org.example.flashsale.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.flashsale.address.entity.Address;
import org.example.flashsale.order.entity.Order;
import org.example.flashsale.order.entity.OrderItem;
import org.example.flashsale.order.entity.OrderStatus;
import org.example.flashsale.order.entity.ShippingAddress;
import org.example.flashsale.product.entity.ProductVariant;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private UUID userId;
    private UUID cartId;            // the OneToOne cart this was placed from
    private String orderNumber;
    private OrderStatus status;
    private AddressSnapshot shippingAddress;
    private AddressSnapshot billingAddress;
    private List<OrderItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal taxTotal;
    private BigDecimal shippingTotal;
    private BigDecimal grandTotal;
    private Instant placedAt;
    private Instant createdAt;
    private Instant updatedAt;


    public static OrderResponse mapToOrderResponse(Order order) {
        return mapToOrderResponse(order, null);
    }


    public static OrderResponse mapToOrderResponse(
            Order order,
            Instant reservationExpiresAt
    ) {

        List<OrderItemResponse> itemResponses =
                order.getItems()
                        .stream()
                        .map(OrderResponse::mapToOrderItemResponse)
                        .toList();

        return OrderResponse.builder()
                .id(order.getId())

                .userId(
                        order.getUser() != null
                                ? order.getUser().getId()
                                : null
                )

//                .cartId(
//                        order.getCart() != null
//                                ? order.getCart().getId()
//                                : null
//                )

                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())

                .shippingAddress(
                        order.getShippingAddress() != null
                                ? mapToAddressSnapshot(order.getShippingAddress())
                                : null
                )

//                .billingAddress(
//                        order.getBillingAddress() != null
//                                ? mapToAddressSnapshot(order.getBillingAddress())
//                                : null
//                )

                .items(itemResponses)

                .subtotal(order.getSubtotal())
                .discountTotal(order.getDiscountTotal())
                .taxTotal(order.getTaxTotal())
                .shippingTotal(order.getShippingTotal())
                .grandTotal(order.getGrandTotal())

                .placedAt(order.getPlacedAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())

                .build();
    }

    private static AddressSnapshot mapToAddressSnapshot(ShippingAddress address) {
        if (address == null) {
            return null;
        }
        return AddressSnapshot.builder()
                .label(address.getLabel())
                .line1(address.getLine1())
                .line2(address.getLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .build();
    }

    private static OrderItemResponse mapToOrderItemResponse(
            OrderItem item
    ) {

        ProductVariant variant = item.getProductVariant();

        String productName = null;

        if (variant != null && variant.getProduct() != null) {
            productName = variant.getProduct().getName();
        }

        return OrderItemResponse.builder()
                .id(item.getId())

                .variantId(
                        variant != null
                                ? variant.getId()
                                : null
                )

                .variantName(
                        variant != null
                                ? variant.getSku()
                                : null
                )

                .productName(productName)

                .quantity(item.getQuantity())

                .unitPrice(item.getUnitPrice())

                .lineTotal(item.getLineTotal())

                .build();
    }
}