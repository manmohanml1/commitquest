package com.commitquest.identity.domain;

public record GitHubIdentity(long userId, String login, String displayName, String avatarUrl) {

    public GitHubIdentity {
        if (userId <= 0) throw new IllegalArgumentException("GitHub user ID must be positive.");
        if (login == null || login.isBlank()) throw new IllegalArgumentException("GitHub login is required.");
        displayName = displayName == null ? "" : displayName;
        avatarUrl = avatarUrl == null ? "" : avatarUrl;
    }
}
