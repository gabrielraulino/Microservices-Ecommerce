package com.ms.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record CreateProductDTO(
        @Schema(description = "Product name", example = "Smartphone XYZ", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(description = "Product description", example = "Latest generation smartphone with advanced features", requiredMode = Schema.RequiredMode.REQUIRED)
        String description,
        @Schema(description = "Product price in BRL", example = "999.99", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal price,
        @Schema(description = "Available stock quantity", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
        int stock
) {
}
