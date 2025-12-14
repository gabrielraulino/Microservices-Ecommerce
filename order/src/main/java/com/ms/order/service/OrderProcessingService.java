package com.ms.order.service;

import com.ms.order.dto.OrderCreatedEvent;
import com.ms.order.dto.OrderProcessedEvent;
import com.ms.order.dto.OrderShippedEvent;
import com.ms.order.enums.OrderStatus;
import com.ms.order.exception.ResourceNotFoundException;
import com.ms.order.model.Order;
import com.ms.order.producer.OrderProducer;
import com.ms.order.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service responsible for processing order status transitions asynchronously.
 * Simulates real-world order processing delays (2-10 minutes per stage).
 */
@Service
@AllArgsConstructor
@Slf4j
public class OrderProcessingService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;
    private final Random random = new Random();

    /**
     * Processes order creation event.
     * Simulates processing delay (2-10 minutes) then updates status to PROCESSING.
     */
    @RabbitListener(queues = "${broker.queue.order.created.name}")
    public void onOrderCreated(@Payload OrderCreatedEvent event) {
        log.info("Order {} created. Starting processing simulation...", event.orderId());

        // Process asynchronously to avoid blocking RabbitMQ listener
        processOrderCreatedAsync(event);
    }

    @Async("orderProcessingExecutor")
    void processOrderCreatedAsync(OrderCreatedEvent event) {
        // Random delay between 2-10 minutes (120000-600000 ms)
        int delayMinutes = 2 + random.nextInt(9); // 2 to 10 minutes
        int delayMs = delayMinutes * 60 * 1000;

        log.info("Order {} will be processed in {} minutes", event.orderId(), delayMinutes);

        try {
            Thread.sleep(delayMs);

            processOrderStatusUpdate(event.orderId(), OrderStatus.PROCESSING, 
                    "PENDING -> PROCESSING", delayMinutes);

            // Publish event for shipping
            OrderProcessedEvent processedEvent = new OrderProcessedEvent(
                    event.orderId(),
                    event.userId()
            );
            orderProducer.publishOrderProcessedEvent(processedEvent);
            log.info("OrderProcessedEvent published for order: {}", event.orderId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Order {} processing was interrupted", event.orderId(), e);
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent for order: {}", event.orderId(), e);
            // Não rejeita para evitar loop infinito - apenas loga o erro
        }
    }

    /**
     * Processes order processed event.
     * Simulates shipping delay (2-10 minutes) then updates status to SHIPPED.
     */
    @RabbitListener(queues = "${broker.queue.order.processed.name}")
    public void onOrderProcessed(@Payload OrderProcessedEvent event) {
        log.info("Order {} processed. Starting shipping simulation...", event.orderId());

        // Process asynchronously to avoid blocking RabbitMQ listener
        processOrderProcessedAsync(event);
    }

    @Async("orderProcessingExecutor")
    void processOrderProcessedAsync(OrderProcessedEvent event) {
        // Random delay between 2-10 minutes
        int delayMinutes = 2 + random.nextInt(9);
        int delayMs = delayMinutes * 60 * 1000;

        log.info("Order {} will be shipped in {} minutes", event.orderId(), delayMinutes);

        try {
            Thread.sleep(delayMs);

            processOrderStatusUpdate(event.orderId(), OrderStatus.SHIPPED,
                    "PROCESSING -> SHIPPED", delayMinutes);

            // Publish event for delivery
            OrderShippedEvent shippedEvent = new OrderShippedEvent(
                    event.orderId(),
                    event.userId()
            );
            orderProducer.publishOrderShippedEvent(shippedEvent);
            log.info("OrderShippedEvent published for order: {}", event.orderId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Order {} shipping was interrupted", event.orderId(), e);
        } catch (Exception e) {
            log.error("Error processing OrderProcessedEvent for order: {}", event.orderId(), e);
            // Não rejeita para evitar loop infinito - apenas loga o erro
        }
    }

    /**
     * Processes order shipped event.
     * Simulates delivery delay (2-10 minutes) then updates status to DELIVERED.
     */
    @RabbitListener(queues = "${broker.queue.order.shipped.name}")
    public void onOrderShipped(@Payload OrderShippedEvent event) {
        log.info("Order {} shipped. Starting delivery simulation...", event.orderId());

        // Process asynchronously to avoid blocking RabbitMQ listener
        processOrderShippedAsync(event);
    }

    @Async("orderProcessingExecutor")
    void processOrderShippedAsync(OrderShippedEvent event) {
        // Random delay between 2-10 minutes
        int delayMinutes = 2 + random.nextInt(9);
        int delayMs = delayMinutes * 60 * 1000;

        log.info("Order {} will be delivered in {} minutes", event.orderId(), delayMinutes);

        try {
            Thread.sleep(delayMs);

            processOrderStatusUpdate(event.orderId(), OrderStatus.DELIVERED,
                    "SHIPPED -> DELIVERED", delayMinutes);

            log.info("Order {} completed delivery workflow", event.orderId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Order {} delivery was interrupted", event.orderId(), e);
        } catch (Exception e) {
            log.error("Error processing OrderShippedEvent for order: {}", event.orderId(), e);
            // Não rejeita para evitar loop infinito - apenas loga o erro
        }
    }

    /**
     * Helper method to update order status, checking if order wasn't cancelled.
     */
    @Transactional
    void processOrderStatusUpdate(Long orderId, OrderStatus newStatus,
                                         String statusTransition, int delayMinutes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Check if order wasn't cancelled
        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.info("Order {} was cancelled. Skipping status update to {}.", orderId, newStatus);
            return;
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order {} status updated: {} (after {} minutes)",
                orderId, statusTransition, delayMinutes);
    }
}
