package com.mimorphism.antifomofeedV2.controller;

import com.mimorphism.antifomofeedV2.dto.PagedResponse;
import com.mimorphism.antifomofeedV2.enums.SourceType;
import com.mimorphism.antifomofeedV2.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api")
public class AntiFomoFeedController {

    private final DiscordChatExporterService discordChatExporterService;
    private final IRCBacklogService ircBacklogService;
    private final StatsService statsService;

    private final HackerNewsService hackerNewsService;

    private final RabbitMqMessagingService rabbitMqMessagingService;

    public AntiFomoFeedController(DiscordChatExporterService discordChatExporterService,
                                  IRCBacklogService ircBacklogService, StatsService statsService, HackerNewsService hackerNewsService, RabbitMqMessagingService rabbitMqMessagingService) {
        this.discordChatExporterService = discordChatExporterService;
        this.ircBacklogService = ircBacklogService;
        this.statsService = statsService;
        this.hackerNewsService = hackerNewsService;
        this.rabbitMqMessagingService = rabbitMqMessagingService;
    }

    @CrossOrigin
    @GetMapping("/triggerProcess")
    public ResponseEntity triggerProcess() {
        ircBacklogService.triggerProcess();
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/getFeed")
    public PagedResponse getFeed(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sourcetypes") List<SourceType> sourceTypes,
            @RequestParam(value = "sortbydate", defaultValue = "desc") String direction) {
        return statsService.getFeedResponse(page, sourceTypes, direction);
    }

    @CrossOrigin
    @GetMapping("/triggerDiscordProcess")
    public void triggerDiscordProcess(@RequestParam(value = "token", defaultValue = "1") String token) {
//        service.getDiscordServers(token);
        discordChatExporterService.triggerDiscordProcess();
    }

    @CrossOrigin
    @GetMapping("/processDiscordLogs")
    public void processDiscordLogs() {
//        service.getDiscordServers(token);
        discordChatExporterService.processDiscordData();
    }

    @MessageMapping("/updateOpened")
    public void updateOpened(Long itemId) {
        statsService.updateItemOpened(itemId);
        statsService.sendStatsUpdate();
    }

    @MessageMapping("/markForDeletion")
    public void markItemForDeletion(Long itemId) {
        statsService.markForDeletion(itemId);
        log.info("memang masuk confirm boleh delete");
        statsService.sendStatsUpdate();
    }

    @CrossOrigin
    @GetMapping("/triggerHackerNewsProcess")
    public void triggerHackerNewsProcess() {
//        service.getDiscordServers(token);
        hackerNewsService.triggerProcess();
    }

    @CrossOrigin
    @PostMapping("/testSendMessage")
    public void testSendMessage(String message) {
//        service.getDiscordServers(token);
        rabbitMqMessagingService.send(message);
    }
}
