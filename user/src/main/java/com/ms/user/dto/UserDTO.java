package com.ms.user.dto;

import com.ms.user.model.User;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserDTO(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt
) {
    public static UserDTO fromEntity(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}