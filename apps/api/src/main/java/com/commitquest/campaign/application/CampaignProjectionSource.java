package com.commitquest.campaign.application;

import com.commitquest.preview.domain.CampaignProjection;

public interface CampaignProjectionSource {

    CampaignProjection create(String repositoryUrl);
}
