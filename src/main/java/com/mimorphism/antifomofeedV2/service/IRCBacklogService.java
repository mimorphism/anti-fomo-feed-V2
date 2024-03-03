package com.mimorphism.antifomofeedV2.service;

import com.mimorphism.antifomofeedV2.repository.FeedItem;
import com.mimorphism.antifomofeedV2.repository.FeedItemRepo;
import com.mimorphism.antifomofeedV2.repository.StatsRepo;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
/**
 * Service class for IRC backlog processing
 *
 * <p>Important acronym: LPG(stands for Link Preview Generator Service)</p>
 */
public class IRCBacklogService {

    private static final Logger log = LoggerFactory.getLogger(IRCBacklogService.class);
    private final FeedItemRepo feedItemRepo;
    private final StatsRepo statsRepo;

    private final StatsService statsService;
    private final String URL_LOG_FILE;
    private final WebClient client;
    private final String FEED_GENERATOR_ENDPOINT;
    private final UrlValidator urlValidator;
    private final int NO_OF_LINKS_TO_PROCESS_PER_SUBMISSION_TO_LPG;
    private final int NO_OF_LINKS_TO_PROCESS_PER_BATCH;


    public IRCBacklogService(
            FeedItemRepo feedItemRepo,
            @Value("${log.file}") String urlLogFile,
            @Value("${feed.generator.endpoint}") String feedGeneratorEndpoint,
            StatsRepo statsRepo,
            StatsService statsService, @Value("${no.of.links.to.process.for.irc}") int noOfLinksToProcess,
            @Value("${no.of.items.to.process.for.per.batch}") int noOfLinksToProcessPerBatch) {
        this.feedItemRepo = feedItemRepo;
        this.FEED_GENERATOR_ENDPOINT = feedGeneratorEndpoint;
        this.URL_LOG_FILE = urlLogFile;
        this.statsRepo = statsRepo;
        this.statsService = statsService;
        NO_OF_LINKS_TO_PROCESS_PER_SUBMISSION_TO_LPG = noOfLinksToProcess;
        this.client = WebClient.builder().baseUrl(FEED_GENERATOR_ENDPOINT).build();
        this.urlValidator = new UrlValidator(new String[]{"http", "https"}, UrlValidator.ALLOW_ALL_SCHEMES);
        this.NO_OF_LINKS_TO_PROCESS_PER_BATCH = noOfLinksToProcessPerBatch;
    }


    @Async
    @Scheduled(cron = "${processing.schedule.irc}")
    public void triggerProcess() {
        var greeting = RandomStringUtils.random(10, true, false);
        var healthCheck = client.get()
                .uri("/healthcheck/" + greeting)
                .retrieve()
                .onStatus(status -> status.value() >= HttpStatus.FORBIDDEN.value(),
                        error -> Mono.error(new RuntimeException("link preview generator service error")))
                .bodyToMono(String.class)
                .block();
        if (StringUtils.equalsIgnoreCase(MessageFormat.format("WE UP! {0}", greeting), healthCheck)) {
            var executorService = Executors.newSingleThreadScheduledExecutor();
            Path filePath = Paths.get(URL_LOG_FILE);
            int limitPerBatch = 0;
            long currentLine = 0;
            long lineToStartProcessing = statsRepo.findById(1).orElseThrow().getLastProcessedLineInIrcLogFile();

            try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ);
                 FileLock lock = channel.lock(0, Long.MAX_VALUE, true);
                 BufferedReader reader = Files.newBufferedReader(filePath)) {
                if (lock != null) {
                    String line = "";
                    var urls = new ArrayList<String>();
                    while (limitPerBatch < NO_OF_LINKS_TO_PROCESS_PER_BATCH) {
                        if (currentLine < lineToStartProcessing) {
                            log.info("jumping to last processed line");
                            reader.readLine();
                            currentLine++;
                            continue;
                        }
                        line = reader.readLine();
                        log.info("processing link: " + line);
                        if (urlValidator.isValid(line)) {
                            log.info("processing line: {}", line);
                            urls.add(line);
                            limitPerBatch++;
                        } else {
                            log.info("invalid URL. skipped: {}", line);
                        }

                        ++currentLine;
                    }
                    if (urls.size() == NO_OF_LINKS_TO_PROCESS_PER_BATCH) {
                        executorService.schedule(getLinkProcessingTask(urls), 1, TimeUnit.SECONDS);
                    }
                }
                log.info("ALL LINKS FINISHED PROCESSING.TOTAL PROCESSED: {}", currentLine);
            } catch (IOException e) {
                executorService.shutdownNow();
                throw new RuntimeException(e);
            } finally {
                var bossUser = statsRepo.findById(1).orElseThrow();
                bossUser.setLastProcessedLineInIrcLogFile(currentLine);
                statsRepo.save(bossUser);
                log.info("last processed line saved to stats table: {}", currentLine);
            }
        } else {
            log.error("Failed to invoke LINK PREVIEW GENERATOR SERVICE!");
            log.error("Process aborted!");
        }
    }

    private void processLink(List<String> url) {
        Map<String, List<String>> bodyValues = new HashMap<>();
        bodyValues.put("urls", url);
        List<FeedItem> response = client.post()
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
        if (!response.isEmpty()) {
            for (var processedLink : response) {
                //possible null value due to failed processing of a url(e.g dead link)
                if (processedLink != null && !StringUtils.isBlank(processedLink.getUrl())) {
                    processedLink.setSource("IRC");
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

    private Runnable getLinkProcessingTask(List<String> urls) {
        Runnable task = () -> {
            processLink(urls);
            log.info("Job of " + urls.size() + " passsed to process executor service");
        };
        return task;
    }

}