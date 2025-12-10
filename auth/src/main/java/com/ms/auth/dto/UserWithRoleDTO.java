package com.ms.auth.dto;

public record UserWithRoleDTO(
        Long id,
        String name,
        String email,
        String role
) {}

