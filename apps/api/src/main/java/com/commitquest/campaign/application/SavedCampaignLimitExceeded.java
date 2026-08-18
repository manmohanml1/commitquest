package com.commitquest.campaign.application;

public final class SavedCampaignLimitExceeded extends RuntimeException {

    public SavedCampaignLimitExceeded(int limit) {
        super("An account can save at most " + limit + " campaigns in v0.4.");
    }
}
