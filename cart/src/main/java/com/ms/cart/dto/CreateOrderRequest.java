package com.ms.cart.dto;

import java.util.List;

public record CreateOrderRequest(
        Long userId,
        List<CreateOrderItemRequest> items,
        String paymentMethod
) {
    public record CreateOrderItemRequest(
            Long productId,
            Integer quantity
    ) {}
}

