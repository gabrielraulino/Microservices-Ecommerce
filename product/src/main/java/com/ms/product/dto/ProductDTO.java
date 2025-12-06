package com.ms.product.dto;

import com.ms.product.model.Product;

import java.math.BigDecimal;

public record ProductDTO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stock) {

    public static ProductDTO fromEntity(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock());
    }
}
