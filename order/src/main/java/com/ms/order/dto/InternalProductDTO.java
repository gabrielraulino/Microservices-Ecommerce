package com.ms.order.dto;

import java.math.BigDecimal;

public record InternalProductDTO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stock) {

}
