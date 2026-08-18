import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
  inject,
  signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import type Phaser from 'phaser';

import { ConnectedCampaignClient, SavedCampaign } from './api/connected-campaign.client';
import { RepositoryPreviewClient } from './api/repository-preview.client';
import { Session } from './api/generated/model/session';
import { PORTFOLIO_CITADEL } from './data/portfolio-citadel.fixture';
import { CampaignEvidence, CampaignProjection, CampaignView } from './domain/campaign';

type PreviewPhase = 'idle' | 'loading' | 'success' | 'error';
type ConnectedPhase =
  'checking' | 'unavailable' | 'signed-out' | 'signing-in' | 'ready' | 'working' | 'error';

@Component({
  selector: 'app-root',
  imports: [DatePipe],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements AfterViewInit, OnDestroy {
  @ViewChild('gameHost') private gameHost?: ElementRef<HTMLDivElement>;

  protected readonly campaign = signal<CampaignProjection>(PORTFOLIO_CITADEL);
  protected readonly campaignViews: ReadonlyArray<{ id: CampaignView; label: string }> = [
    { id: 'map', label: 'World map' },
    { id: 'quests', label: 'Candidate quests' },
    { id: 'encounters', label: 'Encounters' },
    { id: 'chapters', label: 'Chapters' },
  ];
  protected readonly activeView = signal<CampaignView>('map');
  protected readonly selectedIndex = signal(0);
  protected readonly selectedEvidence = signal<CampaignEvidence>(PORTFOLIO_CITADEL.evidence[0]);
  protected readonly repositoryUrl = signal('https://github.com/manmohanml1/commitquest');
  protected readonly previewPhase = signal<PreviewPhase>('idle');
  protected readonly previewMessage = signal(
    'Public repositories only. The preview is temporary and no repository evidence is saved.',
  );
  protected readonly connectedPhase = signal<ConnectedPhase>('checking');
  protected readonly connectedMessage = signal(
    'Waking the private campaign vault. The free host can take about a minute…',
  );
  protected readonly session = signal<Session | null>(null);
  protected readonly savedCampaigns = signal<ReadonlyArray<SavedCampaign>>([]);
  protected readonly pendingDeleteId = signal<string | null>(null);
  protected readonly pendingAccountDelete = signal(false);
  protected readonly githubSignInUrl = '/api/v1/auth/github?returnPath=%2F%23campaign-vault';

  private game?: Phaser.Game;
  private readonly previewClient = inject(RepositoryPreviewClient);
  private readonly connectedClient = inject(ConnectedCampaignClient);

  async ngAfterViewInit(): Promise<void> {
    await Promise.all([this.rebuildMap(), this.loadConnectedState()]);
  }

  ngOnDestroy(): void {
    this.game?.destroy(true);
  }

  protected async createPreview(event: Event): Promise<void> {
    event.preventDefault();
    if (this.previewPhase() === 'loading') return;

    const repositoryUrl = this.repositoryUrl().trim();
    this.previewPhase.set('loading');
    this.previewMessage.set('Reading bounded public GitHub evidence and mapping the campaign…');

    try {
      const projection = await firstValueFrom(this.previewClient.preview(repositoryUrl));
      this.applyCampaign(projection);
      this.previewPhase.set('success');
      this.previewMessage.set(`${projection.title} is ready. No preview data was persisted.`);
      await this.rebuildMap();
      this.gameHost?.nativeElement.scrollIntoView?.({ behavior: 'smooth', block: 'center' });
    } catch (error: unknown) {
      this.previewPhase.set('error');
      this.previewMessage.set(this.previewClient.message(error));
    }
  }

  protected async restorePortfolioCitadel(): Promise<void> {
    this.applyCampaign(PORTFOLIO_CITADEL);
    this.previewPhase.set('idle');
    this.previewMessage.set(
      'Portfolio Citadel restored from the bundled, provider-independent fixture.',
    );
    await this.rebuildMap();
  }

  protected async saveCurrentCampaign(): Promise<void> {
    const session = this.session();
    if (!session || this.connectedPhase() === 'working') return;
    this.connectedPhase.set('working');
    this.connectedMessage.set('Sealing this repository campaign inside your private vault…');
    try {
      const saved = await firstValueFrom(
        this.connectedClient.save(
          session.csrfToken,
          `https://github.com/${this.campaign().repository}`,
        ),
      );
      this.upsertSavedCampaign(saved);
      this.connectedPhase.set('ready');
      this.connectedMessage.set(`${saved.projection.title} is saved privately.`);
    } catch (error: unknown) {
      this.handleConnectedFailure(error, 'The campaign could not be saved. Please try again.');
    }
  }

  protected beginSignIn(): void {
    this.connectedPhase.set('signing-in');
    this.connectedMessage.set('Opening GitHub to verify your identity…');
  }

  protected async openSavedCampaign(saved: SavedCampaign): Promise<void> {
    this.applyCampaign(saved.projection);
    this.repositoryUrl.set(`https://github.com/${saved.projection.repository}`);
    this.previewPhase.set('success');
    this.previewMessage.set(`${saved.projection.title} loaded from your private vault.`);
    await this.rebuildMap();
    this.gameHost?.nativeElement.scrollIntoView?.({ behavior: 'smooth', block: 'center' });
  }

  protected async refreshSavedCampaign(saved: SavedCampaign): Promise<void> {
    const session = this.session();
    if (!session || this.connectedPhase() === 'working') return;
    this.connectedPhase.set('working');
    this.connectedMessage.set(`Refreshing ${saved.projection.title} from public GitHub evidence…`);
    try {
      const refreshed = await firstValueFrom(
        this.connectedClient.refresh(session.csrfToken, saved.id),
      );
      this.upsertSavedCampaign(refreshed);
      if (this.campaign().repository === refreshed.projection.repository) {
        this.applyCampaign(refreshed.projection);
        await this.rebuildMap();
      }
      this.connectedPhase.set('ready');
      this.connectedMessage.set(`${refreshed.projection.title} is up to date.`);
    } catch (error: unknown) {
      this.handleConnectedFailure(error, 'The saved campaign could not be refreshed.');
    }
  }

  protected async toggleVisibility(saved: SavedCampaign): Promise<void> {
    const session = this.session();
    if (!session || this.connectedPhase() === 'working') return;
    this.connectedPhase.set('working');
    const nextVisibility = saved.visibility === 'private' ? 'UNLISTED' : 'PRIVATE';
    try {
      const changed = await firstValueFrom(
        this.connectedClient.changeVisibility(session.csrfToken, saved.id, nextVisibility),
      );
      this.upsertSavedCampaign(changed);
      this.connectedPhase.set('ready');
      this.connectedMessage.set(`${changed.projection.title} is now ${changed.visibility}.`);
    } catch (error: unknown) {
      this.handleConnectedFailure(error, 'Campaign visibility could not be changed.');
    }
  }

  protected async exportSavedCampaign(saved: SavedCampaign): Promise<void> {
    try {
      const exported = await firstValueFrom(this.connectedClient.export(saved.id));
      const payload = new Blob([JSON.stringify(exported, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(payload);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${saved.projection.slug}-commitquest.json`;
      link.click();
      URL.revokeObjectURL(url);
      this.connectedMessage.set(`${saved.projection.title} export prepared.`);
    } catch (error: unknown) {
      this.handleConnectedFailure(error, 'The campaign export could not be prepared.');
    }
  }

  protected requestCampaignDelete(campaignId: string): void {
    this.pendingDeleteId.set(campaignId);
  }

  protected cancelCampaignDelete(): void {
    this.pendingDeleteId.set(null);
  }

  protected async deleteSavedCampaign(saved: SavedCampaign): Promise<void> {
    const session = this.session();
    if (!session || this.connectedPhase() === 'working') return;
    this.connectedPhase.set('working');
    try {
      await firstValueFrom(this.connectedClient.delete(session.csrfToken, saved.id));
      this.savedCampaigns.update((campaigns) => campaigns.filter((item) => item.id !== saved.id));
      this.pendingDeleteId.set(null);
      this.connectedPhase.set('ready');
      this.connectedMessage.set(`${saved.projection.title} was removed from your vault.`);
    } catch (error: unknown) {
      this.handleConnectedFailure(error, 'The saved campaign could not be deleted.');
    }
  }

  protected async logout(): Promise<void> {
    const session = this.session();
    if (!session) return;
    try {
      await firstValueFrom(this.connectedClient.logout(session.csrfToken));
      this.session.set(null);
      this.savedCampaigns.set([]);
      this.pendingAccountDelete.set(false);
      this.connectedPhase.set('signed-out');
      this.connectedMessage.set('Signed out. Public, ephemeral previews are still available.');
    } catch (error: unknown) {
      this.handleConnectedFailure(error, 'Sign out could not be completed. Please try again.');
    }
  }

  protected requestAccountDelete(): void {
    this.pendingAccountDelete.set(true);
  }

  protected cancelAccountDelete(): void {
    this.pendingAccountDelete.set(false);
  }

  protected async deleteAccount(): Promise<void> {
    const session = this.session();
    if (!session || this.connectedPhase() === 'working') return;
    this.connectedPhase.set('working');
    this.connectedMessage.set('Deleting your CommitQuest account and imported campaign data…');
    try {
      await firstValueFrom(this.connectedClient.deleteAccount(session.csrfToken));
      this.session.set(null);
      this.savedCampaigns.set([]);
      this.pendingDeleteId.set(null);
      this.pendingAccountDelete.set(false);
      this.connectedPhase.set('signed-out');
      this.connectedMessage.set(
        'Your CommitQuest account and imported campaign data were permanently deleted.',
      );
    } catch (error: unknown) {
      this.pendingAccountDelete.set(false);
      this.handleConnectedFailure(error, 'Account data could not be deleted. Please try again.');
    }
  }

  protected retryConnectedState(): void {
    if (this.connectedPhase() === 'checking' || this.connectedPhase() === 'working') return;
    this.connectedPhase.set('checking');
    this.connectedMessage.set(
      'Waking the private campaign vault. The free host can take about a minute…',
    );
    void this.loadConnectedState();
  }

  protected updateRepositoryUrl(event: Event): void {
    this.repositoryUrl.set((event.target as HTMLInputElement).value);
  }

  protected selectEvidence(index: number): void {
    const evidence = this.campaign().evidence[index];
    if (!evidence) return;
    this.selectedIndex.set(index);
    this.selectedEvidence.set(evidence);
    this.game?.events.emit('commitquest:select-region', index);
  }

  protected selectView(view: CampaignView): void {
    this.activeView.set(view);
  }

  protected isEphemeralCampaign(): boolean {
    return this.campaign().schemaVersion >= 3 || this.campaign().mode === 'preview';
  }

  protected campaignModeLabel(): string {
    return `${this.campaign().mode.charAt(0).toUpperCase()}${this.campaign().mode.slice(1)}`;
  }

  protected handleViewKey(event: KeyboardEvent, index: number): void {
    if (!['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) return;

    event.preventDefault();
    const lastIndex = this.campaignViews.length - 1;
    const nextIndex =
      event.key === 'Home'
        ? 0
        : event.key === 'End'
          ? lastIndex
          : event.key === 'ArrowRight'
            ? (index + 1) % this.campaignViews.length
            : (index - 1 + this.campaignViews.length) % this.campaignViews.length;
    this.selectView(this.campaignViews[nextIndex].id);

    const tabList = (event.currentTarget as HTMLElement).parentElement;
    (tabList?.querySelectorAll<HTMLButtonElement>('[role="tab"]')[nextIndex] ?? null)?.focus();
  }

  private applyCampaign(campaign: CampaignProjection): void {
    this.campaign.set(campaign);
    this.activeView.set('map');
    this.selectedIndex.set(0);
    this.selectedEvidence.set(campaign.evidence[0]);
  }

  private async loadConnectedState(): Promise<void> {
    try {
      const session = await firstValueFrom(this.connectedClient.session());
      this.session.set(session);
      this.savedCampaigns.set(await firstValueFrom(this.connectedClient.list()));
      this.connectedPhase.set('ready');
      this.connectedMessage.set(
        this.savedCampaigns().length === 0
          ? 'Your vault is ready. Generate or explore a campaign, then save it privately.'
          : `${this.savedCampaigns().length} private campaign${this.savedCampaigns().length === 1 ? '' : 's'} ready.`,
      );
    } catch (error: unknown) {
      if (this.connectedClient.isSignedOut(error)) {
        this.connectedPhase.set('signed-out');
        this.connectedMessage.set('Sign in with GitHub to save private campaigns across visits.');
        return;
      }
      if (this.connectedClient.isUnavailable(error)) {
        this.connectedPhase.set('unavailable');
        this.connectedMessage.set(
          'Private campaign saving is not configured in this environment. Public previews still work.',
        );
        return;
      }
      if (this.connectedClient.isTransient(error)) {
        this.connectedPhase.set('error');
        this.connectedMessage.set(
          'The free vault host is still waking up. Retry the connection; public previews remain available.',
        );
        return;
      }
      this.connectedPhase.set('error');
      this.connectedMessage.set(
        'The private campaign vault could not be reached. Retry the connection or continue with public previews.',
      );
    }
  }

  private upsertSavedCampaign(saved: SavedCampaign): void {
    this.savedCampaigns.update((campaigns) => [
      saved,
      ...campaigns.filter((campaign) => campaign.id !== saved.id),
    ]);
  }

  private handleConnectedFailure(error: unknown, fallback: string): void {
    if (this.connectedClient.isSignedOut(error)) {
      this.session.set(null);
      this.savedCampaigns.set([]);
      this.pendingDeleteId.set(null);
      this.pendingAccountDelete.set(false);
      this.connectedPhase.set('signed-out');
      this.connectedMessage.set('Your session expired. Sign in with GitHub to reopen the vault.');
      return;
    }
    this.connectedPhase.set('error');
    this.connectedMessage.set(this.connectedClient.message(error, fallback));
  }

  private async rebuildMap(): Promise<void> {
    if (
      !this.gameHost ||
      typeof navigator === 'undefined' ||
      navigator.userAgent.includes('jsdom')
    ) {
      return;
    }

    this.game?.destroy(true);
    const [{ default: PhaserRuntime }, { PortfolioCitadelScene }] = await Promise.all([
      import('phaser'),
      import('./game/portfolio-citadel.scene'),
    ]);
    const scene = new PortfolioCitadelScene(this.campaign().evidence, (index) => {
      this.selectEvidence(index);
    });

    this.game = new PhaserRuntime.Game({
      type: PhaserRuntime.AUTO,
      parent: this.gameHost.nativeElement,
      backgroundColor: '#091522',
      width: 760,
      height: 470,
      render: { antialias: true, pixelArt: false },
      scale: { mode: PhaserRuntime.Scale.FIT, autoCenter: PhaserRuntime.Scale.CENTER_BOTH },
      scene,
    });
  }
}
