package com.commitquest.identity.provider.postgresql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import com.commitquest.identity.application.IdentityStore;
import com.commitquest.identity.domain.Account;
import com.commitquest.identity.domain.AccountId;
import com.commitquest.identity.domain.GitHubIdentity;
import com.commitquest.identity.domain.UserSession;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

public final class JooqIdentityStore implements IdentityStore {

    private static final Table<?> ACCOUNT = table(name("cq_account"));
    private static final Field<UUID> ACCOUNT_ID = field(name("id"), UUID.class);
    private static final Field<Long> GITHUB_USER_ID = field(name("github_user_id"), Long.class);
    private static final Field<String> GITHUB_LOGIN = field(name("github_login"), String.class);
    private static final Field<String> DISPLAY_NAME = field(name("display_name"), String.class);
    private static final Field<String> AVATAR_URL = field(name("avatar_url"), String.class);
    private static final Field<Instant> ACCOUNT_CREATED_AT = field(name("created_at"), Instant.class);
    private static final Field<Instant> ACCOUNT_UPDATED_AT = field(name("updated_at"), Instant.class);

    private static final Table<?> OAUTH_STATE = table(name("cq_oauth_state"));
    private static final Field<UUID> OAUTH_STATE_ID = field(name("id"), UUID.class);
    private static final Field<UUID> OAUTH_ACCOUNT_ID = field(name("account_id"), UUID.class);
    private static final Field<String> STATE_DIGEST = field(name("state_digest"), String.class);
    private static final Field<String> RETURN_PATH = field(name("return_path"), String.class);
    private static final Field<Instant> OAUTH_CREATED_AT = field(name("created_at"), Instant.class);
    private static final Field<Instant> OAUTH_EXPIRES_AT = field(name("expires_at"), Instant.class);
    private static final Field<Instant> CONSUMED_AT = field(name("consumed_at"), Instant.class);

    private static final Table<?> SESSION = table(name("cq_user_session"));
    private static final Field<UUID> SESSION_ID = field(name("id"), UUID.class);
    private static final Field<UUID> SESSION_ACCOUNT_ID = field(name("account_id"), UUID.class);
    private static final Field<String> TOKEN_DIGEST = field(name("token_digest"), String.class);
    private static final Field<String> CSRF_DIGEST = field(name("csrf_digest"), String.class);
    private static final Field<Instant> SESSION_CREATED_AT = field(name("created_at"), Instant.class);
    private static final Field<Instant> SESSION_EXPIRES_AT = field(name("expires_at"), Instant.class);
    private static final Field<Instant> REVOKED_AT = field(name("revoked_at"), Instant.class);

    private final DSLContext dsl;

