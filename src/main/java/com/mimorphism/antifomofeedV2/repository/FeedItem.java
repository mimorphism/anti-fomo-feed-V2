package com.mimorphism.antifomofeedV2.repository;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Entity
public class FeedItem {

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String url;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String title = "No Title";

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String domain;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String description = "No Description";

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String image = "No image";

    private boolean markedForDeletion = false;

    private boolean opened = false;

    private String source;

    private LocalDateTime creationDate;

    @Id
    @GeneratedValue
    private Long itemId;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
}

