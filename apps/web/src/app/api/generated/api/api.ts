export * from './identity.service';
import { IdentityClient } from './identity.service';
export * from './repositoryPreviews.service';
import { RepositoryPreviewsClient } from './repositoryPreviews.service';
export * from './savedCampaigns.service';
import { SavedCampaignsClient } from './savedCampaigns.service';
export const APIS = [IdentityClient, RepositoryPreviewsClient, SavedCampaignsClient];
