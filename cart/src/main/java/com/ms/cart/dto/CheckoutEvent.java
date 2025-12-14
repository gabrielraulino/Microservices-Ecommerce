package com.ms.cart.dto;

import java.util.List;

/**
 * Event published when checkout is initiated.
 * This event is consumed by OrderService to create an order.
 */
public record CheckoutEvent(
        Long cartId,
        Long userId,
        String paymentMethod,
        List<CheckoutItem> items
) {
    public record CheckoutItem(
            Long productId,
            Integer quantity
    ) {}
}
