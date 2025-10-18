package com.learning.notifications_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class RabbitTestConfig {

    public static final String EXCHANGE = "order-events-exchange";

    @Bean
    DirectExchange orderEventsExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue newOrdersQueue() {
        return QueueBuilder.durable("new-orders").build();
    }

    @Bean
    Queue deliveredOrdersQueue() {
        return QueueBuilder.durable("delivered-orders").build();
    }

    @Bean
    Queue cancelledOrdersQueue() {
        return QueueBuilder.durable("cancelled-orders").build();
    }

    @Bean
    Queue errorOrdersQueue() {
        return QueueBuilder.durable("error-orders").build();
    }

    @Bean
    Binding newOrdersBinding(DirectExchange orderEventsExchange, Queue newOrdersQueue) {
        return BindingBuilder.bind(newOrdersQueue).to(orderEventsExchange).with("new-orders");
    }

    @Bean
    Binding deliveredOrdersBinding(DirectExchange orderEventsExchange, Queue deliveredOrdersQueue) {
        return BindingBuilder.bind(deliveredOrdersQueue).to(orderEventsExchange).with("delivered-orders");
    }

    @Bean
    Binding cancelledOrdersBinding(DirectExchange orderEventsExchange, Queue cancelledOrdersQueue) {
        return BindingBuilder.bind(cancelledOrdersQueue).to(orderEventsExchange).with("cancelled-orders");
    }

    @Bean
    Binding errorOrdersBinding(DirectExchange orderEventsExchange, Queue errorOrdersQueue) {
        return BindingBuilder.bind(errorOrdersQueue).to(orderEventsExchange).with("error-orders");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
