package com.ms.order.dto;

/**
 * Event published when an order is created.
 * This triggers the order processing workflow.
 */
public record OrderCreatedEvent(
        Long orderId,
        Long userId
) {}
