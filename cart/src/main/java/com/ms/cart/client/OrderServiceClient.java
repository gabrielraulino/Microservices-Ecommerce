package com.ms.cart.client;

import com.ms.cart.dto.CreateOrderRequest;
import com.ms.cart.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order")
public interface OrderServiceClient {
    
    @PostMapping("/orders/create")
    OrderDTO createOrder(@RequestBody CreateOrderRequest orderData);
}
