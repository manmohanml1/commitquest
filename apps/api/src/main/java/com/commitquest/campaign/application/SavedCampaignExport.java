package com.commitquest.campaign.application;

import com.commitquest.campaign.domain.SavedCampaign;
import com.commitquest.preview.domain.CampaignProjection;
import java.time.Instant;
import java.util.UUID;

public record SavedCampaignExport(
        int exportSchemaVersion,
        UUID campaignId,
        String repository,
        String title,
        String visibility,
        int projectionSchemaVersion,
        int mappingAlgorithmVersion,
        int scoringRulesetVersion,
        Instant createdAt,
        Instant updatedAt,
        CampaignProjection projection) {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    static SavedCampaignExport from(SavedCampaign campaign) {
        var projection = campaign.projection();
        return new SavedCampaignExport(
                CURRENT_SCHEMA_VERSION,
                campaign.id().value(),
                projection.repository(),
                projection.title(),
                campaign.visibility().name().toLowerCase(),
                projection.schemaVersion(),
                projection.mappingAlgorithmVersion(),
                projection.scoringRulesetVersion(),
                campaign.createdAt(),
                campaign.updatedAt(),
                projection);
    }
}
