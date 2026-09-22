import { Injectable, computed, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { ConnectedCampaignClient, SavedCampaign } from '../api/connected-campaign.client';
import { Session } from '../api/generated/model/session';
import { IdentityNavigation } from '../api/identity-navigation.service';

export type ShellPhase = 'checking' | 'signed-out' | 'ready' | 'unavailable' | 'error';

@Injectable()
export class AuthenticatedShellStore {
  private readonly client = inject(ConnectedCampaignClient);
  private readonly identityNavigation = inject(IdentityNavigation);

  readonly phase = signal<ShellPhase>('checking');
  readonly message = signal('Rekindling your private campaign archive…');
  readonly session = signal<Session | null>(null);
  readonly campaigns = signal<ReadonlyArray<SavedCampaign>>([]);
  readonly selectedCampaignId = signal<string | null>(null);
  readonly selectedCampaign = computed(
    () =>
      this.campaigns().find((campaign) => campaign.id === this.selectedCampaignId()) ??
      this.campaigns()[0] ??
      null,
  );

  async load(): Promise<void> {
    this.phase.set('checking');
    this.message.set('Rekindling your private campaign archive…');

    try {
      const session = await firstValueFrom(this.client.session());
      const campaigns = await firstValueFrom(this.client.list());
      this.session.set(session);
      this.campaigns.set(campaigns);
      if (!this.selectedCampaignId() && campaigns.length > 0) {
        this.selectedCampaignId.set(campaigns[0].id);
      }
      this.phase.set('ready');
      this.message.set(
        campaigns.length === 0
          ? 'Your workspace is ready. Save a public preview to begin your first realm.'
          : `${campaigns.length} private campaign${campaigns.length === 1 ? '' : 's'} ready.`,
      );
    } catch (error: unknown) {
      this.session.set(null);
      this.campaigns.set([]);
      this.selectedCampaignId.set(null);

      if (this.client.isSignedOut(error)) {
        this.phase.set('signed-out');
        this.message.set('Sign in with GitHub to open your private campaign workspace.');
      } else if (this.client.isUnavailable(error)) {
        this.phase.set('unavailable');
        this.message.set('Connected campaigns are not enabled in this environment.');
      } else if (this.client.isTransient(error)) {
        this.phase.set('error');
        this.message.set(
          'The free campaign host is still waking. Retry or use the public preview.',
        );
      } else {
        this.phase.set('error');
        this.message.set(
          this.client.message(error, 'The campaign workspace could not be reached.'),
        );
      }
    }
  }

  selectCampaign(campaignId: string): void {
    if (this.campaigns().some((campaign) => campaign.id === campaignId)) {
      this.selectedCampaignId.set(campaignId);
    }
  }

  signIn(selectAccount = false): void {
    const returnPath = encodeURIComponent('/app/overview');
    this.identityNavigation.navigate(
      `/api/v1/auth/github?returnPath=${returnPath}${selectAccount ? '&selectAccount=true' : ''}`,
    );
  }

  async signOut(): Promise<void> {
    const session = this.session();
    if (!session) return;

    try {
      await firstValueFrom(this.client.logout(session.csrfToken));
      this.session.set(null);
      this.campaigns.set([]);
      this.selectedCampaignId.set(null);
      this.phase.set('signed-out');
      this.message.set('You are signed out. Public repository previews remain available.');
    } catch (error: unknown) {
      this.phase.set('error');
      this.message.set(this.client.message(error, 'Sign out could not be completed.'));
    }
  }
}
