package com.ms.user.controller;

import com.ms.user.dto.UserCreateDTO;
import com.ms.user.dto.UserDTO;
import com.ms.user.dto.UserWithRoleDTO;
import com.ms.user.dto.ValidateCredentialsDTO;
import com.ms.user.service.UserService;
import com.ms.user.auth.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/users")
@RestController
@AllArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {
    private final UserService service;
    private final CurrentUserService currentUserService;

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public UserDTO getUserById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping()
    @Operation(summary = "Get all users with pagination")
    public List<UserDTO> getAllUsers(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.findAll(pageable);
    }

    @PostMapping()
    @Operation(summary = "")
    public UserDTO createUser(@RequestBody UserCreateDTO user) {
        return service.saveUser(user);
    }

    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable Long id, @RequestBody UserCreateDTO user) {
        return service.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user")
    public UserDTO getCurrentUser() {
        Long userId = currentUserService.getCurrentUserId();
        return service.findById(userId);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user")
    public UserDTO updateCurrentUser(@RequestBody UserCreateDTO user) {
        Long userId = currentUserService.getCurrentUserId();
        return service.updateUser(userId, user);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email")
    public UserDTO getUserByEmail(@PathVariable String email) {
        return service.findByEmail(email);
    }

    @PostMapping("/validate-credentials")
    @Operation(summary = "Validate user credentials")
    public UserWithRoleDTO validateCredentials(@RequestBody ValidateCredentialsDTO credentials) {
        if (!service.validateCredentials(credentials.email(), credentials.password())) {
            throw new com.ms.user.exception.InvalidCredentialsException("Invalid email or password");
        }
        return service.getUserWithRoleByEmail(credentials.email());
    }

}
