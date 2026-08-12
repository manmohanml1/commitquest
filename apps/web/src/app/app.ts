import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
  inject,
  signal,
} from '@angular/core';
import { firstValueFrom } from 'rxjs';
import type Phaser from 'phaser';

import { RepositoryPreviewClient } from './api/repository-preview.client';
import { PORTFOLIO_CITADEL } from './data/portfolio-citadel.fixture';
import { CampaignEvidence, CampaignProjection, CampaignView } from './domain/campaign';

type PreviewPhase = 'idle' | 'loading' | 'success' | 'error';

@Component({
  selector: 'app-root',
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

  private game?: Phaser.Game;
  private readonly previewClient = inject(RepositoryPreviewClient);

  async ngAfterViewInit(): Promise<void> {
    await this.rebuildMap();
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
