import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { App } from './app';
import { provideApi } from './api/generated/provide-api';
import { PORTFOLIO_CITADEL } from './data/portfolio-citadel.fixture';
import { CampaignProjection } from './domain/campaign';

describe('App', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideApi('')],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

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
    expect(element.textContent).toContain('OPEN ISSUES · ROADMAP ITEMS · RECOMMENDED NEXT STEP');
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

  it('submits a public repository and renders its ephemeral campaign', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    const input = element.querySelector<HTMLInputElement>('#repository-url');
    const form = element.querySelector<HTMLFormElement>('.preview-form');
    const preview: CampaignProjection = {
      ...PORTFOLIO_CITADEL,
      schemaVersion: 3,
      mappingAlgorithmVersion: 5,
      mode: 'foundation',
      repository: 'openai/openai-java',
      slug: 'openai-openai-java',
      title: 'Openai Java Frontier',
    };

    if (!input || !form) throw new Error('Preview form is missing');
    input.value = 'https://github.com/openai/openai-java';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));

    const request = http.expectOne('/api/v1/repository-previews');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      repositoryUrl: 'https://github.com/openai/openai-java',
    });
    request.flush(preview);
    await fixture.whenStable();
    fixture.detectChanges();

    expect(element.querySelector('.campaign-section h2')?.textContent).toContain(
      'Openai Java Frontier',
    );
    expect(element.querySelector('.preview-feedback')?.textContent).toContain(
      'No preview data was persisted',
    );
    expect(element.textContent).toContain('Restore Portfolio Citadel');
    expect(element.textContent).toContain('Foundation');
  });

  it('explains absent repository evidence instead of rendering blank ledgers', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    const preview: CampaignProjection = {
      ...PORTFOLIO_CITADEL,
      schemaVersion: 3,
      mappingAlgorithmVersion: 5,
      mode: 'foundation',
      quests: [],
      encounters: [],
      chapters: [],
    };

    element
      .querySelector<HTMLFormElement>('.preview-form')
      ?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    http.expectOne('/api/v1/repository-previews').flush(preview);
    await fixture.whenStable();

    for (const tab of ['quests', 'encounters', 'chapters']) {
      element.querySelector<HTMLButtonElement>(`#campaign-tab-${tab}`)?.click();
      fixture.detectChanges();
    }

    expect(element.textContent).toContain(
      'No candidate or recommendation could be projected from the available repository evidence',
    );
    expect(element.textContent).toContain('CommitQuest does not invent an encounter');
  });

  it('keeps Portfolio Citadel usable when live preview fails', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    element
      .querySelector<HTMLFormElement>('.preview-form')
      ?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));

    http.expectOne('/api/v1/repository-previews').flush(
      {
        code: 'RATE_LIMITED',
        detail: "GitHub's public API rate limit is temporarily exhausted. Try again later.",
      },
      { status: 429, statusText: 'Too Many Requests' },
    );
    await fixture.whenStable();
    fixture.detectChanges();

    expect(element.querySelector('.campaign-section h2')?.textContent).toContain(
      'Portfolio Citadel',
    );
    expect(element.querySelector('.preview-feedback')?.textContent).toContain(
      'rate limit is temporarily exhausted',
    );
  });
});
