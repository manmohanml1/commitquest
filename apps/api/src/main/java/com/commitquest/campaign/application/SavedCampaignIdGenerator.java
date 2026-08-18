package com.commitquest.campaign.application;

import com.commitquest.campaign.domain.SavedCampaignId;

public interface SavedCampaignIdGenerator {

    SavedCampaignId next();
}
