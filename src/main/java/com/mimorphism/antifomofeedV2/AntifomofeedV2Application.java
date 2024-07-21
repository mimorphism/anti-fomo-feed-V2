package com.mimorphism.antifomofeedV2;

import com.mimorphism.antifomofeedV2.configuration.DiscordSecretProperty;
import com.mimorphism.antifomofeedV2.repository.Stats;
import com.mimorphism.antifomofeedV2.repository.StatsRepo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(DiscordSecretProperty.class)
public class AntifomofeedV2Application {


    private final StatsRepo statsRepo;

    public AntifomofeedV2Application(StatsRepo statsRepo) {
        this.statsRepo = statsRepo;
    }

    public static void main(String[] args) {
        SpringApplication.run(AntifomofeedV2Application.class, args);

    }

    @PostConstruct
    public void initStats() {
        if (statsRepo.findById(1).isEmpty()) {
            var bossUser = new Stats();
            bossUser.setTotalViewedAllTime(0L);
            bossUser.setLastProcessedLineInIrcLogFile(0L);
            bossUser.setHackerNewsMinItem(0L);
            bossUser.setHackerNewsMaxItem(0L);
            statsRepo.save(bossUser);
        }
    }

}
