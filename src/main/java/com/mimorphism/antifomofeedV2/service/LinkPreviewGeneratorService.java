package com.mimorphism.antifomofeedV2.service;

import com.mimorphism.antifomofeedV2.enums.SourceType;
import com.mimorphism.antifomofeedV2.repository.FeedItem;
import com.mimorphism.antifomofeedV2.repository.FeedItemRepo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LinkPreviewGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(LinkPreviewGeneratorService.class);
    private final WebClient linkPreviewGeneratorClient;
    private final FeedItemRepo feedItemRepo;

    private final StatsService statsService;

    private final RabbitMqMessagingService rabbitMqMessagingService;


    public LinkPreviewGeneratorService(@Value("${feed.generator.endpoint}") String feedGeneratorEndpoint, FeedItemRepo feedItemRepo, StatsService statsService, RabbitMqMessagingService rabbitMqMessagingService) {
        this.linkPreviewGeneratorClient = WebClient.builder().baseUrl(feedGeneratorEndpoint).build();
        this.feedItemRepo = feedItemRepo;
        this.statsService = statsService;
        this.rabbitMqMessagingService = rabbitMqMessagingService;
    }


    public void processLink(List<String> url, SourceType sourceType) {
        Map<String, List<String>> bodyValues = new HashMap<>();
        bodyValues.put("urls", url);
        List<FeedItem> response = sendLinksToBeProcessedToLinkPreviewGeneratorSvc(bodyValues);
        if (!response.isEmpty()) {
            for (var processedLink : response) {
                //possible null value due to failed processing of a url(e.g dead link)
                if (processedLink != null && !StringUtils.isBlank(processedLink.getUrl())) {
                    processedLink.setSource(sourceType.name());
                    processedLink.setCreationDate(LocalDateTime.now());
                    feedItemRepo.save(processedLink);
                    log.info("link processing success for : " + processedLink.getUrl());
                    statsService.sendStatsUpdate();
                }

            }
        } else {
            log.error("Processing by Link Preview Generator service failed!");
        }
    }
    
    private List<FeedItem> sendLinksToBeProcessedToLinkPreviewGeneratorSvc(Map<String, List<String>> bodyValues) {
        return linkPreviewGeneratorClient.post()
                .uri("/generatePreview")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(bodyValues), Map.class)
                .retrieve()
                .onStatus(status -> status.value() >= HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        error -> Mono.error(new RuntimeException("link preview generator service error")))
                .bodyToMono(new ParameterizedTypeReference<List<FeedItem>>() {
                })
                .onErrorStop()
                .block();
    }
}
