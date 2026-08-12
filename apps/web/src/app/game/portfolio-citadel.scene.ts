import Phaser from 'phaser';

import { CampaignEvidence } from '../domain/campaign';

const COLORS = { mint: 0x65f4c5, amber: 0xf5b942, blue: 0x78bfff } as const;
const LAND = 0x10293a;
const LAND_DARK = 0x0a1d2c;
const STRUCTURE = 0x173b50;

export class PortfolioCitadelScene extends Phaser.Scene {
  private readonly nodes: Phaser.GameObjects.Container[] = [];
  private readonly selectionRings: Phaser.GameObjects.Arc[] = [];
  private selectedIndex = 0;

  constructor(
    private readonly evidence: ReadonlyArray<CampaignEvidence>,
    private readonly onSelect: (index: number) => void,
  ) {
    super('portfolio-citadel');
  }

  create(): void {
    this.drawWorld();
    this.drawRoutes();
    this.evidence.forEach((item, index) => this.createRegion(item, index));
    this.game.events.on('commitquest:select-region', this.highlightRegion, this);
    this.highlightRegion(0);
  }

  shutdown(): void {
    this.game.events.off('commitquest:select-region', this.highlightRegion, this);
  }

  private drawWorld(): void {
    const sky = this.add.graphics();
    sky.fillStyle(0x07121f).fillRect(0, 0, 760, 470);
    sky.fillStyle(0x0a1a2b).fillRect(0, 115, 760, 355);
    sky.fillStyle(0x10243a, 0.65).fillRect(0, 225, 760, 245);

    sky.fillStyle(0x78bfff, 0.08);
    [
      [78, 74, 2],
      [184, 112, 1.5],
      [308, 58, 2],
      [514, 92, 1.5],
      [698, 62, 2],
    ].forEach(([x, y, radius]) => sky.fillCircle(x, y, radius));

    sky.fillStyle(0x0b2032, 0.95);
    sky.fillTriangle(0, 235, 145, 95, 270, 235);
    sky.fillTriangle(155, 235, 355, 72, 510, 235);
    sky.fillTriangle(390, 235, 590, 105, 760, 235);
    sky.fillStyle(0x143448, 0.62);
    sky.fillTriangle(210, 235, 355, 72, 395, 235);
    sky.fillTriangle(485, 235, 590, 105, 642, 235);

    const terrain = this.add.graphics();
    terrain.fillStyle(LAND_DARK, 1);
    terrain.fillPoints(
      [
        new Phaser.Math.Vector2(35, 250),
        new Phaser.Math.Vector2(128, 188),
        new Phaser.Math.Vector2(244, 205),
        new Phaser.Math.Vector2(354, 163),
        new Phaser.Math.Vector2(478, 202),
        new Phaser.Math.Vector2(626, 181),
        new Phaser.Math.Vector2(735, 248),
        new Phaser.Math.Vector2(716, 420),
        new Phaser.Math.Vector2(545, 451),
        new Phaser.Math.Vector2(367, 423),
        new Phaser.Math.Vector2(181, 454),
        new Phaser.Math.Vector2(48, 395),
      ],
      true,
    );
    terrain.lineStyle(2, 0x2f6672, 0.72);
    terrain.strokePoints(
      [
        new Phaser.Math.Vector2(35, 250),
        new Phaser.Math.Vector2(128, 188),
        new Phaser.Math.Vector2(244, 205),
        new Phaser.Math.Vector2(354, 163),
        new Phaser.Math.Vector2(478, 202),
        new Phaser.Math.Vector2(626, 181),
        new Phaser.Math.Vector2(735, 248),
        new Phaser.Math.Vector2(716, 420),
        new Phaser.Math.Vector2(545, 451),
        new Phaser.Math.Vector2(367, 423),
        new Phaser.Math.Vector2(181, 454),
        new Phaser.Math.Vector2(48, 395),
      ],
      true,
    );
    terrain.fillStyle(LAND, 0.7);
    terrain.fillEllipse(370, 330, 590, 205);
    terrain.lineStyle(1, 0x78bfff, 0.1);
    terrain.strokeEllipse(380, 320, 650, 245);
    terrain.strokeEllipse(390, 325, 510, 178);

    const foreground = this.add.graphics();
    foreground.fillStyle(0x06101b, 0.85);
    foreground.fillTriangle(0, 470, 0, 390, 115, 470);
    foreground.fillTriangle(645, 470, 760, 385, 760, 470);
  }

