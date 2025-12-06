package com.ms.product.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event received when an order is cancelled to restore stock.
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

