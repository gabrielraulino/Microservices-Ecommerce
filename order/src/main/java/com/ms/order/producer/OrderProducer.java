package com.ms.order.producer;

import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderProducer {
    private final RabbitTemplate rabbitTemplate;

    OrderProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value(value = "${broker.queue.order.cancelled.name}")
    private String cancelledOrderRoutingKey;

    @Value(value = "${broker.queue.product.update-stock.name}")
    private String updateStockRoutingKey;

    @Value(value = "${broker.queue.order.created.name}")
    private String orderCreatedRoutingKey;

    @Value(value = "${broker.queue.order.processed.name}")
    private String orderProcessedRoutingKey;

    @Value(value = "${broker.queue.order.shipped.name}")
    private String orderShippedRoutingKey;

    public void publishOrderCancelledEvent(@Valid Object orderCancelledEvent) {
        rabbitTemplate.convertAndSend(cancelledOrderRoutingKey, orderCancelledEvent);
    }

    public void publishUpdateStockEvent(@Valid Object updateStockEvent) {
        rabbitTemplate.convertAndSend(updateStockRoutingKey, updateStockEvent);
    }

    public void publishOrderCreatedEvent(@Valid Object orderCreatedEvent) {
        rabbitTemplate.convertAndSend(orderCreatedRoutingKey, orderCreatedEvent);
    }

    public void publishOrderProcessedEvent(@Valid Object orderProcessedEvent) {
        rabbitTemplate.convertAndSend(orderProcessedRoutingKey, orderProcessedEvent);
    }

    public void publishOrderShippedEvent(@Valid Object orderShippedEvent) {
        rabbitTemplate.convertAndSend(orderShippedRoutingKey, orderShippedEvent);
    }
}

