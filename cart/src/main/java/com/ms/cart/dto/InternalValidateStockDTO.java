package com.ms.cart.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record InternalValidateStockDTO(
    Map<Long, Integer> productQuantities
) {
    
}
