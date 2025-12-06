package com.ms.order.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event published when an order is cancelled.
 * This event is consumed by ProductService to restore stock.
 */
public record OrderCancelledEvent(
        Long orderId,
        Long userId,
        List<CancelledItem> items,
        LocalDateTime cancelledDate
) {
    public record CancelledItem(
            Long productId,
            Integer quantity
    ) {}
}

