package com.ms.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response with JWT token")
public record AuthResponse(
        @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,
        
        @Schema(description = "Token type", example = "Bearer")
        String type,
        
        @Schema(description = "User ID", example = "1")
        Long userId,
        
        @Schema(description = "User email", example = "user@example.com")
        String email,
        
        @Schema(description = "User role", example = "USER")
        String role
) {
    public static AuthResponse of(String token, Long userId, String email, String role) {
        return new AuthResponse(token, "Bearer", userId, email, role);
    }
}

