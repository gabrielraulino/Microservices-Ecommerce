package com.ms.auth.dto;

public record ValidateCredentialsRequest(
        String email,
        String password
) {}

