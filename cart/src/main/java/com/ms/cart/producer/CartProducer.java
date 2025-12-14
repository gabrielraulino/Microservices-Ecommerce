package com.ms.cart.producer;

import com.ms.cart.dto.CheckoutEvent;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CartProducer {
    private final RabbitTemplate rabbitTemplate;

    CartProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value(value = "${broker.queue.cart.checkout.name}")
    private String checkoutRoutingKey;

    @Value(value = "${broker.queue.product.update-stock.name}")
    private String updateStockRoutingKey;

    public void publishCheckoutEvent(@Valid CheckoutEvent checkoutEvent) {
        rabbitTemplate.convertAndSend(checkoutRoutingKey, checkoutEvent);
    }

    public void publishUpdateStockEvent(@Valid Object updateStockEvent) {
        rabbitTemplate.convertAndSend(updateStockRoutingKey, updateStockEvent);
    }
}
