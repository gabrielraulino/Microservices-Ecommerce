package com.ms.cart.dto;

import com.ms.cart.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
        Long id,
        Long userId,
        String status,
        LocalDateTime updatedAt,
        LocalDateTime createdAt,
        List<OrderItemDTO> items,
        PaymentMethod paymentMethod,
        Integer totalQuantity,
        BigDecimal totalPrice
) {
    public record OrderItemDTO(
            Long id,
            String name,
            Long productId,
            Integer quantity,
            BigDecimal priceAmount,
            BigDecimal totalPrice
    ) {}
}

