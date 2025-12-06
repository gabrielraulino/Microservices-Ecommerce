package com.ms.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Shopping cart with calculated totals")
public record CartDTO(
        @Schema(description = "Cart ID", example = "1")
        Long id,
        
        @Schema(description = "User ID who owns the cart", example = "1")
        Long userId,
        
        @Schema(description = "List of items in the cart")
        List<CartItemDTO> items,
        
        @Schema(description = "Total quantity of all items", example = "5")
        Integer totalQuantity,
        
        @Schema(description = "Total price of all items", example = "2999.95")
        BigDecimal totalPrice,

        @Schema(description = "Cart creation date")
        LocalDateTime createdAt,
        
        @Schema(description = "Last update date")
        LocalDateTime updatedAt
) {
}

