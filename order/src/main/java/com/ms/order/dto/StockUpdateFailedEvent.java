package com.ms.order.dto;

import java.util.Map;

/**
 * Event received from ProductService when stock update fails.
 * This triggers automatic order cancellation.
 */
public record StockUpdateFailedEvent(
        Long orderId,
        Long userId,
        Map<Long, Integer> productQuantities,
        String errorMessage
) {}

