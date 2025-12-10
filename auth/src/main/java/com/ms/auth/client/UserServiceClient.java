package com.ms.auth.client;

import com.ms.auth.dto.UserCreateDTO;
import com.ms.auth.dto.UserDTO;
import com.ms.auth.dto.ValidateCredentialsRequest;
import com.ms.auth.dto.UserWithRoleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user")
public interface UserServiceClient {

    @GetMapping("/users/email/{email}")
    UserDTO getUserByEmail(@PathVariable String email);

    @PostMapping("/users")
    UserDTO createUser(@RequestBody UserCreateDTO user);

    @PostMapping("/users/validate-credentials")
    UserWithRoleDTO validateCredentials(@RequestBody ValidateCredentialsRequest request);
}

