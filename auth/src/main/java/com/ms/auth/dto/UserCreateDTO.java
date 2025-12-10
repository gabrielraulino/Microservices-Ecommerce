package com.ms.auth.dto;

public record UserCreateDTO(
        String name,
        String email,
        String password,
        String role
) {}

