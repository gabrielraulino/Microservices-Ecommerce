package com.ms.user.controller;

import com.ms.user.dto.UserCreateDTO;
import com.ms.user.dto.UserDTO;
import com.ms.user.service.UserService;
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

}
