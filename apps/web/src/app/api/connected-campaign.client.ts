import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';

import { CampaignProjection } from '../domain/campaign';
import { IdentityClient } from './generated/api/identity.service';
import { SavedCampaignsClient } from './generated/api/savedCampaigns.service';
import { ChangeCampaignVisibilityRequestVisibilityEnum } from './generated/model/changeCampaignVisibilityRequest';
import { Session } from './generated/model/session';
import { SavedCampaign as GeneratedSavedCampaign } from './generated/model/savedCampaign';
import { SavedCampaignExport } from './generated/model/savedCampaignExport';

export interface SavedCampaign {
  readonly id: string;
  readonly projection: CampaignProjection;
  readonly visibility: 'private' | 'unlisted';
  readonly createdAt: string;
  readonly updatedAt: string;
}

@Injectable({ providedIn: 'root' })
export class ConnectedCampaignClient {
  private readonly identity = inject(IdentityClient);
  private readonly campaigns = inject(SavedCampaignsClient);

  session(): Observable<Session> {
    return this.identity.getSession();
  }

  list(): Observable<ReadonlyArray<SavedCampaign>> {
    return this.campaigns
      .listSavedCampaigns()
      .pipe(map((campaigns) => campaigns.map((campaign) => this.map(campaign))));
  }

  save(csrfToken: string, repositoryUrl: string): Observable<SavedCampaign> {
    return this.campaigns
      .saveCampaign(csrfToken, { repositoryUrl })
      .pipe(map((campaign) => this.map(campaign)));
  }

  refresh(csrfToken: string, campaignId: string): Observable<SavedCampaign> {
    return this.campaigns
      .refreshSavedCampaign(csrfToken, campaignId)
      .pipe(map((campaign) => this.map(campaign)));
  }

  changeVisibility(
    csrfToken: string,
    campaignId: string,
    visibility: 'PRIVATE' | 'UNLISTED',
  ): Observable<SavedCampaign> {
    return this.campaigns
      .changeSavedCampaignVisibility(csrfToken, campaignId, {
        visibility:
          visibility === 'PRIVATE'
            ? ChangeCampaignVisibilityRequestVisibilityEnum.PRIVATE
            : ChangeCampaignVisibilityRequestVisibilityEnum.UNLISTED,
      })
      .pipe(map((campaign) => this.map(campaign)));
  }

  export(campaignId: string): Observable<SavedCampaignExport> {
    return this.campaigns.exportSavedCampaign(campaignId);
  }

  delete(csrfToken: string, campaignId: string): Observable<void> {
    return this.campaigns.deleteSavedCampaign(csrfToken, campaignId);
  }

  logout(csrfToken: string): Observable<void> {
    return this.identity.logout(csrfToken);
  }

  deleteAccount(csrfToken: string): Observable<void> {
    return this.identity.deleteAccount(csrfToken);
  }

  message(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const problem = error.error as { detail?: string } | null;
      if (problem?.detail) return problem.detail;
    }
    return fallback;
  }

  isUnavailable(error: unknown): boolean {
    return error instanceof HttpErrorResponse && [0, 404, 503].includes(error.status);
  }

  isSignedOut(error: unknown): boolean {
    return error instanceof HttpErrorResponse && error.status === 401;
  }

  private map(campaign: GeneratedSavedCampaign): SavedCampaign {
    return {
      ...campaign,
      projection: campaign.projection as CampaignProjection,
    };
  }
}
