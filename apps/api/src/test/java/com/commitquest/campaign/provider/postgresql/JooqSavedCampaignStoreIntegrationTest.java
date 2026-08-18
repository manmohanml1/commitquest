package com.commitquest.campaign.provider.postgresql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import com.commitquest.campaign.application.SavedCampaignExport;
import com.commitquest.campaign.application.SavedCampaignLimitExceeded;
import com.commitquest.campaign.application.SavedCampaignService;
import com.commitquest.campaign.domain.CampaignVisibility;
import com.commitquest.campaign.domain.SavedCampaign;
import com.commitquest.campaign.domain.SavedCampaignId;
import com.commitquest.identity.domain.AccountId;
import com.commitquest.identity.domain.GitHubIdentity;
import com.commitquest.identity.domain.UserSession;
import com.commitquest.identity.provider.postgresql.JooqAccountDataDeletion;
import com.commitquest.identity.provider.postgresql.JooqIdentityStore;
import com.commitquest.preview.domain.CampaignProjection;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

@Testcontainers(disabledWithoutDocker = true)
class JooqSavedCampaignStoreIntegrationTest {

    private static final AccountId OWNER = account(1);
    private static final AccountId OTHER_OWNER = account(2);
    private static final Instant CREATED_AT = Instant.parse("2026-08-17T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-08-17T11:00:00Z");

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    private static HikariDataSource dataSource;
    private static DSLContext dsl;
    private static JooqSavedCampaignStore store;
    private static JooqAccountDataDeletion accountDataDeletion;
    private static JooqIdentityStore identityStore;

    @BeforeAll
    static void migrateDatabase() {
        var hikari = new HikariConfig();
        hikari.setJdbcUrl(POSTGRES.getJdbcUrl());
        hikari.setUsername(POSTGRES.getUsername());
        hikari.setPassword(POSTGRES.getPassword());
        hikari.setMaximumPoolSize(2);
        dataSource = new HikariDataSource(hikari);

        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
        dsl = DSL.using(dataSource, SQLDialect.POSTGRES);
        store = new JooqSavedCampaignStore(dsl, new ObjectMapper());
        accountDataDeletion = new JooqAccountDataDeletion(dsl);
        identityStore = new JooqIdentityStore(dsl);
    }

    @AfterAll
    static void closeDataSource() {
        if (dataSource != null) dataSource.close();
    }

    @BeforeEach
    void resetAccounts() {
        dsl.execute("truncate table cq_account cascade");
        insertAccount(OWNER, 101L, "owner");
        insertAccount(OTHER_OWNER, 202L, "other-owner");
    }

    @Test
    void migrationCreatesTheCompleteV04OwnershipSchema() {
        assertThat(tableExists("cq_account")).isTrue();
        assertThat(tableExists("cq_oauth_state")).isTrue();
        assertThat(tableExists("cq_user_session")).isTrue();
        assertThat(tableExists("cq_saved_campaign")).isTrue();
        assertThat(dsl.fetchValue("select version from flyway_schema_history where success order by installed_rank desc limit 1"))
                .isEqualTo("1");
    }

    @Test
    void roundTripsVersionedJsonAndKeepsEveryLookupOwnerScoped() {
        var campaign = campaign(11, OWNER, "Owner/Repository", "Original", CREATED_AT, CREATED_AT);
        store.save(campaign);

        assertThat(store.findByIdAndOwner(campaign.id(), OTHER_OWNER)).isEmpty();
        assertThat(store.findByOwnerAndRepository(OTHER_OWNER, "owner/repository")).isEmpty();
        assertThat(store.findByOwnerAndRepository(OWNER, "OWNER/REPOSITORY"))
                .contains(campaign);

        var refreshed = campaign(11, OWNER, "owner/repository", "Refreshed", CREATED_AT, UPDATED_AT)
                .changeVisibility(CampaignVisibility.UNLISTED, UPDATED_AT);
        store.save(refreshed);

        assertThat(store.countByOwner(OWNER)).isEqualTo(1);
        assertThat(store.findAllByOwner(OWNER)).containsExactly(refreshed);
        assertThat(dsl.fetchValue(
                        "select mapping_algorithm_version from cq_saved_campaign where id = ?",
                        campaign.id().value()))
                .isEqualTo(CampaignProjection.MAPPING_ALGORITHM_VERSION);
        assertThat(dsl.fetchValue(
                        "select export_schema_version from cq_saved_campaign where id = ?",
                        campaign.id().value()))
                .isEqualTo(SavedCampaignExport.CURRENT_SCHEMA_VERSION);
    }

    @Test
    void campaignAndAccountDeletionCannotCrossOwnerBoundaries() {
        var ownerCampaign = campaign(21, OWNER, "owner/one", "One", CREATED_AT, CREATED_AT);
        var otherCampaign = campaign(22, OTHER_OWNER, "other/two", "Two", CREATED_AT, CREATED_AT);
        store.save(ownerCampaign);
        store.save(otherCampaign);

        store.delete(ownerCampaign);
        assertThat(store.findAllByOwner(OWNER)).isEmpty();
        assertThat(store.findAllByOwner(OTHER_OWNER)).containsExactly(otherCampaign);

        insertSessionAndOauthState(OTHER_OWNER);
        assertThat(accountDataDeletion.delete(OTHER_OWNER)).isTrue();
        assertThat(store.findAllByOwner(OTHER_OWNER)).isEmpty();
        assertThat(dsl.fetchCount(table(name("cq_user_session")))).isZero();
        assertThat(dsl.fetchCount(table(name("cq_oauth_state")))).isZero();
        assertThat(accountDataDeletion.delete(OTHER_OWNER)).isFalse();
    }

