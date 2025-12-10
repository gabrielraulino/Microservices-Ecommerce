package com.ms.user.dto;

public record UserCreateDTO(
        String name,
        String email,
        String password,
        String role
) {

}
