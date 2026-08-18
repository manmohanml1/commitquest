package com.commitquest.identity.application;

import com.commitquest.identity.domain.Account;
import com.commitquest.identity.domain.UserSession;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

public final class IdentityService {

    public static final Duration OAUTH_STATE_LIFETIME = Duration.ofMinutes(10);
    public static final Duration SESSION_LIFETIME = Duration.ofDays(7);

    private final IdentityStore store;
    private final OAuthIdentityGateway gateway;
    private final TokenSecurity tokens;
    private final AccountDataDeletion accountDeletion;
    private final Clock clock;

    public IdentityService(
            IdentityStore store,
            OAuthIdentityGateway gateway,
            TokenSecurity tokens,
            AccountDataDeletion accountDeletion,
            Clock clock) {
        this.store = Objects.requireNonNull(store);
        this.gateway = Objects.requireNonNull(gateway);
        this.tokens = Objects.requireNonNull(tokens);
        this.accountDeletion = Objects.requireNonNull(accountDeletion);
        this.clock = Objects.requireNonNull(clock);
    }

    public URI begin(String requestedReturnPath, boolean selectAccount) {
        var returnPath = validateReturnPath(requestedReturnPath);
        var state = tokens.randomToken();
        var now = clock.instant();
        store.saveOAuthAttempt(tokens.digest(state), returnPath, now, now.plus(OAUTH_STATE_LIFETIME));
        var verifier = tokens.codeVerifier(state);
        return gateway.authorizationUri(state, tokens.codeChallenge(verifier), selectAccount);
    }

    public LoginResult complete(String code, String state) {
        if (code == null || code.isBlank() || state == null || state.isBlank()) {
            throw new IdentityFailure(IdentityFailure.Code.INVALID_OAUTH_STATE, "The GitHub sign-in response is incomplete.");
        }
        var now = clock.instant();
        var stateDigest = tokens.digest(state);
        var returnPath = store.consumeOAuthAttempt(stateDigest, now)
                .orElseThrow(() -> new IdentityFailure(
                        IdentityFailure.Code.INVALID_OAUTH_STATE,
                        "The GitHub sign-in request is invalid, expired, or already used."));
        var identity = gateway.exchange(code, tokens.codeVerifier(state));
        var account = store.reconcile(identity, now);
        store.linkOAuthAttempt(stateDigest, account.id());
        store.revokeAllSessions(account.id(), now);
        var sessionToken = tokens.randomToken();
        var csrfToken = tokens.randomToken();
        store.saveSession(new UserSession(
                UUID.randomUUID(),
                account.id(),
                tokens.digest(sessionToken),
                tokens.digest(csrfToken),
                now,
                now.plus(SESSION_LIFETIME),
                null));
        return new LoginResult(account, sessionToken, csrfToken, returnPath);
    }

    public AuthenticatedSession authenticate(String sessionToken) {
        if (sessionToken == null || sessionToken.isBlank()) throw unauthenticated();
        var session = store.findActiveSession(tokens.digest(sessionToken), clock.instant())
                .orElseThrow(IdentityService::unauthenticated);
        var account = store.findAccount(session.accountId()).orElseThrow(IdentityService::unauthenticated);
        return new AuthenticatedSession(account, session);
    }

    public AuthenticatedSession authenticateMutation(String sessionToken, String csrfToken) {
        var authenticated = authenticate(sessionToken);
        if (csrfToken == null
                || csrfToken.isBlank()
                || !constantTimeEquals(authenticated.session().csrfDigest(), tokens.digest(csrfToken))) {
            throw new IdentityFailure(IdentityFailure.Code.INVALID_CSRF, "The request verification token is invalid.");
        }
        return authenticated;
    }

    public void logout(String sessionToken, String csrfToken) {
        authenticateMutation(sessionToken, csrfToken);
        store.revokeSession(tokens.digest(sessionToken), clock.instant());
    }

    public void deleteAccount(String sessionToken, String csrfToken) {
        var authenticated = authenticateMutation(sessionToken, csrfToken);
        if (!accountDeletion.delete(authenticated.account().id())) throw unauthenticated();
    }

    private static String validateReturnPath(String value) {
        var candidate = value == null || value.isBlank() ? "/" : value.strip();
        if (candidate.length() > 512
                || !candidate.startsWith("/")
                || candidate.startsWith("//")
                || candidate.contains("\\")
                || candidate.chars().anyMatch(Character::isISOControl)) {
            throw new IdentityFailure(
                    IdentityFailure.Code.INVALID_RETURN_PATH, "The sign-in return path must stay within CommitQuest.");
        }
        return candidate;
    }

    private static boolean constantTimeEquals(String left, String right) {
        return java.security.MessageDigest.isEqual(
                left.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                right.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }

    private static IdentityFailure unauthenticated() {
        return new IdentityFailure(IdentityFailure.Code.UNAUTHENTICATED, "A valid CommitQuest session is required.");
    }

    public record LoginResult(Account account, String sessionToken, String csrfToken, String returnPath) {}

    public record AuthenticatedSession(Account account, UserSession session) {}
}
