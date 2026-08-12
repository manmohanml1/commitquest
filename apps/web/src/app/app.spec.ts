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
    buttons[1].click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.evidence-panel h3')?.textContent).toContain(
      'Neon Vault',
    );
    expect(
      (fixture.nativeElement as HTMLElement).querySelector<HTMLAnchorElement>('.evidence-panel a')
        ?.href,
    ).toContain('/pull/6');
    expect(buttons[1].getAttribute('aria-pressed')).toBe('true');
  });
});
