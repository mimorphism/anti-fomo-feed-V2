package com.mimorphism.antifomofeedV2.service;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMqMessagingService {

    private final RabbitTemplate template;
    private final DirectExchange direct;


    public RabbitMqMessagingService(RabbitTemplate template, DirectExchange direct) {
        this.template = template;
        this.direct = direct;
    }

    public void send(String link) {
        template.convertAndSend(direct.getName(), "link-to-process", link);
    }
}
