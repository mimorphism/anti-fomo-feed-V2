package com.mimorphism.antifomofeedV2.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ChannelRepo extends CrudRepository<Channel, Long> {

    Optional<Channel> findById(Long channelId);
}
