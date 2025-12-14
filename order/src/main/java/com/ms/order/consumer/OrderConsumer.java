package com.ms.order.consumer;

import com.ms.order.dto.CheckoutEvent;
import com.ms.order.dto.CreateOrderDTO;
import com.ms.order.dto.StockUpdateFailedEvent;
import com.ms.order.dto.UpdateStockEvent;
import com.ms.order.service.OrderService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = "${broker.queue.order.checkout.name}")
    @Transactional
    public void handleCheckoutEvent(@Payload CheckoutEvent event) {
        log.info("Received CheckoutEvent: cartId={}, userId={}, items={}", 
                 event.cartId(), event.userId(), event.items().size());

        try {
            // Convert CheckoutEvent to CreateOrderDTO
            List<CreateOrderDTO.CreateOrderItemDTO> items = event.items().stream()
                    .map(item -> new CreateOrderDTO.CreateOrderItemDTO(
                            item.productId(),
                            item.quantity()
                    ))
                    .toList();

            CreateOrderDTO orderData = new CreateOrderDTO(
                    event.userId(),
                    items,
                    com.ms.order.enums.PaymentMethod.valueOf(event.paymentMethod())
            );

            // Create order
            var order = orderService.createOrder(orderData);
            log.info("Order created successfully from CheckoutEvent. OrderId: {}, UserId: {}", 
                     order.id(), order.userId());

            // Publish UpdateStockEvent to update product stock
            Map<Long, Integer> productQuantities = event.items().stream()
                    .collect(Collectors.toMap(
                            CheckoutEvent.CheckoutItem::productId,
                            CheckoutEvent.CheckoutItem::quantity
                    ));

            UpdateStockEvent stockEvent = new UpdateStockEvent(
                    event.cartId(),
                    event.userId(),
                    order.id(), // Include orderId for potential rollback
                    productQuantities
            );

            orderService.publishUpdateStockEvent(stockEvent);
            log.info("UpdateStockEvent published for order: {}", order.id());

            // Publish OrderCreatedEvent to start order processing workflow
            orderService.publishOrderCreatedEvent(order.id(), order.userId());
            log.info("OrderCreatedEvent published for order: {}", order.id());

        } catch (Exception e) {
            log.error("Error processing CheckoutEvent for cart: {}", event.cartId(), e);
            // Rejeita e não re enfileira para evitar 'loop' infinito
            throw new AmqpRejectAndDontRequeueException("Failed to process checkout", e);
        }
    }

    @RabbitListener(queues = "${broker.queue.order.stock-update-failed.name}")
    @Transactional
    public void handleStockUpdateFailedEvent(@Payload StockUpdateFailedEvent event) {
        log.error("Received StockUpdateFailedEvent: orderId={}, userId={}, error={}", 
                 event.orderId(), event.userId(), event.errorMessage());

        try {
            orderService.handleStockUpdateFailure(event);
        } catch (Exception e) {
            log.error("Error handling StockUpdateFailedEvent for order: {}", event.orderId(), e);
            // Não rejeita para permitir retry ou processamento manual
        }
    }
}

