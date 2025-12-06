package com.ms.order.dto;

import com.ms.order.enums.PaymentMethod;

import java.util.List;

public record CheckoutEvent(
        Long cart,
        Long user,
        List<CheckoutItem> items,
        PaymentMethod paymentMethod
) {
    /**
     * Checkout item representing a product and its quantity.
     * Contains only essential data to reduce serialized event size.
     */
    public record CheckoutItem(
            Long product,
            Integer quantity
    ) {}
}
