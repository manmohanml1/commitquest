import Phaser from 'phaser';

import { CampaignEvidence } from '../domain/campaign';

const COLORS = { mint: 0x65f4c5, amber: 0xf5b942, blue: 0x78bfff } as const;

export class PortfolioCitadelScene extends Phaser.Scene {
  private readonly nodes: Phaser.GameObjects.Container[] = [];
  private selectedIndex = 0;

  constructor(
    private readonly evidence: ReadonlyArray<CampaignEvidence>,
    private readonly onSelect: (index: number) => void,
  ) {
    super('portfolio-citadel');
  }

  create(): void {
    this.drawGrid();
    this.drawRoutes();
    this.evidence.forEach((item, index) => this.createRegion(item, index));
    this.game.events.on('commitquest:select-region', this.highlightRegion, this);
    this.highlightRegion(0);
  }

  shutdown(): void {
    this.game.events.off('commitquest:select-region', this.highlightRegion, this);
  }

  private drawGrid(): void {
    const graphics = this.add.graphics();
    graphics.lineStyle(1, 0x78bfff, 0.07);
    for (let x = 0; x <= 760; x += 38) graphics.lineBetween(x, 0, x, 470);
    for (let y = 0; y <= 470; y += 38) graphics.lineBetween(0, y, 760, y);

    graphics.lineStyle(1, 0x78bfff, 0.14);
    graphics.strokeEllipse(380, 235, 620, 330);
    graphics.strokeEllipse(390, 230, 470, 245);
  }

  private drawRoutes(): void {
    const graphics = this.add.graphics();
    graphics.lineStyle(2, COLORS.mint, 0.55);
    const points = this.evidence.map(
      (item) => new Phaser.Math.Vector2(item.position.x, item.position.y),
    );
    for (let index = 0; index < points.length - 1; index += 1) {
      graphics.lineBetween(
        points[index].x,
        points[index].y,
        points[index + 1].x,
        points[index + 1].y,
      );
    }
    [
      [0, 3],
      [1, 4],
      [2, 5],
    ].forEach(([from, to]) =>
      graphics.lineBetween(points[from].x, points[from].y, points[to].x, points[to].y),
    );
  }

  private createRegion(item: CampaignEvidence, index: number): void {
    const color = COLORS[item.tone];
    const diamond = this.add
      .rectangle(0, 0, 70, 70, 0x10263a, 1)
      .setStrokeStyle(2, color, 0.85)
      .setAngle(45);
    const icon = this.add
      .text(0, -2, item.icon, {
        color: `#${color.toString(16).padStart(6, '0')}`,
        fontFamily: 'Consolas, monospace',
        fontSize: '24px',
        fontStyle: 'bold',
      })
      .setOrigin(0.5);
    const label = this.add
      .text(0, 62, item.title, {
        color: '#f2f7f4',
        fontFamily: 'Segoe UI, sans-serif',
        fontSize: '13px',
        fontStyle: 'bold',
        stroke: '#07111f',
        strokeThickness: 3,
      })
      .setOrigin(0.5);
    const status = this.add
      .text(0, 84, item.status.toUpperCase(), {
        color: '#9bb1be',
        fontFamily: 'Consolas, monospace',
        fontSize: '10px',
        stroke: '#07111f',
        strokeThickness: 2,
      })
      .setOrigin(0.5);
    const container = this.add.container(item.position.x, item.position.y, [
      diamond,
      icon,
      label,
      status,
    ]);
    container.setSize(120, 118).setInteractive({ useHandCursor: true });
    container.on('pointerdown', () => this.onSelect(index));
    container.on('pointerover', () =>
      this.tweens.add({ targets: container, scale: 1.06, duration: 120 }),
    );
    container.on('pointerout', () =>
      this.tweens.add({
        targets: container,
        scale: index === this.selectedIndex ? 1.08 : 1,
        duration: 120,
      }),
    );
    this.nodes.push(container);
  }

  private highlightRegion(index: number): void {
    this.selectedIndex = index;
    this.nodes.forEach((node, nodeIndex) => {
      this.tweens.add({
        targets: node,
        scale: nodeIndex === index ? 1.08 : 1,
        alpha: nodeIndex === index ? 1 : 0.72,
        duration: 160,
      });
    });
  }
}
