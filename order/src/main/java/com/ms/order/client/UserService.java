package com.ms.order.client;


import com.ms.order.dto.InternalUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user")
public interface UserService {

    @GetMapping("/users/{user}")
    InternalUserDTO getUser(@PathVariable Long user);

}
