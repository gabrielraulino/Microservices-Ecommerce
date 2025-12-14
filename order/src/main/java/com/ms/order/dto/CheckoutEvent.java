package com.ms.order.dto;

import java.util.List;

/**
 * Event received from CartService when checkout is initiated.
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
