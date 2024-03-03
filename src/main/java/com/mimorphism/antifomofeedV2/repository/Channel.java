package com.mimorphism.antifomofeedV2.repository;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
public class Channel {

    @Id
    private Long id;

    private String name;

    @Column(name = "server_name")
    private String serverName;

    @Column(name = "last_exported")
    private LocalDateTime lastExported;


}
