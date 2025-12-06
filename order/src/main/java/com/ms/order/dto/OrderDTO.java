package com.ms.order.dto;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ms.order.enums.PaymentMethod;
import com.ms.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Order with items and totals")
public record OrderDTO(
        @Schema(description = "Order ID", example = "1")
        Long id,

        @Schema(description = "User ID who placed the order", example = "1")
        Long userId,

        @Schema(description = "Order status", example = "PENDING")
        OrderStatus status,

        @Schema(description = "Last update date", example = "2021-01-01T12:00:00")
        LocalDateTime updatedAt,

        @Schema(description = "Creation date", example = "2021-01-01T12:00:00")
        LocalDateTime createdAt,

        @Schema(description = "List of items in the order")
        List<OrderItemDTO> items,

        @Schema(description = "Payment method used", example = "CREDIT_CARD")
        PaymentMethod paymentMethod,

        @Schema(description = "Total quantity of all items", example = "5")
        Integer totalQuantity,

        @Schema(description = "Total price of all items", example = "2999.95")
        BigDecimal totalPrice
) {

}