  private drawRoutes(): void {
    const routes = this.add.graphics();
    routes.lineStyle(7, 0x04101a, 0.8);
    this.forEachRoute((from, to) => routes.lineBetween(from.x, from.y, to.x, to.y));
    routes.lineStyle(2, COLORS.mint, 0.55);
    this.forEachRoute((from, to) => routes.lineBetween(from.x, from.y, to.x, to.y));

    this.evidence.forEach((item) => {
      routes.fillStyle(COLORS[item.tone], 0.75);
      routes.fillCircle(item.position.x, item.position.y + 38, 4);
    });
  }

  private forEachRoute(
    callback: (from: Phaser.Math.Vector2, to: Phaser.Math.Vector2) => void,
  ): void {
    const points = this.evidence.map(
      (item) => new Phaser.Math.Vector2(item.position.x, item.position.y + 38),
    );
    [
      [0, 1],
      [1, 2],
      [0, 3],
      [1, 4],
      [2, 5],
      [3, 4],
      [4, 5],
    ].forEach(([from, to]) => callback(points[from], points[to]));
  }

  private createRegion(item: CampaignEvidence, index: number): void {
    const color = COLORS[item.tone];
    const glow = this.add.circle(0, 18, 58, color, 0.08).setStrokeStyle(1, color, 0.32);
    const landmark = this.createLandmark(item.id, color);
    const beacon = this.add.circle(0, -58, 5, color, 1).setStrokeStyle(5, color, 0.12);
    const ring = this.add.circle(0, 20, 66).setStrokeStyle(2, color, 0);
    const container = this.add.container(item.position.x, item.position.y, [
      glow,
      landmark,
      beacon,
      ring,
    ]);

    container.setSize(128, 142).setInteractive({ useHandCursor: true });
    container.on('pointerdown', () => this.onSelect(index));
    container.on('pointerover', () => this.tweenNode(container, 1.08));
    container.on('pointerout', () =>
      this.tweenNode(container, index === this.selectedIndex ? 1.1 : 1),
    );
    this.tweens.add({
      targets: beacon,
      alpha: { from: 0.45, to: 1 },
      scale: { from: 0.82, to: 1.18 },
      duration: 950 + index * 90,
      yoyo: true,
      repeat: -1,
    });
    this.nodes.push(container);
    this.selectionRings.push(ring);
  }

