import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { ConnectedCampaignClient, SavedCampaign } from '../api/connected-campaign.client';
import { IdentityNavigation } from '../api/identity-navigation.service';
import { PORTFOLIO_CITADEL } from '../data/portfolio-citadel.fixture';
import { AuthenticatedShellStore } from './authenticated-shell.store';

describe('AuthenticatedShellStore', () => {
  const session = {
    githubLogin: 'octocat',
    displayName: 'The Octocat',
    avatarUrl: 'https://avatars.example/octocat',
    expiresAt: '2026-09-22T12:00:00Z',
    csrfToken: 'csrf-token',
  };
  const saved: SavedCampaign = {
    id: '00000000-0000-0000-0000-000000000002',
    projection: PORTFOLIO_CITADEL,
    visibility: 'private',
    createdAt: '2026-09-21T12:00:00Z',
    updatedAt: '2026-09-21T12:00:00Z',
  };
  let client: {
    session: ReturnType<typeof vi.fn>;
    list: ReturnType<typeof vi.fn>;
    logout: ReturnType<typeof vi.fn>;
    isSignedOut: ReturnType<typeof vi.fn>;
    isUnavailable: ReturnType<typeof vi.fn>;
    isTransient: ReturnType<typeof vi.fn>;
    message: ReturnType<typeof vi.fn>;
  };
  let navigation: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    client = {
      session: vi.fn(() => of(session)),
      list: vi.fn(() => of([saved])),
      logout: vi.fn(() => of(undefined)),
      isSignedOut: vi.fn(() => false),
      isUnavailable: vi.fn(() => false),
      isTransient: vi.fn(() => false),
      message: vi.fn((_error: unknown, fallback: string) => fallback),
    };
    navigation = { navigate: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        AuthenticatedShellStore,
        { provide: ConnectedCampaignClient, useValue: client },
        { provide: IdentityNavigation, useValue: navigation },
      ],
    });
  });

  it('loads the owner session and selects the first saved campaign', async () => {
    const store = TestBed.inject(AuthenticatedShellStore);

    await store.load();

    expect(store.phase()).toBe('ready');
    expect(store.session()).toEqual(session);
    expect(store.selectedCampaign()).toEqual(saved);
    expect(store.message()).toContain('1 private campaign ready');
  });

  it('keeps signed-out state distinct from unavailable connected services', async () => {
    const signedOut = new Error('signed out');
    client.session.mockReturnValue(throwError(() => signedOut));
    client.isSignedOut.mockImplementation((error: unknown) => error === signedOut);
    const store = TestBed.inject(AuthenticatedShellStore);

    await store.load();

    expect(store.phase()).toBe('signed-out');
    expect(store.message()).toContain('Sign in with GitHub');
  });

  it('preserves only valid campaign selections', async () => {
    const second = { ...saved, id: '00000000-0000-0000-0000-000000000003' };
    client.list.mockReturnValue(of([saved, second]));
    const store = TestBed.inject(AuthenticatedShellStore);
    await store.load();

    store.selectCampaign(second.id);
    expect(store.selectedCampaign()).toEqual(second);
    store.selectCampaign('cross-owner-campaign');
    expect(store.selectedCampaign()).toEqual(second);
  });

  it('keeps the requested app route through either GitHub identity path', () => {
    const store = TestBed.inject(AuthenticatedShellStore);

    store.signIn();
    store.signIn(true);

    expect(navigation.navigate).toHaveBeenNthCalledWith(
      1,
      '/api/v1/auth/github?returnPath=%2Fapp%2Foverview',
    );
    expect(navigation.navigate).toHaveBeenNthCalledWith(
      2,
      '/api/v1/auth/github?returnPath=%2Fapp%2Foverview&selectAccount=true',
    );
  });

  it('clears private shell state after sign out', async () => {
    const store = TestBed.inject(AuthenticatedShellStore);
    await store.load();

    await store.signOut();

    expect(client.logout).toHaveBeenCalledWith('csrf-token');
    expect(store.phase()).toBe('signed-out');
    expect(store.session()).toBeNull();
    expect(store.campaigns()).toEqual([]);
  });
});
