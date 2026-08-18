package com.commitquest.campaign.application;

import com.commitquest.campaign.domain.SavedCampaign;
import com.commitquest.campaign.domain.SavedCampaignId;
import com.commitquest.identity.domain.AccountId;
import java.util.List;
import java.util.Optional;

public interface SavedCampaignStore {

    Optional<SavedCampaign> findByOwnerAndRepository(AccountId ownerId, String repository);

    Optional<SavedCampaign> findByIdAndOwner(SavedCampaignId campaignId, AccountId ownerId);

    List<SavedCampaign> findAllByOwner(AccountId ownerId);

    long countByOwner(AccountId ownerId);

    SavedCampaign save(SavedCampaign campaign);

    void delete(SavedCampaign campaign);

    int deleteAllByOwner(AccountId ownerId);
}
