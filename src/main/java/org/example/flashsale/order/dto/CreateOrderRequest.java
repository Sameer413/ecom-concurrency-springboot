package org.example.flashsale.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;


@Getter
public class CreateOrderRequest {

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}
