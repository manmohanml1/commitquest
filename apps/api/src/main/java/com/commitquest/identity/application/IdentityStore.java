package com.commitquest.identity.application;

import com.commitquest.identity.domain.Account;
import com.commitquest.identity.domain.AccountId;
import com.commitquest.identity.domain.GitHubIdentity;
import com.commitquest.identity.domain.UserSession;
import java.time.Instant;
import java.util.Optional;

public interface IdentityStore {

    void saveOAuthAttempt(String stateDigest, String returnPath, Instant createdAt, Instant expiresAt);

    Optional<String> consumeOAuthAttempt(String stateDigest, Instant now);

    void linkOAuthAttempt(String stateDigest, AccountId accountId);

    Account reconcile(GitHubIdentity identity, Instant now);

    Optional<Account> findAccount(AccountId accountId);

    void saveSession(UserSession session);

    Optional<UserSession> findActiveSession(String tokenDigest, Instant now);

    void revokeSession(String tokenDigest, Instant now);

    void revokeAllSessions(AccountId accountId, Instant now);
}
