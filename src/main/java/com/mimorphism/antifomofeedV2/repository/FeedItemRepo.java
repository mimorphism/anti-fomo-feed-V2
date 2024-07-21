package com.mimorphism.antifomofeedV2.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface FeedItemRepo extends PagingAndSortingRepository<FeedItem, Long> {

    List<FeedItem> findAllBy(Pageable settings);

    Page<FeedItem> findBySourceIn(Pageable settings, List<String> source);

    FeedItem findByItemId(Long itemId);

    Long countByOpenedFalse();

    Long countByOpenedTrue();

}
