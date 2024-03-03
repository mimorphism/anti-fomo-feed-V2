package com.mimorphism.antifomofeedV2.controller;

import com.mimorphism.antifomofeedV2.dto.PagedResponse;
import com.mimorphism.antifomofeedV2.service.DiscordChatExporterService;
import com.mimorphism.antifomofeedV2.service.IRCBacklogService;
import com.mimorphism.antifomofeedV2.service.StatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api")
public class Controller {

    private final DiscordChatExporterService discordChatExporterService;
    private final IRCBacklogService ircBacklogService;
    private final StatsService statsService;

    public Controller(DiscordChatExporterService discordChatExporterService,
                      IRCBacklogService ircBacklogService, StatsService statsService) {
        this.discordChatExporterService = discordChatExporterService;
        this.ircBacklogService = ircBacklogService;
        this.statsService = statsService;
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
            @RequestParam(value = "page", defaultValue = "1") int page) {
        return statsService.getFeedResponse(page);
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
}
