package com.ms.order.controller;

import com.ms.order.dto.CreateOrderDTO;
import com.ms.order.dto.OrderDTO;
import com.ms.order.enums.PaymentMethod;
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

//    private final AuthModuleAPI authModuleAPI;

    @PostMapping("/create")
    @Operation(summary = "Create a new order")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequest request) {
        // Convert request to DTO
        CreateOrderDTO orderData = new CreateOrderDTO(
                request.userId(),
                request.items().stream()
                        .map(item -> new CreateOrderDTO.CreateOrderItemDTO(
                                item.productId(),
                                item.quantity()
                        ))
                        .toList(),
                PaymentMethod.valueOf(request.paymentMethod())
        );
        
        OrderDTO order = orderService.createOrder(orderData);
        return ResponseEntity.ok(order);
    }
    
    // Request DTO for Feign client
    record CreateOrderRequest(
            Long userId,
            List<CreateOrderItemRequest> items,
            String paymentMethod
    ) {
        record CreateOrderItemRequest(
                Long productId,
                Integer quantity
        ) {}
    }

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
    public List<OrderDTO> getCurrentUserOrders(@RequestParam Long userId){
//        Long userId = authModuleAPI.getCurrentUserId();
        return orderService.findByUserId(userId);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order by ID")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id, @RequestParam Long userId) {
//        Long userId = authModuleAPI.getCurrentUserId();
        OrderDTO order = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(order);
    }
}
