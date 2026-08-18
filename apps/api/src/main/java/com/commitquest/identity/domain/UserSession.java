package com.commitquest.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record UserSession(
        UUID id,
        AccountId accountId,
        String tokenDigest,
        String csrfDigest,
        Instant createdAt,
        Instant expiresAt,
        Instant revokedAt) {

    public UserSession {
        Objects.requireNonNull(id, "Session ID is required.");
        Objects.requireNonNull(accountId, "Session account is required.");
        if (tokenDigest == null || tokenDigest.isBlank()) throw new IllegalArgumentException("Token digest is required.");
        if (csrfDigest == null || csrfDigest.isBlank()) throw new IllegalArgumentException("CSRF digest is required.");
        Objects.requireNonNull(createdAt, "Session creation time is required.");
        Objects.requireNonNull(expiresAt, "Session expiry is required.");
        if (!expiresAt.isAfter(createdAt)) throw new IllegalArgumentException("Session expiry must follow creation.");
    }

    public boolean activeAt(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}
