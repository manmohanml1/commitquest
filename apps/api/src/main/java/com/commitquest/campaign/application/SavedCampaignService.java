package com.commitquest.campaign.application;

import com.commitquest.campaign.domain.CampaignVisibility;
import com.commitquest.campaign.domain.SavedCampaign;
import com.commitquest.campaign.domain.SavedCampaignId;
import com.commitquest.identity.domain.AccountId;
import java.time.Clock;
import java.util.List;
import java.util.Objects;

public final class SavedCampaignService {

    public static final int MAX_SAVED_CAMPAIGNS = 25;

    private final SavedCampaignStore store;
    private final CampaignProjectionSource projectionSource;
    private final SavedCampaignIdGenerator idGenerator;
    private final Clock clock;

    public SavedCampaignService(
            SavedCampaignStore store,
            CampaignProjectionSource projectionSource,
            SavedCampaignIdGenerator idGenerator,
            Clock clock) {
        this.store = Objects.requireNonNull(store);
        this.projectionSource = Objects.requireNonNull(projectionSource);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    public SavedCampaign save(AccountId ownerId, String repositoryUrl) {
        Objects.requireNonNull(ownerId, "Campaign owner is required.");
        var projection = projectionSource.create(repositoryUrl);
        var existing = store.findByOwnerAndRepository(ownerId, projection.repository());
        var now = clock.instant();
        if (existing.isPresent()) return store.save(existing.orElseThrow().refresh(projection, now));
        if (store.countByOwner(ownerId) >= MAX_SAVED_CAMPAIGNS) {
            throw new SavedCampaignLimitExceeded(MAX_SAVED_CAMPAIGNS);
        }
        return store.save(SavedCampaign.create(idGenerator.next(), ownerId, projection, now));
    }

    public List<SavedCampaign> list(AccountId ownerId) {
        Objects.requireNonNull(ownerId, "Campaign owner is required.");
        return List.copyOf(store.findAllByOwner(ownerId));
    }

    public SavedCampaign get(AccountId ownerId, SavedCampaignId campaignId) {
        Objects.requireNonNull(ownerId, "Campaign owner is required.");
        Objects.requireNonNull(campaignId, "Saved campaign ID is required.");
        return store.findByIdAndOwner(campaignId, ownerId).orElseThrow(SavedCampaignNotFound::new);
    }

    public SavedCampaign changeVisibility(
            AccountId ownerId, SavedCampaignId campaignId, CampaignVisibility visibility) {
        Objects.requireNonNull(visibility, "Campaign visibility is required.");
        return store.save(get(ownerId, campaignId).changeVisibility(visibility, clock.instant()));
    }

    public SavedCampaignExport export(AccountId ownerId, SavedCampaignId campaignId) {
        return SavedCampaignExport.from(get(ownerId, campaignId));
    }

    public void delete(AccountId ownerId, SavedCampaignId campaignId) {
        store.delete(get(ownerId, campaignId));
    }

    public int deleteAllForOwner(AccountId ownerId) {
        Objects.requireNonNull(ownerId, "Campaign owner is required.");
        return store.deleteAllByOwner(ownerId);
    }
}
