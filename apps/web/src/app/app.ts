import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild, signal } from '@angular/core';
import type Phaser from 'phaser';

import { PORTFOLIO_CITADEL } from './data/portfolio-citadel.fixture';
import { CampaignEvidence } from './domain/campaign';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements AfterViewInit, OnDestroy {
  @ViewChild('gameHost') private gameHost?: ElementRef<HTMLDivElement>;

  protected readonly campaign = PORTFOLIO_CITADEL;
  protected readonly selectedIndex = signal(0);
  protected readonly selectedEvidence = signal<CampaignEvidence>(PORTFOLIO_CITADEL.evidence[0]);
  private game?: Phaser.Game;

  async ngAfterViewInit(): Promise<void> {
    if (
      !this.gameHost ||
      typeof navigator === 'undefined' ||
      navigator.userAgent.includes('jsdom')
    ) {
      return;
    }

    const [{ default: PhaserRuntime }, { PortfolioCitadelScene }] = await Promise.all([
      import('phaser'),
      import('./game/portfolio-citadel.scene'),
    ]);

    const scene = new PortfolioCitadelScene(PORTFOLIO_CITADEL.evidence, (index) => {
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

  ngOnDestroy(): void {
    this.game?.destroy(true);
  }

  protected selectEvidence(index: number): void {
    const evidence = this.campaign.evidence[index];
    if (!evidence) return;
    this.selectedIndex.set(index);
    this.selectedEvidence.set(evidence);
    this.game?.events.emit('commitquest:select-region', index);
  }
}
