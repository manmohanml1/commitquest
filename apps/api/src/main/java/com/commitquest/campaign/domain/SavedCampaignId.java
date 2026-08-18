package com.commitquest.campaign.domain;

import java.util.Objects;
import java.util.UUID;

public record SavedCampaignId(UUID value) {

    public SavedCampaignId {
        Objects.requireNonNull(value, "Saved campaign ID is required.");
    }
}
