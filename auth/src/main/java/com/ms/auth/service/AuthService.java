package com.ms.auth.service;

import com.ms.auth.client.UserServiceClient;
import com.ms.auth.dto.AuthResponse;
import com.ms.auth.dto.LoginRequest;
import com.ms.auth.dto.RegisterRequest;
import com.ms.auth.dto.UserCreateDTO;
import com.ms.auth.dto.UserDTO;
import com.ms.auth.dto.UserWithRoleDTO;
import com.ms.auth.dto.ValidateCredentialsRequest;
import com.ms.auth.exception.InvalidAdminKeyException;
import com.ms.auth.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserServiceClient userServiceClient;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.key}")
    private String adminKey;

    public AuthResponse register(RegisterRequest request) {
        // Verificar se usuário já existe
        try {
            UserDTO existingUser = userServiceClient.getUserByEmail(request.email());
            if (existingUser != null) {
                throw new InvalidCredentialsException("User already exists with this email");
            }
        } catch (Exception e) {
            // Se não encontrar, continua com o registro
        }

        // Determinar role baseado na adminKey
        String role = "USER";
        
        if (request.adminKey() != null && !request.adminKey().isBlank()) {
            if (!adminKey.equals(request.adminKey())) {
                throw new InvalidAdminKeyException("Invalid admin key");
            }
            role = "ADMIN";
        }

        // Criar novo usuário (senha será codificada no User Service)
        UserCreateDTO userCreateDTO = new UserCreateDTO(
                request.name(),
                request.email(),
                request.password(), // Será codificada no User Service
                role
        );

        UserDTO user = userServiceClient.createUser(userCreateDTO);

        // Gerar token JWT com a role correta
        String token = jwtService.generateToken(user.email(), role, user.id());

        return AuthResponse.of(token, user.id(), user.email(), role);
    }

    public AuthResponse login(LoginRequest request) {
        // Validar credenciais no User Service
        ValidateCredentialsRequest credentialsRequest = new ValidateCredentialsRequest(
                request.email(),
                request.password()
        );

        UserWithRoleDTO user;
        try {
            user = userServiceClient.validateCredentials(credentialsRequest);
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Gerar token JWT
        String token = jwtService.generateToken(user.email(), user.role(), user.id());

        return AuthResponse.of(token, user.id(), user.email(), user.role());
    }

    public AuthResponse validateToken(String token) {
        if (jwtService.isTokenExpired(token)) {
            throw new InvalidCredentialsException("Token expired");
        }

        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);
        Long userId = jwtService.extractUserId(token);

        if (!jwtService.validateToken(token, email)) {
            throw new InvalidCredentialsException("Invalid token");
        }

        return AuthResponse.of(token, userId, email, role);
    }
}

