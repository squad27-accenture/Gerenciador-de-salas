package com.squad27.gerenciadorsalas.messaging;

import com.squad27.gerenciadorsalas.dto.LayoutProcessingMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LayoutProducer {

    private static final Logger log = LoggerFactory.getLogger(LayoutProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key.layout}")
    private String routingKey;

    public LayoutProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarProcessamentoLayout(LayoutProcessingMessageDTO mensagem) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, mensagem);
            log.info("[RabbitMQ] Mensagem publicada para processamento de layout. salaId={}, imagemUrl={}",
                    mensagem.salaId(), mensagem.imagemUrl());
        } catch (AmqpException e) {
            log.error("[RabbitMQ] Falha ao publicar mensagem de layout. salaId={}. Erro: {}",
                    mensagem.salaId(), e.getMessage(), e);
            // Não relança: falha do broker não deve derrubar o upload (RNF-06)
            // O status AGUARDANDO_LAYOUT já foi salvo; o admin pode reenviar
        }
    }
}