# Authenticated application shell

## Purpose

Version 0.5 moves signed-in work out of the v0.4 landing-page continuation and into a dedicated application shell. This document is the implementation-ready information architecture and responsive wireframe contract for that shell. It does not authorize visual acceptance, a deployment, or a release.

## V0.4 journey audit

The released application deliberately proved the connected behavior before committing to the permanent signed-in experience. Its strengths must survive the redesign:

- public preview and bundled Portfolio Citadel remain usable without authentication, PostgreSQL, or GitHub availability;
- signed-out, free-host waking, unavailable, authenticated, expired-session, export, and destructive-confirmation states are explicit;
- saved-campaign actions are owner scoped and preserve evidence provenance;
- keyboard, narrow-viewport, reduced-motion, and provider-failure regressions are covered.

The v0.4 shape is not the v0.5 destination:

- one root component currently coordinates the public preview, session recovery, private vault, world map, ledgers, identity modal, and destructive actions;
- signed-in controls continue the marketing page instead of establishing a repeat-use workspace;
- navigation uses document anchors and has no route-level campaign context;
- the private vault is the primary authenticated surface even though the lasting product is the evolving campaign;
- account, connection, campaign selection, and mode preference do not have dedicated locations.

V0.5 changes composition and navigation while preserving the released behavior and transport contracts.

## Route contract

Routes are semantic product locations, not separate implementations per design mode.

| Route | Purpose | Campaign required |
| --- | --- | --- |
| `/` | Public landing page, bundled demonstration, and ephemeral public preview | No |
| `/app` | Resolve the most recent available campaign or show campaign selection | No |
| `/app/campaigns/:campaignId/overview` | Realm identity, evidence state, recent verified change, and recommended next quest | Yes |
| `/app/campaigns/:campaignId/world` | Phaser world plus equivalent semantic region navigator and evidence panel | Yes |
| `/app/campaigns/:campaignId/quests` | Verified issues, repository-authored candidates, and recommendations | Yes |
| `/app/campaigns/:campaignId/chronicle` | Encounters, releases, defenses, and evidence timeline | Yes |
| `/app/vault` | Saved campaigns, save/refresh, visibility, export, and deletion | No |
| `/app/connections` | GitHub App installations, repository selection, permissions, suspension, and revocation | No |
| `/app/account` | GitHub identity, design mode, accessibility preferences, sign-out, and account deletion | No |

The implementation may begin with route-level standalone components behind the existing root bootstrap. The public route must remain independently renderable when the connected API is unavailable. History navigation requires an explicit static-host fallback after `/api/*` proxy precedence; a browser refresh on every route is part of preview acceptance.

### Navigation and state rules

- Opening an authenticated route without a session starts the existing branded gateway preflight and then offers GitHub sign-in with the requested relative route preserved.
- An expired session returns to a signed-out shell recovery state without discarding the current public or in-memory campaign.
- A missing or cross-owner campaign produces the same not-found presentation.
- Switching Modern and Chronicle preserves the route, campaign, focused task where practical, filters, unsaved form values, and scroll context.
- Route titles and primary headings name the campaign and location; mode-specific vocabulary may supplement but never replace the plain-language label.
- Browser back/forward navigation restores application location without repeating a mutation.

