package com.ms.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductQuantity(
        @Schema(description = "Product ID", example = "1")
        Long productId,
        @Schema(description = "Requested quantity", example = "5")
        Integer quantity
) {
}

