package com.mimorphism.antifomofeedV2.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface FeedItemRepo extends PagingAndSortingRepository<FeedItem, Long> {

    List<FeedItem> findAllBy(Pageable settings);

    List<FeedItem> findAllByMarkedForDeletionTrue();

    FeedItem findByItemId(Long itemId);

    Long countByOpenedFalseAndMarkedForDeletionFalse();

    Long countByOpenedFalse();

    Long countByOpenedTrue();

}
