package com.ms.order.dto;

import com.ms.order.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO for creating a new order")
public record CreateOrderDTO(
        @Schema(description = "User ID who is placing the order", example = "1")
        Long userId,

        @Schema(description = "List of items in the order")
        List<CreateOrderItemDTO> items,

        @Schema(description = "Payment method", example = "CREDIT_CARD")
        PaymentMethod paymentMethod
) {
    @Schema(description = "Order item data for creation")
    public record CreateOrderItemDTO(
            @Schema(description = "Product ID", example = "1")
            Long productId,

            @Schema(description = "Quantity", example = "2")
            Integer quantity
    ) {}
}

