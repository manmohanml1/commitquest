package com.commitquest.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.commitquest.identity.domain.Account;
import com.commitquest.identity.domain.AccountId;
import com.commitquest.identity.domain.GitHubIdentity;
import com.commitquest.identity.domain.UserSession;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdentityServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-17T12:00:00Z");

    @Test
    void completesOneTimePkceLoginAndIssuesAnOpaqueSession() {
        var store = new InMemoryIdentityStore();
        var gateway = new FakeGateway();
        var tokens = new DeterministicTokens("state-token", "session-token", "csrf-token");
        var deletion = new FakeAccountDeletion();
        var service = service(store, gateway, tokens, deletion);

        var authorization = service.begin("/#campaign", false);
        var result = service.complete("temporary-code", "state-token");
        var authenticated = service.authenticateMutation("session-token", "csrf-token");

        assertThat(authorization.toString()).contains("state=state-token", "challenge=challenge:verifier:state-token");
        assertThat(gateway.exchangedCode).isEqualTo("temporary-code");
        assertThat(gateway.exchangedVerifier).isEqualTo("verifier:state-token");
        assertThat(result.returnPath()).isEqualTo("/#campaign");
        assertThat(result.account().githubLogin()).isEqualTo("octocat");
        assertThat(authenticated.account()).isEqualTo(result.account());
        assertThat(store.oauthAttempts.get("digest:state-token").accountId()).isEqualTo(result.account().id());
        assertThat(store.sessions.values()).singleElement().satisfies(session -> {
            assertThat(session.tokenDigest()).isEqualTo("digest:session-token");
            assertThat(session.csrfDigest()).isEqualTo("digest:csrf-token");
            assertThat(session.expiresAt()).isEqualTo(NOW.plus(IdentityService.SESSION_LIFETIME));
        });
        assertThatThrownBy(() -> service.complete("another-code", "state-token"))
                .isInstanceOf(IdentityFailure.class)
                .extracting(failure -> ((IdentityFailure) failure).code())
                .isEqualTo(IdentityFailure.Code.INVALID_OAUTH_STATE);
    }

    @Test
    void rejectsCrossOriginPathsCsrfMismatchAndExpiredSessions() {
        var store = new InMemoryIdentityStore();
        var tokens = new DeterministicTokens("state", "session", "csrf");
        var service = service(store, new FakeGateway(), tokens, new FakeAccountDeletion());

        assertThatThrownBy(() -> service.begin("https://attacker.example/return", false))
                .isInstanceOf(IdentityFailure.class)
                .extracting(failure -> ((IdentityFailure) failure).code())
                .isEqualTo(IdentityFailure.Code.INVALID_RETURN_PATH);
        service.begin("/", false);
        service.complete("code", "state");
        assertThatThrownBy(() -> service.authenticateMutation("session", "wrong"))
                .isInstanceOf(IdentityFailure.class)
                .extracting(failure -> ((IdentityFailure) failure).code())
                .isEqualTo(IdentityFailure.Code.INVALID_CSRF);

        var later = new IdentityService(
                store,
                new FakeGateway(),
                tokens,
                new FakeAccountDeletion(),
                Clock.fixed(NOW.plus(IdentityService.SESSION_LIFETIME), ZoneOffset.UTC));
        assertThatThrownBy(() -> later.authenticate("session"))
                .isInstanceOf(IdentityFailure.class)
                .extracting(failure -> ((IdentityFailure) failure).code())
                .isEqualTo(IdentityFailure.Code.UNAUTHENTICATED);
    }

    @Test
    void logoutRevokesTheSessionAndAccountDeletionUsesTheAuthenticatedOwner() {
        var store = new InMemoryIdentityStore();
        var tokens = new DeterministicTokens("state-one", "session-one", "csrf-one");
        var deletion = new FakeAccountDeletion();
        var service = service(store, new FakeGateway(), tokens, deletion);
        service.begin("/", false);
        var login = service.complete("code", "state-one");

        service.logout("session-one", "csrf-one");
        assertThatThrownBy(() -> service.authenticate("session-one")).isInstanceOf(IdentityFailure.class);

        var secondTokens = new DeterministicTokens("state-two", "session-two", "csrf-two");
        var secondService = service(store, new FakeGateway(), secondTokens, deletion);
        secondService.begin("/", false);
        secondService.complete("code", "state-two");
        secondService.deleteAccount("session-two", "csrf-two");

        assertThat(deletion.deletedAccount).isEqualTo(login.account().id());
    }

    private static IdentityService service(
            InMemoryIdentityStore store,
            FakeGateway gateway,
            TokenSecurity tokens,
            AccountDataDeletion deletion) {
        return new IdentityService(store, gateway, tokens, deletion, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static final class DeterministicTokens implements TokenSecurity {
        private final ArrayDeque<String> values;

        private DeterministicTokens(String... values) {
            this.values = new ArrayDeque<>(java.util.List.of(values));
        }

        @Override
        public String randomToken() {
            return values.removeFirst();
        }

        @Override
        public String digest(String token) {
            return "digest:" + token;
        }

        @Override
        public String codeVerifier(String state) {
            return "verifier:" + state;
        }

        @Override
        public String codeChallenge(String codeVerifier) {
            return "challenge:" + codeVerifier;
        }
    }

    private static final class FakeGateway implements OAuthIdentityGateway {
        private String exchangedCode;
        private String exchangedVerifier;

        @Override
        public URI authorizationUri(String state, String codeChallenge, boolean selectAccount) {
            return URI.create("https://github.test/authorize?state=" + state + "&challenge=" + codeChallenge);
        }

        @Override
        public GitHubIdentity exchange(String code, String codeVerifier) {
            exchangedCode = code;
            exchangedVerifier = codeVerifier;
            return new GitHubIdentity(1, "octocat", "The Octocat", "https://avatars.example/octocat");
        }
    }

    private static final class FakeAccountDeletion implements AccountDataDeletion {
        private AccountId deletedAccount;

        @Override
        public boolean delete(AccountId accountId) {
            deletedAccount = accountId;
            return true;
        }
    }

    private static final class InMemoryIdentityStore implements IdentityStore {
        private final Map<String, OAuthAttempt> oauthAttempts = new LinkedHashMap<>();
        private final Map<AccountId, Account> accounts = new LinkedHashMap<>();
        private final Map<String, UserSession> sessions = new LinkedHashMap<>();

        @Override
        public void saveOAuthAttempt(String stateDigest, String returnPath, Instant createdAt, Instant expiresAt) {
            oauthAttempts.put(stateDigest, new OAuthAttempt(returnPath, expiresAt, false, null));
        }

        @Override
        public Optional<String> consumeOAuthAttempt(String stateDigest, Instant now) {
            var attempt = oauthAttempts.get(stateDigest);
            if (attempt == null || attempt.consumed() || !attempt.expiresAt().isAfter(now)) return Optional.empty();
            oauthAttempts.put(stateDigest, new OAuthAttempt(attempt.returnPath(), attempt.expiresAt(), true, attempt.accountId()));
            return Optional.of(attempt.returnPath());
        }

        @Override
        public void linkOAuthAttempt(String stateDigest, AccountId accountId) {
            var attempt = oauthAttempts.get(stateDigest);
            oauthAttempts.put(stateDigest, new OAuthAttempt(attempt.returnPath(), attempt.expiresAt(), true, accountId));
        }

        @Override
        public Account reconcile(GitHubIdentity identity, Instant now) {
            var existing = accounts.values().stream()
                    .filter(account -> account.githubUserId() == identity.userId())
                    .findFirst();
            var account = new Account(
                    existing.map(Account::id).orElseGet(() -> new AccountId(new UUID(0, 1))),
                    identity.userId(),
                    identity.login(),
                    identity.displayName(),
                    identity.avatarUrl(),
                    existing.map(Account::createdAt).orElse(now),
                    now);
            accounts.put(account.id(), account);
            return account;
        }

        @Override
        public Optional<Account> findAccount(AccountId accountId) {
            return Optional.ofNullable(accounts.get(accountId));
        }

        @Override
        public void saveSession(UserSession session) {
            sessions.put(session.tokenDigest(), session);
        }

        @Override
        public Optional<UserSession> findActiveSession(String tokenDigest, Instant now) {
            return Optional.ofNullable(sessions.get(tokenDigest)).filter(session -> session.activeAt(now));
        }

        @Override
        public void revokeSession(String tokenDigest, Instant now) {
            var session = sessions.get(tokenDigest);
            if (session != null) sessions.put(tokenDigest, revoked(session, now));
        }

        @Override
        public void revokeAllSessions(AccountId accountId, Instant now) {
            sessions.replaceAll((digest, session) -> session.accountId().equals(accountId) ? revoked(session, now) : session);
        }

        private static UserSession revoked(UserSession session, Instant now) {
            return new UserSession(
                    session.id(),
                    session.accountId(),
                    session.tokenDigest(),
                    session.csrfDigest(),
                    session.createdAt(),
                    session.expiresAt(),
                    now);
        }
    }

    private record OAuthAttempt(String returnPath, Instant expiresAt, boolean consumed, AccountId accountId) {}
}
