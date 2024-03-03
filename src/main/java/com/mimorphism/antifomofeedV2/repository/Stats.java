package com.mimorphism.antifomofeedV2.repository;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class Stats {

    @Id
    public Integer id = 1;
    public Long totalViewedAllTime;
    public Long lastProcessedLineInIrcLogFile;

}
