package com.ms.product.consumer;

import com.ms.product.dto.OrderCancelledEvent;
import com.ms.product.dto.ProductQuantity;
import com.ms.product.dto.StockUpdateFailedEvent;
import com.ms.product.dto.UpdateStockEvent;
import com.ms.product.producer.ProductProducer;
import com.ms.product.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class ProductConsumer {

    private final ProductService productService;
    private final ProductProducer productProducer;

    @RabbitListener(queues = "${broker.queue.product.update-stock.name}")
    public void handleUpdateStockEvent(@Payload UpdateStockEvent event) {
        log.info("Received UpdateStockEvent: cartId={}, orderId={}, userId={}, products={}", 
                 event.cartId(), event.orderId(), event.userId(), event.productQuantities().size());

        try {
            // Convert Map to List of ProductQuantity
            List<ProductQuantity> productQuantities = event.productQuantities().entrySet().stream()
                    .map(entry -> new ProductQuantity(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            // Decrement stock
            productService.decrementProducts(productQuantities);
            
            log.info("Stock updated successfully for order: {}", event.orderId());
        } catch (Exception e) {
            log.error("Error processing UpdateStockEvent for order: {}", event.orderId(), e);
            
            // Publish failure event to OrderService for automatic cancellation
            StockUpdateFailedEvent failureEvent = new StockUpdateFailedEvent(
                    event.orderId(),
                    event.userId(),
                    event.productQuantities(),
                    e.getMessage()
            );
            
            productProducer.publishStockUpdateFailedEvent(failureEvent);
            log.info("StockUpdateFailedEvent published for order: {}", event.orderId());
            
            // Rejeita e não re enfileira para evitar 'loop' infinito
            throw new AmqpRejectAndDontRequeueException("Failed to update stock", e);
        }
    }

    @RabbitListener(queues = "${broker.queue.product.cancelled-order.name}")
    public void handleOrderCancelledEvent(@Payload OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent: orderId={}, userId={}, items={}", 
                 event.orderId(), event.userId(), event.items().size());

        try {
            // Convert to ProductQuantity list
            List<ProductQuantity> productQuantities = event.items().stream()
                    .map(item -> new ProductQuantity(item.productId(), item.quantity()))
                    .collect(Collectors.toList());

            // Restore stock (increment)
            productService.incrementProducts(productQuantities);
            
            log.info("Stock restored successfully for order: {}", event.orderId());
        } catch (Exception e) {
            log.error("Error processing OrderCancelledEvent for order: {}", event.orderId(), e);
            throw new AmqpRejectAndDontRequeueException("Failed to restore stock", e);
        }
    }
}