    @Test
    void serializesOwnerWritesAndEnforcesTheAccountLimitInPostgresql() {
        for (var index = 0; index < SavedCampaignService.MAX_SAVED_CAMPAIGNS; index++) {
            store.save(campaign(
                    100 + index,
                    OWNER,
                    "owner/repository-" + index,
                    "Campaign " + index,
                    CREATED_AT,
                    CREATED_AT));
        }

        assertThatThrownBy(() -> store.save(campaign(
                        999,
                        OWNER,
                        "owner/over-limit",
                        "Over limit",
                        CREATED_AT,
                        CREATED_AT)))
                .isInstanceOf(SavedCampaignLimitExceeded.class);
        assertThat(store.countByOwner(OWNER)).isEqualTo(SavedCampaignService.MAX_SAVED_CAMPAIGNS);
    }

    @Test
    void consumesOAuthStateOnceAndPersistsOnlySessionDigests() {
        identityStore.saveOAuthAttempt("d".repeat(64), "/#campaign", CREATED_AT, UPDATED_AT);
        assertThat(identityStore.consumeOAuthAttempt("d".repeat(64), CREATED_AT.plusSeconds(1)))
                .contains("/#campaign");
        assertThat(identityStore.consumeOAuthAttempt("d".repeat(64), CREATED_AT.plusSeconds(2)))
                .isEmpty();

        var account = identityStore.reconcile(
                new GitHubIdentity(303, "octocat", "The Octocat", "https://avatars.example/octocat"),
                CREATED_AT);
        identityStore.linkOAuthAttempt("d".repeat(64), account.id());
        var updated = identityStore.reconcile(
                new GitHubIdentity(303, "octocat-renamed", "Octocat", "https://avatars.example/new"),
                UPDATED_AT);
        assertThat(updated.id()).isEqualTo(account.id());
        assertThat(updated.createdAt()).isEqualTo(CREATED_AT);

        var session = new UserSession(
                new UUID(0, 404),
                account.id(),
                "e".repeat(64),
                "f".repeat(64),
                CREATED_AT,
                UPDATED_AT,
                null);
        identityStore.saveSession(session);
        assertThat(identityStore.findActiveSession("e".repeat(64), CREATED_AT.plusSeconds(1)))
                .contains(session);
        identityStore.revokeAllSessions(account.id(), CREATED_AT.plusSeconds(2));
        assertThat(identityStore.findActiveSession("e".repeat(64), CREATED_AT.plusSeconds(3)))
                .isEmpty();

        assertThat(accountDataDeletion.delete(account.id())).isTrue();
        assertThat(dsl.fetchCount(table(name("cq_oauth_state")))).isZero();
        assertThat(dsl.fetchCount(table(name("cq_user_session")))).isZero();
    }

    private boolean tableExists(String tableName) {
        return Boolean.TRUE.equals(dsl.fetchValue(
                "select exists (select 1 from information_schema.tables where table_schema = 'public' and table_name = ?)",
                tableName));
    }

    private void insertAccount(AccountId id, long githubUserId, String login) {
        dsl.execute(
                "insert into cq_account (id, github_user_id, github_login, created_at, updated_at) values (?, ?, ?, ?, ?)",
                id.value(), githubUserId, login, CREATED_AT, CREATED_AT);
    }

    private void insertSessionAndOauthState(AccountId ownerId) {
        dsl.execute(
                "insert into cq_user_session "
                        + "(id, account_id, token_digest, csrf_digest, created_at, expires_at) "
                        + "values (?, ?, ?, ?, ?, ?)",
                new UUID(0, 301),
                ownerId.value(),
                "a".repeat(64),
                "b".repeat(64),
                CREATED_AT,
                UPDATED_AT);
        dsl.execute(
                "insert into cq_oauth_state "
                        + "(id, account_id, state_digest, return_path, created_at, expires_at) "
                        + "values (?, ?, ?, ?, ?, ?)",
                new UUID(0, 302),
                ownerId.value(),
                "c".repeat(64),
                "/campaigns",
                CREATED_AT,
                UPDATED_AT);
    }

    private static SavedCampaign campaign(
            long id,
            AccountId ownerId,
            String repository,
            String title,
            Instant createdAt,
            Instant updatedAt) {
        return new SavedCampaign(
                new SavedCampaignId(new UUID(0, id)),
                ownerId,
                projection(repository, title),
                CampaignVisibility.PRIVATE,
                createdAt,
                updatedAt);
    }

    private static CampaignProjection projection(String repository, String title) {
        return new CampaignProjection(
                CampaignProjection.SCHEMA_VERSION,
                CampaignProjection.MAPPING_ALGORITHM_VERSION,
                CampaignProjection.SCORING_RULESET_VERSION,
                repository.replace('/', '-').toLowerCase(),
                title,
                repository,
                "foundation",
                "main",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    private static AccountId account(long value) {
        return new AccountId(new UUID(0, value));
    }
}
