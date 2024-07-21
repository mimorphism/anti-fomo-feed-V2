package com.mimorphism.antifomofeedV2.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String LINK_TO_PROCESS_QUEUE = "link-to-process";

    public static final String PROCESSED_LINK_QUEUE = "processed-link";

    public static final String EXCHANGE_NAME = "antiFomoFeedExchange";
    public static final String ROUTING_KEY = "antiFomoFeed.#";


    @Bean
    public DirectExchange direct() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    Queue linkToProcessQueue() {
        return new Queue(LINK_TO_PROCESS_QUEUE, true);
    }

    @Bean
    Queue processedLinkQueue() {
        return new Queue(PROCESSED_LINK_QUEUE, true);
    }

    @Bean
    public Binding bindingForLinkToProcess(DirectExchange direct,
                                           Queue linkToProcessQueue) {
        return BindingBuilder.bind(linkToProcessQueue)
                .to(direct)
                .with("link-to-process");
    }

    @Bean
    public Binding bindingForProcessedLink(DirectExchange direct,
                                           Queue processedLinkQueue) {
        return BindingBuilder.bind(processedLinkQueue)
                .to(direct)
                .with("processed-link");
    }

}
