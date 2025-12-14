package com.ms.order.dto;

public record OrderShippedEvent(long orderId, Long userId) {
}
