package com.commitquest.campaign.domain;

import com.commitquest.identity.domain.AccountId;
import com.commitquest.preview.domain.CampaignProjection;
import java.time.Instant;
import java.util.Objects;

public record SavedCampaign(
        SavedCampaignId id,
        AccountId ownerId,
        CampaignProjection projection,
        CampaignVisibility visibility,
        Instant createdAt,
        Instant updatedAt) {

    public SavedCampaign {
        Objects.requireNonNull(id, "Saved campaign ID is required.");
        Objects.requireNonNull(ownerId, "Campaign owner is required.");
        Objects.requireNonNull(projection, "Campaign projection is required.");
        Objects.requireNonNull(visibility, "Campaign visibility is required.");
        Objects.requireNonNull(createdAt, "Campaign creation time is required.");
        Objects.requireNonNull(updatedAt, "Campaign update time is required.");
        if (projection.repository().isBlank()) throw new IllegalArgumentException("Repository is required.");
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Campaign update time cannot precede creation.");
        }
    }

    public static SavedCampaign create(
            SavedCampaignId id, AccountId ownerId, CampaignProjection projection, Instant now) {
        return new SavedCampaign(id, ownerId, projection, CampaignVisibility.PRIVATE, now, now);
    }

    public SavedCampaign refresh(CampaignProjection refreshedProjection, Instant now) {
        return new SavedCampaign(id, ownerId, refreshedProjection, visibility, createdAt, now);
    }

    public SavedCampaign changeVisibility(CampaignVisibility nextVisibility, Instant now) {
        return new SavedCampaign(id, ownerId, projection, nextVisibility, createdAt, now);
    }
}
