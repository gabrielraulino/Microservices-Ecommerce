package com.ms.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Cart item with product information")
public record CartItemDTO(
        @Schema(description = "Cart item ID", example = "1")
        Long id,
        
        @Schema(description = "Product ID", example = "5")
        Long productId,
        
        @Schema(description = "Product name", example = "Smartphone XYZ")
        String productName,
        
        @Schema(description = "Unit price", example = "999.99")
        BigDecimal unitPrice,

        @Schema(description = "Quantity in cart", example = "2")
        int quantity,
        
        @Schema(description = "Subtotal (unitPrice * quantity)", example = "1999.98")
        BigDecimal subtotal
) {
}

