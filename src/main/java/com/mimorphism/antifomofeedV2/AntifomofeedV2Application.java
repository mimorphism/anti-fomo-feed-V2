package com.mimorphism.antifomofeedV2;

import com.mimorphism.antifomofeedV2.repository.Stats;
import com.mimorphism.antifomofeedV2.repository.StatsRepo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
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
            var idOne = new Stats();
            idOne.setTotalViewedAllTime(0L);
            idOne.setLastProcessedLineInIrcLogFile(0L);
            statsRepo.save(idOne);
        }
    }

}
