package com.ms.order.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Order item with product information")
public record OrderItemDTO(
    @Schema(description = "Order item ID", example = "1")
    Long id,
    @Schema(description = "Product name", example = "Smartphone XYZ")
    String name,
    @Schema(description = "Product ID", example = "5")
    Long productId,
    @Schema(description = "Quantity", example = "2")
    Integer quantity,
    @Schema(description = "Unit price", example = "999.99")
    BigDecimal priceAmount,
    @Schema(description = "Total price", example = "1999.98")
    BigDecimal totalPrice
) {}