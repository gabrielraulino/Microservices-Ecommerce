//package com.ms.order;
//
//import com.ms.order.repository.OrderRepository;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Random;
//
//@Service
//@AllArgsConstructor
//@Slf4j
//public class OrderProcessingService {
//
//    private final OrderRepository orderRepository;
//    private final ApplicationEventPublisher eventPublisher;
//    private final Random random = new Random();
//
//    /**
//     * Processes order creation event.
//     * Simulates processing delay (2-10 minutes) then updates status to PROCESSING.
//     */
//    @ApplicationModuleListener
//    public void onOrderCreated(OrderCreatedEvent event) {
//        log.info("Order {} created. Starting processing simulation...", event.orderId());
//
//        // Random delay between 2-10 minutes (120000-600000 ms)
//        int delayMinutes = 2 + random.nextInt(9); // 2 to 10 minutes
//        int delayMs = delayMinutes * 60 * 1000;
//
//        log.info("Order {} will be processed in {} minutes", event.orderId(), delayMinutes);
//
//        try {
//            Thread.sleep(delayMs);
//
//            Order order = orderRepository.findById(event.orderId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Order", event.orderId()));
//
//            // Check if order wasn't cancelled
//            if (order.getStatus() != OrderStatus.CANCELLED) {
//                order.setStatus(OrderStatus.PROCESSING);
//                order.setUpdatedAt(LocalDateTime.now());
//                orderRepository.save(order);
//
//                log.info("Order {} status updated: PENDING -> PROCESSING (after {} minutes)",
//                        event.orderId(), delayMinutes);
//
//                // Publish event for shipping
//                OrderProcessedEvent processedEvent = new OrderProcessedEvent(
//                        order.getId(),
//                        order.getUserId()
//                );
//                eventPublisher.publishEvent(processedEvent);
//            } else {
//                log.info("Order {} was cancelled. Skipping processing.", event.orderId());
//            }
//
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            log.error("Order {} processing was interrupted", event.orderId(), e);
//        }
//
//    }
//
//    /**
//     * Processes order processed event.
//     * Simulates shipping delay (2-10 minutes) then updates status to SHIPPED.
//     */
//    @ApplicationModuleListener
//    public void onOrderProcessed(OrderProcessedEvent event) {
//        log.info("Order {} processed. Starting shipping simulation...", event.orderId());
//
//        // Random delay between 2-10 minutes
//        int delayMinutes = 2 + random.nextInt(9);
//        int delayMs = delayMinutes * 60 * 1000;
//
//        log.info("Order {} will be shipped in {} minutes", event.orderId(), delayMinutes);
//
//        try {
//            Thread.sleep(delayMs);
//
//            Order order = orderRepository.findById(event.orderId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Order", event.orderId()));
//
//            // Check if order wasn't cancelled
//            if (order.getStatus() != OrderStatus.CANCELLED) {
//                order.setStatus(OrderStatus.SHIPPED);
//                order.setUpdatedAt(LocalDateTime.now());
//                orderRepository.save(order);
//
//                log.info("Order {} status updated: PROCESSING -> SHIPPED (after {} minutes)",
//                        event.orderId(), delayMinutes);
//
//                // Publish event for delivery
//                OrderShippedEvent shippedEvent = new OrderShippedEvent(
//                        order.getId(),
//                        order.getUserId()
//                );
//                eventPublisher.publishEvent(shippedEvent);
//            } else {
//                log.info("Order {} was cancelled. Skipping shipping.", event.orderId());
//            }
//
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            log.error("Order {} shipping was interrupted", event.orderId(), e);
//        }
//
//    }
//
//    /**
//     * Processes order shipped event.
//     * Simulates delivery delay (2-10 minutes) then updates status to DELIVERED.
//     */
//    @ApplicationModuleListener
//    public void onOrderShipped(OrderShippedEvent event) {
//        log.info("Order {} shipped. Starting delivery simulation...", event.orderId());
//
//        // Random delay between 2-10 minutes
//        int delayMinutes = 2 + random.nextInt(9);
//        int delayMs = delayMinutes * 60 * 1000;
//
//        log.info("Order {} will be delivered in {} minutes", event.orderId(), delayMinutes);
//
//        try {
//            Thread.sleep(delayMs);
//
//            Order order = orderRepository.findById(event.orderId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Order", event.orderId()));
//
//            // Check if order wasn't cancelled
//            if (order.getStatus() != OrderStatus.CANCELLED) {
//                order.setStatus(OrderStatus.DELIVERED);
//                order.setUpdatedAt(LocalDateTime.now());
//                orderRepository.save(order);
//
//                log.info("Order {} status updated: SHIPPED -> DELIVERED (after {} minutes)",
//                        event.orderId(), delayMinutes);
//            } else {
//                log.info("Order {} was cancelled. Skipping delivery.", event.orderId());
//            }
//
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            log.error("Order {} delivery was interrupted", event.orderId(), e);
//        }
//
//    }
//}