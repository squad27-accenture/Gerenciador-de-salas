package com.squad27.gerenciadorsalas.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.queue.layout}")
    private String layoutQueue;

    @Value("${rabbitmq.queue.layout.dlq}")
    private String layoutDlq;

    @Value("${rabbitmq.routing-key.layout}")
    private String layoutRoutingKey;

    @Value("${rabbitmq.retry.max-attempts}")
    private int maxAttempts;



    // --- Exchange principal ---
    @Bean
    public DirectExchange layoutExchange() {
        return ExchangeBuilder
                .directExchange(exchangeName)
                .durable(true)
                .build();
    }

    // --- Dead Letter Exchange ---
    @Bean
    public DirectExchange layoutDeadLetterExchange() {
        return ExchangeBuilder
                .directExchange(exchangeName + ".dlx")
                .durable(true)
                .build();
    }

    // --- Fila principal com DLQ configurada ---
    @Bean
    public Queue layoutProcessingQueue() {
        return QueueBuilder
                .durable(layoutQueue)
                .withArgument("x-dead-letter-exchange", exchangeName + ".dlx")
                .withArgument("x-dead-letter-routing-key", layoutRoutingKey + ".dlq")
                .withArgument("x-max-retries", maxAttempts) // informativo para o agente
                .build();
    }

    // --- Dead Letter Queue ---
    @Bean
    public Queue layoutDeadLetterQueue() {
        return QueueBuilder
                .durable(layoutDlq)
                .build();
    }

    // --- Bindings ---
    @Bean
    public Binding layoutQueueBinding() {
        return BindingBuilder
                .bind(layoutProcessingQueue())
                .to(layoutExchange())
                .with(layoutRoutingKey);
    }

    @Bean
    public Binding layoutDlqBinding() {
        return BindingBuilder
                .bind(layoutDeadLetterQueue())
                .to(layoutDeadLetterExchange())
                .with(layoutRoutingKey + ".dlq");
    }

    // --- Conversor JSON (serializa o DTO automaticamente) ---
    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // --- RabbitTemplate com conversor JSON ---
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter()); // sem mudança aqui
        return template;
    }
}