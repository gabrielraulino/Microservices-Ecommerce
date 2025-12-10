package com.ms.user.dto;

import com.ms.user.model.User;

public record UserWithRoleDTO(
        Long id,
        String name,
        String email,
        String role
) {
    public static UserWithRoleDTO fromEntity(User user) {
        return new UserWithRoleDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}

