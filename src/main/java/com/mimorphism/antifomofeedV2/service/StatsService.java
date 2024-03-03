package com.mimorphism.antifomofeedV2.service;

import com.mimorphism.antifomofeedV2.dto.PagedResponse;
import com.mimorphism.antifomofeedV2.dto.StatsUpdate;
import com.mimorphism.antifomofeedV2.repository.FeedItemRepo;
import com.mimorphism.antifomofeedV2.repository.StatsRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class StatsService {
    private static final int DEFAULT_ITEM_NUMBER_PER_PAGE = 20;
    private static final Logger log = LoggerFactory.getLogger(StatsService.class);
    private final FeedItemRepo feedItemRepo;
    private final StatsRepo statsRepo;
    private final SimpMessagingTemplate simpMessagingTemplate;


    public StatsService(FeedItemRepo feedItemRepo, StatsRepo statsRepo, SimpMessagingTemplate simpMessagingTemplate) {
        this.feedItemRepo = feedItemRepo;
        this.statsRepo = statsRepo;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public PagedResponse getFeedResponse(int page) {
        page -= 1;
        PagedResponse response = new PagedResponse();
        var totalViewedAllTime = statsRepo.findById(1).orElseThrow().getTotalViewedAllTime();
        // first page
        if (page == 0) {
            var firstPage = getFeedItems(page);
            response.setTotalPages(firstPage.getTotalPages());
            response.setContent(firstPage.getContent());
            response.setTotalItem(firstPage.getTotalElements());
            response.setCurrentPage(page + 1);
            response.setInitialStatsUpdate(getStatsUpdate());
        } else {
            response.setContent(getFeedItems(page).getContent());
            response.setCurrentPage(page + 1);
        }
        response.setTotalViewedAllTime(totalViewedAllTime);
        return response;
    }

    public Page getFeedItems(int page) {
        return feedItemRepo.findAll(PageRequest.of(page, DEFAULT_ITEM_NUMBER_PER_PAGE));
    }

    public StatsUpdate getStatsUpdate() {
        var totalViewedAllTime = feedItemRepo.countByOpenedTrue();
        var remaining = feedItemRepo.countByOpenedFalseAndMarkedForDeletionFalse();
        if (totalViewedAllTime == null || totalViewedAllTime == 0L) totalViewedAllTime = 0L;
        if (remaining == null || remaining == 0L) remaining = 0L;
        return new StatsUpdate(totalViewedAllTime, remaining);
    }

    public void updateItemOpened(Long itemId) {
        var item = feedItemRepo.findByItemId(itemId);
        if (item != null) {
            item.setOpened(true);
            feedItemRepo.save(item);
        }

    }

    public void sendStatsUpdate() {
        simpMessagingTemplate.convertAndSend("/topic/statsupdate", getStatsUpdate());
    }

    public void markForDeletion(Long itemId) {
        var item = feedItemRepo.findByItemId(itemId);
        if (item != null) {
            item.setMarkedForDeletion(true);
            feedItemRepo.save(item);
            sendStatsUpdate();
        } else {
            log.error("Failed to mark item for deletion with id:" + itemId);
        }
    }

}
