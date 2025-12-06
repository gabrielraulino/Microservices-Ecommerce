package com.ms.order.consumer;

import com.ms.order.dto.CheckoutEvent;
import com.ms.order.exception.InsufficientStockException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    @RabbitListener(queues = "${broker.queue.order.name}")
    public void listenOrderEvents(@Payload CheckoutEvent payload) {

        System.out.println("Received order event: " + payload);

    }
}
