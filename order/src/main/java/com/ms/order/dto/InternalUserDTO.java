package com.ms.order.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InternalUserDTO(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt
) {
}