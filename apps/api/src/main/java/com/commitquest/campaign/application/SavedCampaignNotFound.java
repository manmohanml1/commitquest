package com.commitquest.campaign.application;

public final class SavedCampaignNotFound extends RuntimeException {

    public SavedCampaignNotFound() {
        super("The saved campaign was not found.");
    }
}
