package com.ms.product.producer;

import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProductProducer {
    private final RabbitTemplate rabbitTemplate;

    ProductProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value(value = "${broker.queue.order.stock-update-failed.name}")
    private String stockUpdateFailedRoutingKey;

    public void publishStockUpdateFailedEvent(@Valid Object stockUpdateFailedEvent) {
        rabbitTemplate.convertAndSend(stockUpdateFailedRoutingKey, stockUpdateFailedEvent);
    }
}

