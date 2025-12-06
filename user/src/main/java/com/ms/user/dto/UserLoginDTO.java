package com.ms.user.dto;


import com.ms.user.model.Role;
import com.ms.user.model.User;

public record UserLoginDTO(
    String email,
    String password,
    Role role
) {
    public static UserLoginDTO fromEntity(User user) {
        return new UserLoginDTO(
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }
}
