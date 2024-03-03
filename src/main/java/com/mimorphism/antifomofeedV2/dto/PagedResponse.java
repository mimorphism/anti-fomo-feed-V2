package com.mimorphism.antifomofeedV2.dto;

import com.mimorphism.antifomofeedV2.repository.FeedItem;
import lombok.Data;

import java.util.List;

@Data
public class PagedResponse {

    private List<FeedItem> content;
    private Long totalViewedAllTime;
    private long totalItem;
    private int totalPages;
    private int currentPage;
    private StatsUpdate initialStatsUpdate;
}
