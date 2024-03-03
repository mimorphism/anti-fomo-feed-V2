package com.mimorphism.antifomofeedV2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface DiscordServerRepo extends JpaRepository<DiscordServer, Long> {

}
