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

    public void publishOrderCancelledEvent(@Valid Object orderCancelledEvent) {
        rabbitTemplate.convertAndSend(cancelledOrderRoutingKey, orderCancelledEvent);
    }
}

