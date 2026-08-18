package com.commitquest.campaign.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.commitquest.campaign.domain.CampaignVisibility;
import com.commitquest.campaign.domain.SavedCampaign;
import com.commitquest.campaign.domain.SavedCampaignId;
import com.commitquest.identity.domain.AccountId;
import com.commitquest.preview.domain.CampaignProjection;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class SavedCampaignServiceTest {

    private static final AccountId OWNER = account(1);
    private static final AccountId OTHER_OWNER = account(2);
    private static final Instant CREATED_AT = Instant.parse("2026-08-17T10:00:00Z");
    private static final Instant REFRESHED_AT = Instant.parse("2026-08-17T11:00:00Z");

    @Test
    void savesPrivatelyAndRefreshesTheSameOwnerRepositoryInPlace() {
        var store = new InMemorySavedCampaignStore();
        var source = new MutableProjectionSource(projection("owner/repository", "First title"));
        var firstService = service(store, source, CREATED_AT);

        var created = firstService.save(OWNER, "https://github.com/owner/repository");
        source.projection = projection("owner/repository", "Refreshed title");
        var refreshed = service(store, source, REFRESHED_AT)
                .save(OWNER, "https://github.com/owner/repository");

        assertThat(created.visibility()).isEqualTo(CampaignVisibility.PRIVATE);
        assertThat(refreshed.id()).isEqualTo(created.id());
        assertThat(refreshed.createdAt()).isEqualTo(CREATED_AT);
        assertThat(refreshed.updatedAt()).isEqualTo(REFRESHED_AT);
        assertThat(refreshed.projection().title()).isEqualTo("Refreshed title");
        assertThat(store.findAllByOwner(OWNER)).hasSize(1);
    }

    @Test
    void treatsCrossOwnerAndMissingCampaignsAsTheSameNotFoundResult() {
        var store = new InMemorySavedCampaignStore();
        var service = service(store, new MutableProjectionSource(projection("owner/repository", "Title")), CREATED_AT);
        var saved = service.save(OWNER, "https://github.com/owner/repository");

        assertThatThrownBy(() -> service.get(OTHER_OWNER, saved.id()))
                .isInstanceOf(SavedCampaignNotFound.class)
                .hasMessage("The saved campaign was not found.");
        assertThatThrownBy(() -> service.get(OWNER, campaignId(999)))
                .isInstanceOf(SavedCampaignNotFound.class)
                .hasMessage("The saved campaign was not found.");
    }

    @Test
    void enforcesThePerOwnerLimitWithoutBlockingAnotherOwner() {
        var store = new InMemorySavedCampaignStore();
        var source = new MutableProjectionSource(projection("owner/repository-0", "Title"));
        var service = service(store, source, CREATED_AT);

        for (var index = 0; index < SavedCampaignService.MAX_SAVED_CAMPAIGNS; index++) {
            source.projection = projection("owner/repository-" + index, "Title " + index);
            service.save(OWNER, "https://github.com/owner/repository-" + index);
        }
        source.projection = projection("owner/over-limit", "Over limit");

        assertThatThrownBy(() -> service.save(OWNER, "https://github.com/owner/over-limit"))
                .isInstanceOf(SavedCampaignLimitExceeded.class)
                .hasMessageContaining("25");

        var otherOwnerCampaign = service.save(OTHER_OWNER, "https://github.com/owner/over-limit");
        assertThat(otherOwnerCampaign.ownerId()).isEqualTo(OTHER_OWNER);
    }

    @Test
    void changesVisibilityAndExportsTheVersionedProjection() {
        var store = new InMemorySavedCampaignStore();
        var service = service(store, new MutableProjectionSource(projection("owner/repository", "Title")), CREATED_AT);
        var saved = service.save(OWNER, "https://github.com/owner/repository");

        var changed = service.changeVisibility(OWNER, saved.id(), CampaignVisibility.UNLISTED);
        var exported = service.export(OWNER, saved.id());

        assertThat(changed.visibility()).isEqualTo(CampaignVisibility.UNLISTED);
        assertThat(exported.exportSchemaVersion()).isEqualTo(SavedCampaignExport.CURRENT_SCHEMA_VERSION);
        assertThat(exported.campaignId()).isEqualTo(saved.id().value());
        assertThat(exported.repository()).isEqualTo("owner/repository");
        assertThat(exported.visibility()).isEqualTo("unlisted");
        assertThat(exported.projectionSchemaVersion()).isEqualTo(CampaignProjection.SCHEMA_VERSION);
        assertThat(exported.mappingAlgorithmVersion()).isEqualTo(CampaignProjection.MAPPING_ALGORITHM_VERSION);
        assertThat(exported.scoringRulesetVersion()).isEqualTo(CampaignProjection.SCORING_RULESET_VERSION);
    }

    @Test
    void deletesOnlyCampaignsOwnedByTheRequestedAccount() {
        var store = new InMemorySavedCampaignStore();
        var source = new MutableProjectionSource(projection("owner/first", "First"));
        var service = service(store, source, CREATED_AT);
        var first = service.save(OWNER, "https://github.com/owner/first");
        source.projection = projection("owner/second", "Second");
        service.save(OWNER, "https://github.com/owner/second");
        source.projection = projection("other/only", "Other");
        service.save(OTHER_OWNER, "https://github.com/other/only");

        service.delete(OWNER, first.id());
        assertThat(service.list(OWNER)).hasSize(1);
        assertThat(service.deleteAllForOwner(OWNER)).isEqualTo(1);
        assertThat(service.list(OWNER)).isEmpty();
        assertThat(service.list(OTHER_OWNER)).hasSize(1);
    }

    private static SavedCampaignService service(
            InMemorySavedCampaignStore store, MutableProjectionSource source, Instant now) {
        var sequence = new AtomicLong(store.campaigns.size() + 1L);
        return new SavedCampaignService(
                store,
                source,
                () -> campaignId(sequence.getAndIncrement()),
                Clock.fixed(now, ZoneOffset.UTC));
    }

    private static CampaignProjection projection(String repository, String title) {
        return new CampaignProjection(
                CampaignProjection.SCHEMA_VERSION,
                CampaignProjection.MAPPING_ALGORITHM_VERSION,
                CampaignProjection.SCORING_RULESET_VERSION,
                repository.replace('/', '-'),
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

    private static SavedCampaignId campaignId(long value) {
        return new SavedCampaignId(new UUID(0, value));
    }

    private static final class MutableProjectionSource implements CampaignProjectionSource {
        private CampaignProjection projection;

        private MutableProjectionSource(CampaignProjection projection) {
            this.projection = projection;
        }

        @Override
        public CampaignProjection create(String repositoryUrl) {
            return projection;
        }
    }

    private static final class InMemorySavedCampaignStore implements SavedCampaignStore {
        private final Map<SavedCampaignId, SavedCampaign> campaigns = new LinkedHashMap<>();

        @Override
        public Optional<SavedCampaign> findByOwnerAndRepository(AccountId ownerId, String repository) {
            return campaigns.values().stream()
                    .filter(campaign -> campaign.ownerId().equals(ownerId))
                    .filter(campaign -> campaign.projection().repository().equalsIgnoreCase(repository))
                    .findFirst();
        }

        @Override
        public Optional<SavedCampaign> findByIdAndOwner(SavedCampaignId campaignId, AccountId ownerId) {
            return Optional.ofNullable(campaigns.get(campaignId))
                    .filter(campaign -> campaign.ownerId().equals(ownerId));
        }

        @Override
        public List<SavedCampaign> findAllByOwner(AccountId ownerId) {
            return campaigns.values().stream()
                    .filter(campaign -> campaign.ownerId().equals(ownerId))
                    .toList();
        }

        @Override
        public long countByOwner(AccountId ownerId) {
            return findAllByOwner(ownerId).size();
        }

        @Override
        public SavedCampaign save(SavedCampaign campaign) {
            campaigns.put(campaign.id(), campaign);
            return campaign;
        }

        @Override
        public void delete(SavedCampaign campaign) {
            campaigns.remove(campaign.id());
        }

        @Override
        public int deleteAllByOwner(AccountId ownerId) {
            var ownedIds = new ArrayList<>(campaigns.values().stream()
                    .filter(campaign -> campaign.ownerId().equals(ownerId))
                    .map(SavedCampaign::id)
                    .toList());
            ownedIds.forEach(campaigns::remove);
            return ownedIds.size();
        }
    }
}
