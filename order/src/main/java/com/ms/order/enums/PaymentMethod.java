package com.ms.order.enums;

public enum PaymentMethod {
    CREDIT_CARD ("CREDIT_CARD"),
    PIX("PIX"),
    CASH("CASH");

    private final String method;

    PaymentMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