  private createLandmark(id: string, color: number): Phaser.GameObjects.Graphics {
    const graphics = this.add.graphics();
    graphics.fillStyle(0x06101b, 0.52).fillEllipse(0, 58, 100, 25);
    graphics.fillStyle(STRUCTURE, 1).lineStyle(3, color, 0.92);

    switch (id) {
      case 'presentation-capital':
        graphics.fillRect(-34, -5, 68, 58).strokeRect(-34, -5, 68, 58);
        graphics.fillRect(-17, -40, 34, 93).strokeRect(-17, -40, 34, 93);
        graphics.fillTriangle(-34, -5, -22, -24, -10, -5);
        graphics.fillTriangle(10, -5, 22, -24, 34, -5);
        graphics.fillTriangle(-17, -40, 0, -60, 17, -40);
        graphics.fillStyle(color, 0.9).fillRect(-5, 31, 10, 22);
        break;
      case 'gateway-keep':
        graphics.fillRect(-46, -20, 27, 73).strokeRect(-46, -20, 27, 73);
        graphics.fillRect(19, -20, 27, 73).strokeRect(19, -20, 27, 73);
        graphics.fillRect(-24, 0, 48, 53).strokeRect(-24, 0, 48, 53);
        graphics.fillTriangle(-46, -20, -32, -43, -19, -20);
        graphics.fillTriangle(19, -20, 32, -43, 46, -20);
        graphics.fillStyle(0x06101b).fillRoundedRect(-9, 19, 18, 34, 9);
        graphics.lineStyle(3, color, 0.92).strokeRoundedRect(-9, 19, 18, 34, 9);
        break;
      case 'neon-vault':
        graphics.fillRoundedRect(-45, -12, 90, 65, 14).strokeRoundedRect(-45, -12, 90, 65, 14);
        graphics.fillStyle(LAND_DARK).fillCircle(0, 10, 25);
        graphics.lineStyle(4, color, 0.92).strokeCircle(0, 10, 25);
        graphics.lineStyle(2, color, 0.7).lineBetween(-25, 10, 25, 10);
        graphics.lineBetween(0, -15, 0, 35);
        graphics.fillStyle(color, 0.9).fillCircle(0, 10, 5);
        break;
      case 'guardian-barracks':
        graphics.fillStyle(STRUCTURE, 1);
        graphics.fillPoints(
          [
            new Phaser.Math.Vector2(0, -52),
            new Phaser.Math.Vector2(39, -31),
            new Phaser.Math.Vector2(32, 19),
            new Phaser.Math.Vector2(0, 55),
            new Phaser.Math.Vector2(-32, 19),
            new Phaser.Math.Vector2(-39, -31),
          ],
          true,
        );
        graphics
          .lineStyle(3, color, 0.92)
          .strokePoints(
            [
              new Phaser.Math.Vector2(0, -52),
              new Phaser.Math.Vector2(39, -31),
              new Phaser.Math.Vector2(32, 19),
              new Phaser.Math.Vector2(0, 55),
              new Phaser.Math.Vector2(-32, 19),
              new Phaser.Math.Vector2(-39, -31),
            ],
            true,
          );
        graphics.lineStyle(5, color, 0.85).lineBetween(-16, 2, -3, 17);
        graphics.lineBetween(-3, 17, 21, -15);
        break;
      case 'control-spire':
        graphics.fillTriangle(-35, 53, 0, -62, 35, 53);
        graphics.lineStyle(3, color, 0.92);
        graphics.lineBetween(-35, 53, 0, -62);
        graphics.lineBetween(0, -62, 35, 53);
        graphics.lineBetween(-35, 53, 35, 53);
        graphics.fillStyle(LAND_DARK).fillCircle(0, -12, 17);
        graphics.lineStyle(3, color, 0.92).strokeCircle(0, -12, 17);
        graphics.fillStyle(color, 0.9).fillCircle(0, -12, 5);
        graphics.lineStyle(2, color, 0.55).strokeCircle(0, -12, 29);
        break;
      default:
        graphics.fillRect(-42, 5, 84, 48).strokeRect(-42, 5, 84, 48);
        graphics.fillTriangle(-42, 5, -22, -22, -4, 5);
        graphics.fillTriangle(4, 5, 22, -22, 42, 5);
        graphics.fillStyle(color, 0.95);
        graphics.fillPoints(
          [
            new Phaser.Math.Vector2(0, -58),
            new Phaser.Math.Vector2(18, -29),
            new Phaser.Math.Vector2(7, -4),
            new Phaser.Math.Vector2(-8, -17),
            new Phaser.Math.Vector2(-17, -36),
          ],
          true,
        );
        graphics.fillStyle(0x06101b).fillRect(-9, 30, 18, 23);
        break;
    }

    return graphics;
  }

  private tweenNode(node: Phaser.GameObjects.Container, scale: number): void {
    this.tweens.add({ targets: node, scale, duration: 140, ease: 'Sine.Out' });
  }

  private highlightRegion(index: number): void {
    this.selectedIndex = index;
    this.nodes.forEach((node, nodeIndex) => {
      const selected = nodeIndex === index;
      this.tweens.add({
        targets: node,
        scale: selected ? 1.1 : 1,
        alpha: selected ? 1 : 0.72,
        duration: 180,
        ease: 'Sine.Out',
      });
      this.selectionRings[nodeIndex].setStrokeStyle(
        2,
        COLORS[this.evidence[nodeIndex].tone],
        selected ? 0.8 : 0,
      );
    });
  }
}
