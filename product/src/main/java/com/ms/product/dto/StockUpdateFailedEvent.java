package com.ms.product.dto;

import java.util.Map;

/**
 * Event published when stock update fails.
 * This event is consumed by OrderService to cancel the order automatically.
 */
public record StockUpdateFailedEvent(
        Long orderId,
        Long userId,
        Map<Long, Integer> productQuantities,
        String errorMessage
) {}

