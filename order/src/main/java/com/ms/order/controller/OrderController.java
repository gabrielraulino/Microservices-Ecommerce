package com.ms.order.controller;

import com.ms.order.dto.OrderDTO;
import com.ms.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {
    private final OrderService orderService;
    private final com.ms.order.auth.CurrentUserService currentUserService;

    @GetMapping()
    @Operation(summary = "Get all orders with pagination")
    public List<OrderDTO> findAllOrders(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id") Pageable pageable){
        return orderService.getAllOrders(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public OrderDTO findById(@PathVariable Long id){
        return orderService.findById(id);
    }

    @GetMapping("/user")
    @Operation(summary = "Get current user's orders")
    public List<OrderDTO> getCurrentUserOrders(){
        Long userId = currentUserService.getCurrentUserId();
        return orderService.findByUserId(userId);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order by ID")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId();
        OrderDTO order = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(order);
    }
}
