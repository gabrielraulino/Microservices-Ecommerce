package com.ms.product.dto;

import java.util.Map;

/**
 * Event received to update product stock after order creation.
 */
public record UpdateStockEvent(
        Long cartId,
        Long userId,
        Map<Long, Integer> productQuantities
) {}

