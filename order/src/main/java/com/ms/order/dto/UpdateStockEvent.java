package com.ms.order.dto;

import java.util.Map;

/**
 * Event published to ProductService to update stock after order creation.
 */
public record UpdateStockEvent(
        Long cartId,
        Long userId,
        Long orderId,
        Map<Long, Integer> productQuantities
) {}

