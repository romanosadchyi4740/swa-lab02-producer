package com.romanosadchyi.labs.appz_lab02.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${rabbitmq.queue.name}")
    private String queueName;

    public Queue queue() {
        return new Queue(queueName);
    }
}
