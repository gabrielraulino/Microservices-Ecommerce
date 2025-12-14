package com.ms.order.dto;

/**
 * Event published when an order is processed (status changed to PROCESSING).
 * This triggers the shipping workflow.
 */
public record OrderProcessedEvent(
        Long orderId,
        Long userId
) {}
