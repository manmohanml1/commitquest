package com.commitquest.campaign.provider;

import com.commitquest.campaign.application.SavedCampaignService;
import com.commitquest.campaign.application.SavedCampaignStore;
import com.commitquest.campaign.domain.SavedCampaignId;
import com.commitquest.preview.application.RepositoryPreviewService;
import java.time.Clock;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "commitquest.identity", name = "enabled", havingValue = "true")
public class CampaignConfiguration {

    @Bean
    SavedCampaignService savedCampaignService(
            SavedCampaignStore store, RepositoryPreviewService previewService) {
        return new SavedCampaignService(
                store,
                previewService::preview,
                () -> new SavedCampaignId(UUID.randomUUID()),
                Clock.systemUTC());
    }
}
