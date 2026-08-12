import { TestBed } from '@angular/core/testing';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [App] }).compileComponents();
  });

  it('renders the Portfolio Citadel evidence experience', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Your repository');
    expect(element.textContent).toContain('Portfolio Citadel');
    expect(element.textContent).toContain('13');
    expect(element.textContent).toContain('GitHub activity, translated');
  });

  it('switches the evidence panel through accessible controls', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const buttons = fixture.nativeElement.querySelectorAll(
      '.region-controls button',
    ) as NodeListOf<HTMLButtonElement>;
    expect(buttons).toHaveLength(6);
    expect(buttons[0].getAttribute('aria-label')).toBe(
      'Select Presentation Capital, status Restored',
    );
    buttons[1].click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.map-stage-label strong')?.textContent).toContain(
      'Gateway Keep',
    );
    expect(fixture.nativeElement.querySelector('.evidence-panel h3')?.textContent).toContain(
      'Gateway Keep',
    );
    expect(
      (fixture.nativeElement as HTMLElement).querySelector<HTMLAnchorElement>('.evidence-panel a')
        ?.href,
    ).toContain('/pull/10');
    expect(buttons[1].getAttribute('aria-pressed')).toBe('true');
  });

  it('navigates the complete campaign through accessible tabs', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    const questTab = element.querySelector<HTMLButtonElement>('#campaign-tab-quests');
    questTab?.click();
    fixture.detectChanges();

    expect(questTab?.getAttribute('aria-selected')).toBe('true');
    expect(element.querySelector('#campaign-panel-quests')?.hasAttribute('hidden')).toBe(false);
    expect(element.textContent).toContain('Candidate quest board');
    expect(element.textContent).toContain('REPOSITORY-AUTHORED · NOT YET VERIFIED');
  });

  it('supports arrow-key movement between campaign tabs', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    const mapTab = element.querySelector<HTMLButtonElement>('#campaign-tab-map');
    mapTab?.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowRight', bubbles: true }));
    fixture.detectChanges();

    expect(
      element
        .querySelector<HTMLButtonElement>('#campaign-tab-quests')
        ?.getAttribute('aria-selected'),
    ).toBe('true');
  });
});
