package com.commitquest.persistence;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("commitquest.persistence")
public record ConnectedPersistenceProperties(
        boolean enabled, String url, String username, String password, int maximumPoolSize) {

    public ConnectedPersistenceProperties {
        if (maximumPoolSize < 1 || maximumPoolSize > 10) {
            throw new IllegalArgumentException("Persistence pool size must be between 1 and 10.");
        }
    }

    void requireCompleteConfiguration() {
        if (url == null || url.isBlank()) throw new IllegalStateException("CommitQuest database URL is required.");
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("CommitQuest database username is required.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("CommitQuest database password is required.");
        }
    }
}
