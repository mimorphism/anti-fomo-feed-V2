package com.mimorphism.antifomofeedV2.service;

import com.mimorphism.antifomofeedV2.dto.PagedResponse;
import com.mimorphism.antifomofeedV2.dto.StatsUpdate;
import com.mimorphism.antifomofeedV2.enums.SourceType;
import com.mimorphism.antifomofeedV2.repository.FeedItem;
import com.mimorphism.antifomofeedV2.repository.FeedItemRepo;
import com.mimorphism.antifomofeedV2.repository.Stats;
import com.mimorphism.antifomofeedV2.repository.StatsRepo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class StatsService {
    private static final int DEFAULT_ITEM_NUMBER_PER_SOURCE_PER_PAGE = 20;
    private static final Logger log = LoggerFactory.getLogger(StatsService.class);
    private final String API_BASE_URL;
    private final FeedItemRepo feedItemRepo;
    private final StatsRepo statsRepo;
    private final SimpMessagingTemplate simpMessagingTemplate;

    private final WebClient client;


    public StatsService(@Value("${hackernews.api.url}") String apiBaseUrl, FeedItemRepo feedItemRepo, StatsRepo statsRepo, SimpMessagingTemplate simpMessagingTemplate) {
        API_BASE_URL = apiBaseUrl;
        this.feedItemRepo = feedItemRepo;
        this.statsRepo = statsRepo;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.client = WebClient.builder().baseUrl(API_BASE_URL).build();
    }

    public PagedResponse getFeedResponse(int page, List<SourceType> sourceTypes, String sortDirection) {
        page -= 1;
        PagedResponse response = new PagedResponse();
        var totalViewedAllTime = statsRepo.findById(getBossUser().id).orElseThrow().getTotalViewedAllTime();
        var contents = getFeedItems(page, sourceTypes, sortDirection);
        // first page
        if (page == 0) {
            var firstPage = contents;
            response.setTotalPages(firstPage.getTotalPages());
            response.setContent(firstPage.getContent());
            response.setTotalItem(firstPage.getTotalElements());
            response.setCurrentPage(page + 1);
            response.setInitialStatsUpdate(getStatsUpdate());
        } else {
            response.setContent(contents.getContent());
            response.setCurrentPage(page + 1);
        }
        response.setTotalViewedAllTime(totalViewedAllTime);
        return response;
    }

    public Page<FeedItem> getFeedItems(int page, List<SourceType> sourceTypes, String sortDirection) {
        return feedItemRepo.findBySourceIn(PageRequest.of(page,
                DEFAULT_ITEM_NUMBER_PER_SOURCE_PER_PAGE,
                Sort.by("DESC".equals(sortDirection.toUpperCase()) ? Sort.Direction.DESC : Sort.Direction.ASC, "creationDate")), sourceTypes.stream()
                .map(Enum::name)
                .collect(Collectors.toList()));
    }

    public StatsUpdate getStatsUpdate() {
        var totalViewedAllTime = Objects.requireNonNull(getBossUser()).getTotalViewedAllTime();
        var remaining = feedItemRepo.countByOpenedFalse();
        if (totalViewedAllTime == null || totalViewedAllTime == 0L) totalViewedAllTime = 0L;
        if (remaining == null || remaining == 0L) remaining = 0L;
        return new StatsUpdate(totalViewedAllTime, remaining);
    }

    public void updateItemOpened(Long itemId) {
        var item = feedItemRepo.findByItemId(itemId);
        if (item != null) {
            item.setOpened(true);
            var bossUser = getBossUser();
            bossUser.setTotalViewedAllTime(bossUser.getTotalViewedAllTime() + 1);
            statsRepo.save(bossUser);
            feedItemRepo.save(item);
        }

    }

    public void sendStatsUpdate() {
        simpMessagingTemplate.convertAndSend("/topic/statsupdate", getStatsUpdate());
    }

    public void markForDeletion(Long itemId) {
        var item = feedItemRepo.findByItemId(itemId);
        if (item != null) {
//            feedItemRepo.delete(item);
            //soft delete
            item.setDeleted(true);
            feedItemRepo.save(item);
            sendStatsUpdate();
        } else {
            log.error("Failed to mark item for deletion with id:" + itemId);
        }
    }

    public Long getHackerNewsMaxItem() {
        Long defaultValue = 1L;
//        try {
        var latestValueFromHackerNews = client.get()
                .uri("/maxitem.json")
                .retrieve()
                .onStatus(status -> status.value() >= HttpStatus.FORBIDDEN.value(),
                        error -> Mono.error(new RuntimeException("HackerNews API Error")))
                .bodyToMono(String.class)
                .onErrorComplete()
                .block();
        if (!StringUtils.isBlank(latestValueFromHackerNews)) {
            var bossUser = getBossUser();
            bossUser.setHackerNewsMaxItem(Long.valueOf(latestValueFromHackerNews));
            statsRepo.save(bossUser);
            return Long.valueOf(latestValueFromHackerNews);
        }

        var valueFromDb = getBossUser().getHackerNewsMaxItem();
        if (valueFromDb != null && valueFromDb > 0L) {
            return valueFromDb;
        }
        return defaultValue;
    }

    public Long getHackerNewsMinItem() {
        Long defaultValue = 1L;
        var valueFromDb = Objects.requireNonNull(getBossUser()).getHackerNewsMinItem();
        if (valueFromDb == null) {
            var bossUser = getBossUser();
            bossUser.setHackerNewsMinItem(Long.valueOf(defaultValue));
            statsRepo.save(bossUser);
            return defaultValue;
        } else {
            return valueFromDb;
        }
    }

    public void setHackerNewsMinItem(Long currentItem) {
        var bossUser = statsRepo.findById(getBossUser().getId()).orElseThrow();
        bossUser.setHackerNewsMinItem(currentItem);
        statsRepo.save(bossUser);
    }

    public long getLineToStartProcessingForIRC() {
        return Objects.requireNonNull(getBossUser()).getLastProcessedLineInIrcLogFile();
    }

    public void setLineToStartProcessingForIRC(long currentLine) {
        var bossUser = getBossUser();
        bossUser.setLastProcessedLineInIrcLogFile(currentLine);
        statsRepo.save(bossUser);
    }

    private Stats getBossUser() {
        try {
            return statsRepo.findById(1).orElseThrow();
        } catch (NoSuchElementException ex) {
            log.error("Cannot find boss user!");
        }
        return null;
    }


}
