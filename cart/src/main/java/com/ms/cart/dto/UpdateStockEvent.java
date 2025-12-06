package com.ms.cart.dto;

import java.util.Map;

/**
 * Event published after order creation to update product stock.
 * This event is consumed by ProductService to decrement stock.
 */
public record UpdateStockEvent(
        Long cartId,
        Long userId,
        Map<Long, Integer> productQuantities
) {}

