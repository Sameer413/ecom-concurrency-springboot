package org.example.flashsale.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.flashsale.order.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {
    private UUID id;
    private String orderNumber;
    private OrderStatus status;
    private int totalItems;
    private BigDecimal grandTotal;
    private Instant placedAt;
}