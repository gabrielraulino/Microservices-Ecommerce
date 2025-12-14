package com.ms.order.service;

import com.ms.order.auth.CurrentUserService;
import com.ms.order.client.ProductService;
import com.ms.order.client.UserService;
import com.ms.order.dto.*;
import com.ms.order.exception.ResourceNotFoundException;
import com.ms.order.exception.InvalidOperationException;
import com.ms.order.enums.OrderStatus;
import com.ms.order.model.Order;
import com.ms.order.model.OrderItem;
import com.ms.order.producer.OrderProducer;
import com.ms.order.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository repository;

    private final UserService userService;

    private final ProductService productService;

    private final OrderProducer orderProducer;

    private final CurrentUserService currentUserService;

    /**
     * Publishes UpdateStockEvent to ProductService after order creation.
     */
    public void publishUpdateStockEvent(UpdateStockEvent event) {
        orderProducer.publishUpdateStockEvent(event);
    }

    /**
     * Publishes OrderCreatedEvent to start order processing workflow.
     */
    public void publishOrderCreatedEvent(Long orderId, Long userId) {
        com.ms.order.dto.OrderCreatedEvent event = new com.ms.order.dto.OrderCreatedEvent(orderId, userId);
        orderProducer.publishOrderCreatedEvent(event);
    }

    /**
     * Handles stock update failure by automatically cancelling the order.
     * This is called when ProductService fails to update stock.
     */
    @Transactional
    public void handleStockUpdateFailure(StockUpdateFailedEvent event) {
        log.error("Stock update failed for order: {}. Automatically cancelling order.", event.orderId());
        
        try {
            Order order = findOrderById(event.orderId());
            
            // Only cancel if order is still in a cancellable state
            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PROCESSING) {
                order.setStatus(OrderStatus.CANCELLED);
                order.setUpdatedAt(LocalDateTime.now());
                repository.save(order);
                
                log.info("Order {} automatically cancelled due to stock update failure", event.orderId());
            } else {
                log.warn("Order {} cannot be cancelled. Current status: {}", event.orderId(), order.getStatus());
            }
        } catch (Exception e) {
            log.error("Error cancelling order {} after stock update failure", event.orderId(), e);
        }
    }


    public OrderDTO findById(Long id){
        return buildOrderDTO(repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order", id)));
    }

    public List<OrderDTO> findByUserId(Long id){
        Optional.ofNullable(userService.getUser(id)).orElseThrow(() -> new ResourceNotFoundException("User", id));

        return repository.findByUserId(id).stream().map(this::buildOrderDTO).toList();
    }

    public List<OrderDTO> getAllOrders(Pageable pageable){
        return repository.findAll(pageable)
                .map(this::buildOrderDTO)
                .getContent();
    }

    @Transactional
    public OrderDTO createOrder(CreateOrderDTO orderData) {
        log.info("Creating order for user: {}", orderData.userId());

        // Validate user exists
        Optional.ofNullable(userService.getUser(orderData.userId()))
                .orElseThrow(() -> new ResourceNotFoundException("User", orderData.userId()));

        // Create order
        Order order = Order.builder()
                .userId(orderData.userId())
                .status(OrderStatus.PENDING)
                .paymentMethod(orderData.paymentMethod())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create order items
        List<OrderItem> orderItems = orderData.items().stream()
                .map(item -> {
                    OrderItem orderItem = new OrderItem(item.productId(), item.quantity());
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .toList();

        order.setItems(orderItems);

        // Save order
        Order savedOrder = repository.save(order);
        log.info("Order created successfully. ID: {}, User: {}", savedOrder.getId(), savedOrder.getUserId());

        return buildOrderDTO(savedOrder);
    }

    private OrderDTO buildOrderDTO(Order order){

        List<OrderItemDTO> items = buildOrderItemsDTO(order.getItems());

        BigDecimal totalPrice = items.stream().map(OrderItemDTO::totalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalQuantity = items.stream().map(OrderItemDTO::quantity).reduce(0, Integer::sum);

        return new OrderDTO(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getUpdatedAt(),
                order.getCreatedAt(),
                items,
                order.getPaymentMethod(),
                totalQuantity,
                totalPrice
        );
    }

    private List<OrderItemDTO> buildOrderItemsDTO(List<OrderItem> items){
        if (items.isEmpty()) {
            return List.of();
        }

        // Extract unique product IDs
        Set<Long> productIds = items.stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toSet());

        // Fetch all products in a single call (avoids N+1)
        Map<Long, InternalProductDTO> productMap = productService.findProductsByIds(productIds)
                .stream()
                .collect(Collectors.toMap(InternalProductDTO::id, Function.identity()));

        // Iterate over original items to maintain order and process all
        // Filter items whose products were not found
        return items.stream()
                .filter(item -> productMap.containsKey(item.getProductId()))
                .map(item -> {
                    InternalProductDTO product = productMap.get(item.getProductId());
                    BigDecimal totalPrice = product.price()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    
                    return new OrderItemDTO(
                            item.getId(),
                            product.name(),
                            item.getProductId(),
                            item.getQuantity(),
                            product.price(),
                            totalPrice
                    );
                })
                .toList();
    }

    @Transactional
    public OrderDTO cancelOrder(Long orderId, Long userId) {
        log.info("Starting order cancellation: {} by user: {}", orderId, userId);

        Order order = findOrderById(orderId);

        // Validate that the user owns the order
        validateOrderOwnership(order, userId);

        validateOrderStatusToCancel(order);

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        Order cancelledOrder = repository.save(order);

        // Build cancellation event
        OrderCancelledEvent event = new OrderCancelledEvent(
                order.getId(),
                order.getUserId(),
                order.getItems().stream()
                        .map(item -> new OrderCancelledEvent.CancelledItem(
                                item.getProductId(),
                                item.getQuantity()
                        ))
                        .toList(),
                LocalDateTime.now()
        );

        // Publish event to restore stock (via RabbitMQ)
        orderProducer.publishOrderCancelledEvent(event);

        log.info("Order {} cancelled successfully. Event published to restore stock for {} items",
                orderId, event.items().size());

        return buildOrderDTO(cancelledOrder);
    }

    private Order findOrderById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private void validateOrderOwnership(Order order, Long userId) {
        // Admins can cancel any order
        if (currentUserService.isAdmin()) {
            return;
        }
        
        // Regular users can only cancel their own orders
        if (!order.getUserId().equals(userId)) {
            throw new InvalidOperationException("cancel order", "user can only cancel their own orders");
        }
    }

    private void validateOrderStatusToCancel(Order order){
        if (order.getStatus().equals(OrderStatus.CANCELLED)) {
            throw new InvalidOperationException("cancel order", "order is already cancelled");
        }

        if (order.getStatus().equals(OrderStatus.DELIVERED)) {
            throw new InvalidOperationException("cancel order", "cannot cancel a delivered order");
        }

        if(order.getStatus().equals(OrderStatus.SHIPPED)) {
            throw new InvalidOperationException("cancel order", "cannot cancel a shipped order");
        }
    }
}