## Desktop wireframe

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│ CQ CommitQuest   Campaign: CommitQuest Forge ▾      Modern | Chronicle  User │
├────────────────┬─────────────────────────────────────────────────────────────┤
│ Overview       │ Repository context / connection state / evidence freshness  │
│ World          ├─────────────────────────────────────────────────────────────┤
│ Quest log      │                                                             │
│ Chronicle      │ Current route content                                       │
│                │                                                             │
│ Vault          │ One dominant task, supporting evidence, explicit states     │
│ Connections    │                                                             │
│ Account        │                                                             │
├────────────────┴─────────────────────────────────────────────────────────────┤
│ Source status · projection/mapping/ruleset versions · recovery action        │
└──────────────────────────────────────────────────────────────────────────────┘
```

The campaign switcher stays in the top command bar. Product destinations stay in the primary side navigation. Account and connection controls never compete with the current campaign task.

## Mobile wireframe

```text
┌──────────────────────────────┐
│ CQ  Campaign ▾       Mode ◐  │
├──────────────────────────────┤
│ Route title                  │
│ connection / freshness      │
├──────────────────────────────┤
│                              │
│ Current route content        │
│ single-column reading order  │
│                              │
├──────────────────────────────┤
│ Overview World Quests More   │
└──────────────────────────────┘
```

- At 390 CSS pixels, four frequent destinations fit in the bottom navigation; **More** opens Chronicle, Vault, Connections, and Account as a labelled sheet.
- At 320 CSS pixels, labels remain visible, controls do not horizontally scroll, and the campaign switcher truncates visually without truncating its accessible name.
- Destructive actions remain inside their destination and never appear in bottom navigation or the campaign switcher.
- Chronicle decoration yields to the same content order and tap targets as Modern.

## Location responsibilities

### Overview

Show campaign identity, connection and freshness state, one explainable summary per applicable maturity dimension, the most recent verified change, and exactly one recommended next quest. V0.5 does not calculate a score; unavailable and not-applicable dimensions remain distinct.

### World

Preserve the lazy-loaded Phaser map. The semantic region navigator, selected evidence, loading state, and source link remain available without the canvas. Mode changes map materials and framing, not coordinates, evidence, or unlock state.

### Quest log

Provide separate filters and headings for verified GitHub issues, repository-authored candidates, and CommitQuest recommendations. No combined count may imply that a recommendation is verified work.

### Chronicle

Combine encounters and release chapters into an evidence timeline while retaining their distinct types, sources, and verification language.

### Vault

Keep save, explicit refresh, visibility, export, and deletion behavior. Campaign selection moves to the global switcher; the Vault remains the library and lifecycle-management destination.

### Connections and account

Connections owns GitHub App state and repository grants. Account owns identity, mode, accessibility, sign-out, and permanent data deletion. GitHub OAuth identity and GitHub App authorization remain visibly separate.

## Shared shell states

Every authenticated route must render these states through shared components:

| State | Required presentation |
| --- | --- |
| Gateway waking | Branded bounded progress, public-demo escape, reduced-motion equivalent |
| Signed out | Requested destination retained; quick reconnect and explicit account choice |
| No campaigns | Connect or save a repository; no empty world or invented progress |
| Provider unavailable | Last safe projection where permitted, freshness warning, retry action |
| Installation suspended/revoked | Permission explanation and reconnect path; no false repository absence |
| Session expired | Preserve non-sensitive view state and require reauthentication before mutations |
| Loading/working | Name the operation and prevent duplicate mutation |
| Error | Explain recoverable action without exposing credentials or provider payloads |
| Destructive confirmation | Name exact scope, consequences, cancel action, and irreversible step |

## Component boundary

```text
AppRoot
├── PublicExperience
└── AuthenticatedShell
    ├── CampaignCommandBar
    ├── PrimaryNavigation
    ├── ConnectionStatus
    ├── RouterOutlet
    │   ├── RealmOverview
    │   ├── WorldWorkspace
    │   ├── QuestLog
    │   ├── ChronicleTimeline
    │   ├── CampaignVault
    │   ├── Connections
    │   └── AccountSettings
    └── MobileDestinationSheet
```

Session, campaign library, selected campaign, and mode are shell-level state. Route components request application operations through services; they do not call generated transport clients directly. Phaser remains isolated behind the world adapter.

## Acceptance before implementation

- Owner visually accepts the desktop and mobile hierarchy.
- History-route hosting and OAuth return-path spikes prove refresh, callback, and back/forward behavior.
- The v0.4 public preview, authentication, vault, and destructive-action tests are mapped to their v0.5 component owners before extraction.
- Modern and Chronicle token/component contracts are accepted before visual CSS is written.
