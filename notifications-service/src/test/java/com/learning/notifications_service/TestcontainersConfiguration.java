package com.learning.notifications_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    // --- PostgreSQL Container ---
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"))
                .withDatabaseName("testdb")
                .withUsername("sa")
                .withPassword("sa");
    }

    // --- RabbitMQ Container ---
    @Bean
    @ServiceConnection
    RabbitMQContainer rabbitContainer() {
        return new RabbitMQContainer(DockerImageName.parse("rabbitmq:4.0.4-alpine"));
    }

    // --- MailHog Container ---
    @Bean
    GenericContainer<?> mailhog() {
        return new GenericContainer<>(DockerImageName.parse("mailhog/mailhog:v1.0.1")).withExposedPorts(1025, 8025);
    }

    // --- Dynamic properties for MailHog ---
    @Bean
    DynamicPropertyRegistrar dynamicPropertyRegistrar(GenericContainer<?> mailhog) {
        return (registry) -> {
            registry.add("spring.mail.host", mailhog::getHost);
            registry.add("spring.mail.port", mailhog::getFirstMappedPort);
        };
    }

    // --- Dynamic properties for PostgreSQL ---
    @DynamicPropertySource
    static void registerPostgresProps(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"))
                .withDatabaseName("testdb")
                .withUsername("sa")
                .withPassword("sa");
        postgres.start();

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update"); // or "create-drop" for clean tests
    }

    // --- RabbitMQ exchanges, queues, and bindings ---
    @Bean
    public Declarables rabbitDeclarables() {
        TopicExchange exchange = new TopicExchange("orders-exchange");

        Queue newOrders = new Queue("new-orders", false);
        Queue deliveredOrders = new Queue("delivered-orders", false);
        Queue cancelledOrders = new Queue("cancelled-orders", false);
        Queue errorOrders = new Queue("error-orders", false);

        return new Declarables(
                exchange,
                newOrders,
                deliveredOrders,
                cancelledOrders,
                errorOrders,
                BindingBuilder.bind(newOrders).to(exchange).with("new-orders"),
                BindingBuilder.bind(deliveredOrders).to(exchange).with("delivered-orders"),
                BindingBuilder.bind(cancelledOrders).to(exchange).with("cancelled-orders"),
                BindingBuilder.bind(errorOrders).to(exchange).with("error-orders"));
    }

    // --- JSON message converter for RabbitTemplate ---
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
