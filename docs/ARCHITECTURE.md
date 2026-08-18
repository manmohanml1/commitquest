# Architecture

## Evolution

## Repository boundaries

```text
apps/web       Angular product UI and Phaser adapter
apps/api       Spring Boot API and worker entry points beginning in v0.3
packages       Generated contracts, design tokens, and reusable assets
docs           Product, ADR, operations, deployment, and testing records
infrastructure Terraform and local container definitions when first required
```

### 0.1–0.2: demo application

The Angular application contains a bundled, sanitized Portfolio Citadel fixture. Phaser is lazy-loaded only for the map surface; Angular owns navigation, accessible controls, evidence state, and product UI. The demo has no credentials, persistence, or provider dependency and remains usable during external outages.

### 0.3: public repository preview

A Spring Boot API becomes the sole GitHub API boundary. It validates repository URLs, manages rate limits, normalizes evidence, and returns a versioned campaign projection. The browser never receives a GitHub credential.

Projection schema v3 and mapping algorithm v6 classify each request as a full, history, foundation, or archive campaign and compose a concise deterministic campaign name. Open issues remain verified candidates; roadmap entries are repository-authored candidates; merged pull requests are verified encounters; commit-only history is inferred; published Releases, tags, and default-branch foundations remain visibly distinct. Repositories without a GitHub description use a bounded README introduction and then verified repository facts as deterministic fallbacks. A bounded ten-minute in-memory cache reduces provider traffic without introducing persistence or a paid service.

The hero artwork is a campaign crest, not the world map and not a score. The actual world map is the lazy-loaded Phaser surface backed by six accessible Angular region controls. See [World map and progression](product/WORLD_MAP.md).

### 0.4–1.0: connected campaigns

The connected-product system uses:

- Angular web application with an isolated Phaser map adapter
- Java 25 and Spring Boot 4 modular monolith API
- PostgreSQL transactional inbox, outbox, projections, and reward ledger
- A thin transport-only webhook receiver that verifies GitHub signatures and commits deliveries quickly
- Request-driven or bounded-poll processing from the Spring Boot codebase on the zero-cost deployment
- A separate worker and durable queue only after measured throughput requires them and a cost-safe provider is accepted
- Redis only when shared ephemeral coordination is demonstrated necessary
- GitHub App with minimum read permissions
- OpenTelemetry-compatible instrumentation across the API, database, outbound GitHub requests, and any future worker

ADR 0005 authorizes the connected zero-cost deployment boundary: Vercel Hobby, Render Free, and a separate Neon Free project. Cloud Run remains the preferred migration target when availability or commercial use justifies billing-enabled infrastructure; AWS/SQS remains an optional scale path rather than a v1 requirement.

## Backend modules

```text
identity
github-integration
evidence
campaign
world-map
quest
encounter
progression
health
showcase
audit
```

Modules expose explicit public interfaces. GitHub payloads terminate at the integration adapter and cannot leak into domain types.

Identity and campaign ownership remain separate from repository-provider access. V0.4 GitHub OAuth proves who the user is; v0.5 GitHub App installation later proves which repositories CommitQuest may read. Saved-campaign application services query through owner-scoped ports so an unknown campaign and another user's campaign produce the same result.

V0.4 keeps connected transport behind complete persistence and identity configuration. The Angular application probes the same-origin session endpoint and presents explicit states for free-host wake-up, manual recovery, connected services unavailable, signed out with account intent, signing in, or an authenticated owner library. Ordinary sign-out retains only an in-memory reconnect hint; account deletion makes GitHub's official account picker the primary next action. All mutations carry the session-bound double-submit CSRF value and pass server-side Origin/Referer validation. Public preview transport does not depend on this path.

## Processing model

```text
GitHub webhook
  -> signature verification
  -> durable inbox keyed by X-GitHub-Delivery
  -> immediate 2xx response
  -> Spring Boot normalization on bounded poll, refresh, or future worker
  -> deterministic domain transition
  -> transactional outbox
  -> campaign projection and client refresh
```

## Invariants

- GitHub remains the source of truth for verified evidence.
- One evidence outcome can create at most one reward.
- Historical rewards record their scoring ruleset.
- Public showcases use an allowlisted projection, not filtered private responses.
- AI output cannot mutate state and always has a deterministic fallback.
- Mapping, evidence, fixture, configuration, scoring, and prompt versions are recorded independently.
