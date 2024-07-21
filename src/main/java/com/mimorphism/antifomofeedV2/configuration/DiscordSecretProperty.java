package com.mimorphism.antifomofeedV2.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("discordsecret")
public class DiscordSecretProperty {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}