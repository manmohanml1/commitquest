package com.commitquest.campaign.provider.postgresql;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import com.commitquest.campaign.application.SavedCampaignExport;
import com.commitquest.campaign.application.SavedCampaignLimitExceeded;
import com.commitquest.campaign.application.SavedCampaignService;
import com.commitquest.campaign.application.SavedCampaignStore;
import com.commitquest.campaign.domain.CampaignVisibility;
import com.commitquest.campaign.domain.SavedCampaign;
import com.commitquest.campaign.domain.SavedCampaignId;
import com.commitquest.identity.domain.AccountId;
import com.commitquest.preview.domain.CampaignProjection;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public final class JooqSavedCampaignStore implements SavedCampaignStore {

    private static final Table<?> CAMPAIGN = table(name("cq_saved_campaign"));
    private static final Table<?> ACCOUNT = table(name("cq_account"));
    private static final Field<UUID> ID = field(name("id"), UUID.class);
    private static final Field<UUID> ACCOUNT_PRIMARY_ID = field(name("id"), UUID.class);
    private static final Field<UUID> ACCOUNT_ID = field(name("account_id"), UUID.class);
    private static final Field<String> REPOSITORY_OWNER = field(name("repository_owner"), String.class);
    private static final Field<String> REPOSITORY_NAME = field(name("repository_name"), String.class);
    private static final Field<JSONB> PROJECTION = field(name("projection"), SQLDataType.JSONB);
    private static final Field<String> VISIBILITY = field(name("visibility"), String.class);
    private static final Field<Integer> PROJECTION_SCHEMA_VERSION = field(name("projection_schema_version"), Integer.class);
    private static final Field<Integer> MAPPING_ALGORITHM_VERSION = field(name("mapping_algorithm_version"), Integer.class);
    private static final Field<Integer> SCORING_RULESET_VERSION = field(name("scoring_ruleset_version"), Integer.class);
    private static final Field<Integer> EXPORT_SCHEMA_VERSION = field(name("export_schema_version"), Integer.class);
    private static final Field<Instant> CREATED_AT = field(name("created_at"), Instant.class);
    private static final Field<Instant> UPDATED_AT = field(name("updated_at"), Instant.class);

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    public JooqSavedCampaignStore(DSLContext dsl, ObjectMapper objectMapper) {
        this.dsl = Objects.requireNonNull(dsl);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public Optional<SavedCampaign> findByOwnerAndRepository(AccountId ownerId, String repository) {
        var identity = RepositoryIdentity.parse(repository);
        return dsl.select(campaignFields())
                .from(CAMPAIGN)
                .where(ACCOUNT_ID.eq(ownerId.value()))
                .and(REPOSITORY_OWNER.eq(identity.owner()))
                .and(REPOSITORY_NAME.eq(identity.name()))
                .fetchOptional(this::map);
    }

    @Override
    public Optional<SavedCampaign> findByIdAndOwner(SavedCampaignId campaignId, AccountId ownerId) {
        return dsl.select(campaignFields())
                .from(CAMPAIGN)
                .where(ID.eq(campaignId.value()))
                .and(ACCOUNT_ID.eq(ownerId.value()))
                .fetchOptional(this::map);
    }

    @Override
    public List<SavedCampaign> findAllByOwner(AccountId ownerId) {
        return dsl.select(campaignFields())
                .from(CAMPAIGN)
                .where(ACCOUNT_ID.eq(ownerId.value()))
                .orderBy(UPDATED_AT.desc(), ID.asc())
                .fetch(this::map);
    }

    @Override
    public long countByOwner(AccountId ownerId) {
        return dsl.fetchCount(CAMPAIGN, ACCOUNT_ID.eq(ownerId.value()));
    }

    @Override
    public SavedCampaign save(SavedCampaign campaign) {
        var identity = RepositoryIdentity.parse(campaign.projection().repository());
        var projection = JSONB.valueOf(writeProjection(campaign.projection()));
        dsl.transaction(configuration -> {
            var transaction = DSL.using(configuration);
            lockOwner(transaction, campaign.ownerId());
            enforceLimitForNewCampaign(transaction, campaign);
            persist(transaction, campaign, identity, projection);
        });
        return campaign;
    }

    private void lockOwner(DSLContext transaction, AccountId ownerId) {
        var ownerExists = transaction.select(ACCOUNT_PRIMARY_ID)
                .from(ACCOUNT)
                .where(ACCOUNT_PRIMARY_ID.eq(ownerId.value()))
                .forUpdate()
                .fetchOptional()
                .isPresent();
        if (!ownerExists) throw new IllegalStateException("The campaign owner does not exist.");
    }

    private void enforceLimitForNewCampaign(DSLContext transaction, SavedCampaign campaign) {
        var alreadyPersisted = transaction.fetchExists(
                CAMPAIGN,
                ID.eq(campaign.id().value()).and(ACCOUNT_ID.eq(campaign.ownerId().value())));
        if (!alreadyPersisted
                && transaction.fetchCount(CAMPAIGN, ACCOUNT_ID.eq(campaign.ownerId().value()))
                        >= SavedCampaignService.MAX_SAVED_CAMPAIGNS) {
            throw new SavedCampaignLimitExceeded(SavedCampaignService.MAX_SAVED_CAMPAIGNS);
        }
    }

    private void persist(
            DSLContext transaction, SavedCampaign campaign, RepositoryIdentity identity, JSONB projection) {
        var affectedRows = transaction.insertInto(CAMPAIGN)
                .set(ID, campaign.id().value())
                .set(ACCOUNT_ID, campaign.ownerId().value())
                .set(REPOSITORY_OWNER, identity.owner())
                .set(REPOSITORY_NAME, identity.name())
                .set(PROJECTION, projection)
                .set(VISIBILITY, campaign.visibility().name().toLowerCase(Locale.ROOT))
                .set(PROJECTION_SCHEMA_VERSION, campaign.projection().schemaVersion())
                .set(MAPPING_ALGORITHM_VERSION, campaign.projection().mappingAlgorithmVersion())
                .set(SCORING_RULESET_VERSION, campaign.projection().scoringRulesetVersion())
                .set(EXPORT_SCHEMA_VERSION, SavedCampaignExport.CURRENT_SCHEMA_VERSION)
                .set(CREATED_AT, campaign.createdAt())
                .set(UPDATED_AT, campaign.updatedAt())
                .onConflict(ID)
                .doUpdate()
                .set(PROJECTION, projection)
                .set(VISIBILITY, campaign.visibility().name().toLowerCase(Locale.ROOT))
                .set(PROJECTION_SCHEMA_VERSION, campaign.projection().schemaVersion())
                .set(MAPPING_ALGORITHM_VERSION, campaign.projection().mappingAlgorithmVersion())
                .set(SCORING_RULESET_VERSION, campaign.projection().scoringRulesetVersion())
                .set(EXPORT_SCHEMA_VERSION, SavedCampaignExport.CURRENT_SCHEMA_VERSION)
                .set(UPDATED_AT, campaign.updatedAt())
                .where(ACCOUNT_ID.eq(campaign.ownerId().value()))
                .and(REPOSITORY_OWNER.eq(identity.owner()))
                .and(REPOSITORY_NAME.eq(identity.name()))
                .execute();
        if (affectedRows != 1) {
            throw new IllegalStateException("The saved campaign identity or owner did not match persisted data.");
        }
    }

    @Override
    public void delete(SavedCampaign campaign) {
        dsl.deleteFrom(CAMPAIGN)
                .where(ID.eq(campaign.id().value()))
                .and(ACCOUNT_ID.eq(campaign.ownerId().value()))
                .execute();
    }

    @Override
    public int deleteAllByOwner(AccountId ownerId) {
        return dsl.deleteFrom(CAMPAIGN)
                .where(ACCOUNT_ID.eq(ownerId.value()))
                .execute();
    }

    private Field<?>[] campaignFields() {
        return new Field<?>[] {ID, ACCOUNT_ID, PROJECTION, VISIBILITY, CREATED_AT, UPDATED_AT};
    }

    private SavedCampaign map(Record record) {
        return new SavedCampaign(
                new SavedCampaignId(record.get(ID)),
                new AccountId(record.get(ACCOUNT_ID)),
                readProjection(record.get(PROJECTION)),
                CampaignVisibility.valueOf(record.get(VISIBILITY).toUpperCase(Locale.ROOT)),
                record.get(CREATED_AT),
                record.get(UPDATED_AT));
    }

    private String writeProjection(CampaignProjection projection) {
        try {
            return objectMapper.writeValueAsString(projection);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize the campaign projection.", exception);
        }
    }

    private CampaignProjection readProjection(JSONB projection) {
        try {
            return objectMapper.readValue(projection.data(), CampaignProjection.class);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not deserialize the campaign projection.", exception);
        }
    }

    private record RepositoryIdentity(String owner, String name) {

        private static RepositoryIdentity parse(String repository) {
            Objects.requireNonNull(repository, "Repository is required.");
            var parts = repository.strip().split("/", -1);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                throw new IllegalArgumentException("Repository must use the owner/name form.");
            }
            return new RepositoryIdentity(
                    parts[0].toLowerCase(Locale.ROOT), parts[1].toLowerCase(Locale.ROOT));
        }
    }
}
