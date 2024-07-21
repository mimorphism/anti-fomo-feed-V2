package com.mimorphism.antifomofeedV2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mimorphism.antifomofeedV2.enums.SourceType;
import com.mimorphism.antifomofeedV2.repository.FeedItemRepo;
import com.mimorphism.antifomofeedV2.repository.StatsRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class HackerNewsService {
    private static final Logger log = LoggerFactory.getLogger(HackerNewsService.class);

    private final String API_BASE_URL;

    private final int NO_OF_ITEMS_PER_BATCH;
    private final StatsService statsService;
    private final WebClient hackerNewsClient;
    private final StatsRepo statsRepo;

    private final LinkPreviewGeneratorService linkPreviewGeneratorService;

    public HackerNewsService(@Value("${hackernews.api.url}") String apiBaseUrl,
                             @Value("${hackernews.no.of.items.per.batch}") int noOfItemsPerBatch, StatsService statsService, FeedItemRepo feedItemRepo, StatsRepo statsRepo, LinkPreviewGeneratorService linkPreviewGeneratorService) {
        API_BASE_URL = apiBaseUrl;
        NO_OF_ITEMS_PER_BATCH = noOfItemsPerBatch;
        this.statsService = statsService;
        this.statsRepo = statsRepo;
        this.linkPreviewGeneratorService = linkPreviewGeneratorService;
        this.hackerNewsClient = WebClient.builder().baseUrl(API_BASE_URL).build();
    }

    @Async
    @Scheduled(cron = "${processing.schedule.irc}")
    public void triggerProcess() {
        var maxItem = statsService.getHackerNewsMaxItem();
        var minItem = statsService.getHackerNewsMinItem();
        var numberOfItemsPerBatch = 50L;

        if (maxItem == 1L) {
            log.error("Can't retrieve max item. Most probably error in accessing HackerNews's API");
            return;
        }
        var currentItem = Long.valueOf(minItem);
        List<CompletableFuture<String>> urlRetrievingJobs = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        while (currentItem <= (minItem + numberOfItemsPerBatch)) {
            if (currentItem.equals(maxItem)) {
                break;
            }
            Long finalCurrentItem = currentItem;
            try {
                CompletableFuture<String> currentJob = CompletableFuture
                        .supplyAsync(
                                () -> {
                                    var jsonItem = hackerNewsClient.get()
                                            .uri(uriBuilder -> uriBuilder.path(
                                                    "/item/{currentItem}"
                                            ).build(finalCurrentItem + ".json"))
                                            .retrieve()
                                            .onStatus(status -> status.value() >= HttpStatus.FORBIDDEN.value(),
                                                    error -> Mono.error(new RuntimeException("HackerNews API Error")))
                                            .bodyToMono(JsonNode.class)
                                            .onErrorStop()
                                            .block();

                                    if (jsonItem != null && jsonItem.get("url") != null) {
                                        return jsonItem.get("url").asText();
                                    }
                                    return null;
                                },
                                executor);
                urlRetrievingJobs.add(currentJob);
                currentItem++;

            } catch (RuntimeException ex) {
                log.error("HackerNews API probably down. Aborting process...");
                break;
            }
        }
        List<String> urls = CompletableFuture.allOf(urlRetrievingJobs.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> null)
                .thenApply(v -> urlRetrievingJobs.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toList()))
                .join();

        if (!urls.isEmpty()) {
            linkPreviewGeneratorService.processLink(urls, SourceType.HACKERNEWS);
        }
        //TODO make this proper.
        statsService.setHackerNewsMinItem(currentItem);
    }


}
