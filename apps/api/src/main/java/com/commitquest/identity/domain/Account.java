package com.commitquest.identity.domain;

import java.time.Instant;
import java.util.Objects;

public record Account(
        AccountId id,
        long githubUserId,
        String githubLogin,
        String displayName,
        String avatarUrl,
        Instant createdAt,
        Instant updatedAt) {

    public Account {
        Objects.requireNonNull(id, "Account ID is required.");
        if (githubUserId <= 0) throw new IllegalArgumentException("GitHub user ID must be positive.");
        if (githubLogin == null || githubLogin.isBlank()) {
            throw new IllegalArgumentException("GitHub login is required.");
        }
        displayName = displayName == null ? "" : displayName;
        avatarUrl = avatarUrl == null ? "" : avatarUrl;
        Objects.requireNonNull(createdAt, "Account creation time is required.");
        Objects.requireNonNull(updatedAt, "Account update time is required.");
        if (updatedAt.isBefore(createdAt)) throw new IllegalArgumentException("Account timestamps are invalid.");
    }
}