    public JooqIdentityStore(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void saveOAuthAttempt(String stateDigest, String returnPath, Instant createdAt, Instant expiresAt) {
        dsl.deleteFrom(OAUTH_STATE).where(OAUTH_EXPIRES_AT.le(createdAt)).execute();
        dsl.insertInto(OAUTH_STATE)
                .set(OAUTH_STATE_ID, UUID.randomUUID())
                .set(STATE_DIGEST, stateDigest)
                .set(RETURN_PATH, returnPath)
                .set(OAUTH_CREATED_AT, createdAt)
                .set(OAUTH_EXPIRES_AT, expiresAt)
                .execute();
    }

    @Override
    public Optional<String> consumeOAuthAttempt(String stateDigest, Instant now) {
        return dsl.update(OAUTH_STATE)
                .set(CONSUMED_AT, now)
                .where(STATE_DIGEST.eq(stateDigest))
                .and(CONSUMED_AT.isNull())
                .and(OAUTH_EXPIRES_AT.gt(now))
                .returningResult(RETURN_PATH)
                .fetchOptional(RETURN_PATH);
    }

    @Override
    public void linkOAuthAttempt(String stateDigest, AccountId accountId) {
        dsl.update(OAUTH_STATE)
                .set(OAUTH_ACCOUNT_ID, accountId.value())
                .where(STATE_DIGEST.eq(stateDigest))
                .and(CONSUMED_AT.isNotNull())
                .execute();
    }

    @Override
    public Account reconcile(GitHubIdentity identity, Instant now) {
        return dsl.insertInto(ACCOUNT)
                .set(ACCOUNT_ID, UUID.randomUUID())
                .set(GITHUB_USER_ID, identity.userId())
                .set(GITHUB_LOGIN, identity.login())
                .set(DISPLAY_NAME, identity.displayName())
                .set(AVATAR_URL, identity.avatarUrl())
                .set(ACCOUNT_CREATED_AT, now)
                .set(ACCOUNT_UPDATED_AT, now)
                .onConflict(GITHUB_USER_ID)
                .doUpdate()
                .set(GITHUB_LOGIN, identity.login())
                .set(DISPLAY_NAME, identity.displayName())
                .set(AVATAR_URL, identity.avatarUrl())
                .set(ACCOUNT_UPDATED_AT, now)
                .returning(accountFields())
                .fetchSingle(this::mapAccount);
    }

    @Override
    public Optional<Account> findAccount(AccountId accountId) {
        return dsl.select(accountFields())
                .from(ACCOUNT)
                .where(ACCOUNT_ID.eq(accountId.value()))
                .fetchOptional(this::mapAccount);
    }

    @Override
    public void saveSession(UserSession session) {
        dsl.deleteFrom(SESSION).where(SESSION_EXPIRES_AT.le(session.createdAt())).execute();
        dsl.insertInto(SESSION)
                .set(SESSION_ID, session.id())
                .set(SESSION_ACCOUNT_ID, session.accountId().value())
                .set(TOKEN_DIGEST, session.tokenDigest())
                .set(CSRF_DIGEST, session.csrfDigest())
                .set(SESSION_CREATED_AT, session.createdAt())
                .set(SESSION_EXPIRES_AT, session.expiresAt())
                .set(REVOKED_AT, session.revokedAt())
                .execute();
    }

    @Override
    public Optional<UserSession> findActiveSession(String tokenDigest, Instant now) {
        return dsl.select(sessionFields())
                .from(SESSION)
                .where(TOKEN_DIGEST.eq(tokenDigest))
                .and(REVOKED_AT.isNull())
                .and(SESSION_EXPIRES_AT.gt(now))
                .fetchOptional(this::mapSession);
    }

    @Override
    public void revokeSession(String tokenDigest, Instant now) {
        dsl.update(SESSION)
                .set(REVOKED_AT, now)
                .where(TOKEN_DIGEST.eq(tokenDigest))
                .and(REVOKED_AT.isNull())
                .execute();
    }

    @Override
    public void revokeAllSessions(AccountId accountId, Instant now) {
        dsl.update(SESSION)
                .set(REVOKED_AT, now)
                .where(SESSION_ACCOUNT_ID.eq(accountId.value()))
                .and(REVOKED_AT.isNull())
                .execute();
    }

    private Field<?>[] accountFields() {
        return new Field<?>[] {
            ACCOUNT_ID, GITHUB_USER_ID, GITHUB_LOGIN, DISPLAY_NAME, AVATAR_URL, ACCOUNT_CREATED_AT, ACCOUNT_UPDATED_AT
        };
    }

    private Field<?>[] sessionFields() {
        return new Field<?>[] {
            SESSION_ID,
            SESSION_ACCOUNT_ID,
            TOKEN_DIGEST,
            CSRF_DIGEST,
            SESSION_CREATED_AT,
            SESSION_EXPIRES_AT,
            REVOKED_AT
        };
    }

    private Account mapAccount(Record record) {
        return new Account(
                new AccountId(record.get(ACCOUNT_ID)),
                record.get(GITHUB_USER_ID),
                record.get(GITHUB_LOGIN),
                record.get(DISPLAY_NAME),
                record.get(AVATAR_URL),
                record.get(ACCOUNT_CREATED_AT),
                record.get(ACCOUNT_UPDATED_AT));
    }

    private UserSession mapSession(Record record) {
        return new UserSession(
                record.get(SESSION_ID),
                new AccountId(record.get(SESSION_ACCOUNT_ID)),
                record.get(TOKEN_DIGEST),
                record.get(CSRF_DIGEST),
                record.get(SESSION_CREATED_AT),
                record.get(SESSION_EXPIRES_AT),
                record.get(REVOKED_AT));
    }
}
