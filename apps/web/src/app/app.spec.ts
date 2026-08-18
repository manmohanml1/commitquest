import { TestBed } from '@angular/core/testing';
import { ComponentFixture } from '@angular/core/testing';
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

  function renderWithConnectedModeUnavailable(): ComponentFixture<App> {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    http.expectOne('/api/v1/session').flush(null, { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();
    return fixture;
  }

  it('renders the Portfolio Citadel evidence experience', () => {
    const fixture = renderWithConnectedModeUnavailable();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Your repository');
    expect(element.textContent).toContain('Portfolio Citadel');
    expect(element.textContent).toContain('MAPPING MODEL V2');
    expect(element.textContent).toContain('progression scoring and unlocks begin');
    expect(element.textContent).toContain('13');
    expect(element.textContent).toContain('GitHub activity, translated');
  });

  it('switches the evidence panel through accessible controls', () => {
    const fixture = renderWithConnectedModeUnavailable();
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
    const fixture = renderWithConnectedModeUnavailable();
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
    const fixture = renderWithConnectedModeUnavailable();
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
    const fixture = renderWithConnectedModeUnavailable();
    const element = fixture.nativeElement as HTMLElement;
    const input = element.querySelector<HTMLInputElement>('#repository-url');
    const form = element.querySelector<HTMLFormElement>('.preview-form');
    const preview: CampaignProjection = {
      ...PORTFOLIO_CITADEL,
      schemaVersion: 3,
      mappingAlgorithmVersion: 6,
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
    fixture.detectChanges();
    expect(element.querySelector('.campaign-transformation')?.textContent).toContain(
      'READING SIGNALS',
    );
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
    expect(element.querySelector('.campaign-transformation')).toBeNull();
  });

  it('explains absent repository evidence instead of rendering blank ledgers', async () => {
    const fixture = renderWithConnectedModeUnavailable();
    const element = fixture.nativeElement as HTMLElement;
    const preview: CampaignProjection = {
      ...PORTFOLIO_CITADEL,
      schemaVersion: 3,
      mappingAlgorithmVersion: 6,
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
    const fixture = renderWithConnectedModeUnavailable();
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

  it('keeps the private vault honest when connected services are unavailable', async () => {
    const fixture = renderWithConnectedModeUnavailable();
    await fixture.whenStable();
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('#campaign-vault')?.textContent).toContain(
      'Not enabled in this environment',
    );
    expect(element.querySelector('#campaign-vault')?.textContent).toContain(
      'Public previews still work',
    );
    expect(element.querySelector<HTMLButtonElement>('.vault-session button')?.disabled).toBe(true);
  });

  it('shows GitHub sign-in when connected mode is available without a session', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    http
      .expectOne('/api/v1/session')
      .flush({ code: 'UNAUTHENTICATED' }, { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();
    fixture.detectChanges();

    const link = (fixture.nativeElement as HTMLElement).querySelector<HTMLAnchorElement>(
      '.vault-session a',
    );
    expect(link?.textContent).toContain('Sign in with GitHub');
    expect(link?.getAttribute('href')).toBe('/api/v1/auth/github?returnPath=%2F%23campaign-vault');
  });

  it('loads the owner library and saves the current campaign privately', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    http.expectOne('/api/v1/session').flush({
      githubLogin: 'octocat',
      displayName: 'The Octocat',
      avatarUrl: 'https://avatars.example/octocat',
      expiresAt: '2026-08-24T12:00:00Z',
      csrfToken: 'csrf-token',
    });
    await Promise.resolve();
    http.expectOne('/api/v1/campaigns').flush([]);
    await fixture.whenStable();
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('#campaign-vault')?.textContent).toContain('The Octocat');
    expect(element.querySelector('#campaign-vault')?.textContent).toContain(
      'No campaigns saved yet',
    );
    element.querySelector<HTMLButtonElement>('.vault-save')?.click();

    const save = http.expectOne('/api/v1/campaigns');
    expect(save.request.method).toBe('POST');
    expect(save.request.headers.get('X-CommitQuest-CSRF')).toBe('csrf-token');
    expect(save.request.body).toEqual({
      repositoryUrl: 'https://github.com/manmohanml1/portfolio-website',
    });
    save.flush({
      id: '00000000-0000-0000-0000-000000000002',
      projection: PORTFOLIO_CITADEL,
      visibility: 'private',
      createdAt: '2026-08-17T12:00:00Z',
      updatedAt: '2026-08-17T12:00:00Z',
    });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(element.querySelector('#campaign-vault')?.textContent).toContain(
      'Portfolio Citadel is saved privately',
    );
    expect(element.querySelector('.vault-card')?.textContent).toContain(
      'manmohanml1/portfolio-website',
    );
  });

  it('returns an expired session to the signed-out vault without losing the public campaign', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    http.expectOne('/api/v1/session').flush({
      githubLogin: 'octocat',
      displayName: 'The Octocat',
      avatarUrl: 'https://avatars.example/octocat',
      expiresAt: '2026-08-24T12:00:00Z',
      csrfToken: 'csrf-token',
    });
    await Promise.resolve();
    http.expectOne('/api/v1/campaigns').flush([]);
    await fixture.whenStable();
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('.vault-save')?.click();
    http
      .expectOne('/api/v1/campaigns')
      .flush({ code: 'UNAUTHENTICATED' }, { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(element.querySelector('#campaign-vault')?.textContent).toContain('Your session expired');
    expect(element.querySelector('.vault-session a')?.textContent).toContain('Sign in with GitHub');
    expect(element.querySelector('.campaign-section h2')?.textContent).toContain(
      'Portfolio Citadel',
    );
  });

  it('operates saved campaigns through refresh, visibility, and confirmed deletion controls', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    http.expectOne('/api/v1/session').flush({
      githubLogin: 'octocat',
      displayName: 'The Octocat',
      avatarUrl: 'https://avatars.example/octocat',
      expiresAt: '2026-08-24T12:00:00Z',
      csrfToken: 'csrf-token',
    });
    await Promise.resolve();
    const saved = {
      id: '00000000-0000-0000-0000-000000000002',
      projection: PORTFOLIO_CITADEL,
      visibility: 'private',
      createdAt: '2026-08-17T12:00:00Z',
      updatedAt: '2026-08-17T12:00:00Z',
    } as const;
    http.expectOne('/api/v1/campaigns').flush([saved]);
    await fixture.whenStable();
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    const action = (label: string) =>
      [...element.querySelectorAll<HTMLButtonElement>('.vault-card-actions button')].find(
        (button) => button.textContent?.trim() === label,
      );

    action('Refresh')?.click();
    const refresh = http.expectOne(`/api/v1/campaigns/${saved.id}/refresh`);
    expect(refresh.request.method).toBe('POST');
    expect(refresh.request.headers.get('X-CommitQuest-CSRF')).toBe('csrf-token');
    refresh.flush({ ...saved, updatedAt: '2026-08-17T13:00:00Z' });
    await fixture.whenStable();
    fixture.detectChanges();

    action('Make unlisted')?.click();
    const visibility = http.expectOne(`/api/v1/campaigns/${saved.id}/visibility`);
    expect(visibility.request.method).toBe('PATCH');
    expect(visibility.request.body).toEqual({ visibility: 'UNLISTED' });
    visibility.flush({ ...saved, visibility: 'unlisted' });
    await fixture.whenStable();
    fixture.detectChanges();

    action('Delete')?.click();
    fixture.detectChanges();
    expect(element.querySelector('.delete-confirmation')?.textContent).toContain(
      'Remove permanently?',
    );
    action('Yes, remove')?.click();
    const deletion = http.expectOne(`/api/v1/campaigns/${saved.id}`);
    expect(deletion.request.method).toBe('DELETE');
    deletion.flush(null, { status: 204, statusText: 'No Content' });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(element.querySelector('.vault-card')).toBeNull();
  });

  it('requires confirmation before deleting the account and all imported data', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    http.expectOne('/api/v1/session').flush({
      githubLogin: 'octocat',
      displayName: 'The Octocat',
      avatarUrl: 'https://avatars.example/octocat',
      expiresAt: '2026-08-24T12:00:00Z',
      csrfToken: 'csrf-token',
    });
    await Promise.resolve();
    http.expectOne('/api/v1/campaigns').flush([]);
    await fixture.whenStable();
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('.vault-session .danger-action')?.click();
    fixture.detectChanges();

    expect(element.querySelector('.account-delete-confirmation')?.textContent).toContain(
      'Delete account and all imported data?',
    );
    [...element.querySelectorAll<HTMLButtonElement>('.account-delete-confirmation button')]
      .find((button) => button.textContent?.trim() === 'Yes, delete account data')
      ?.click();

    const deletion = http.expectOne('/api/v1/account');
    expect(deletion.request.method).toBe('DELETE');
    expect(deletion.request.headers.get('X-CommitQuest-CSRF')).toBe('csrf-token');
    deletion.flush(null, { status: 204, statusText: 'No Content' });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(element.querySelector('.vault-session a')?.textContent).toContain('Sign in with GitHub');
    expect(element.querySelector('.account-delete-confirmation')).toBeNull();
    expect(element.querySelector('.vault-feedback')?.textContent).toContain('permanently deleted');
  });
});
