package com.ms.order.enums;

public enum OrderStatus {
    PENDING(),
    PROCESSING(),
    SHIPPED(),
    DELIVERED(),
    CANCELLED();

    OrderStatus() {
    }
}