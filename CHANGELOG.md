# Changelog

All notable changes follow Keep a Changelog and Semantic Versioning conventions.

## [Unreleased]

### Added

- Add opt-in, owner-scoped saved-campaign HTTP contracts for create-or-refresh, list, read, explicit refresh, visibility preference, versioned export, and deletion.
- Add an accessible Angular private campaign vault with honest unavailable and signed-out states, GitHub sign-in entry, owner library controls, session-expiry recovery, and destructive confirmation.
- Generate saved-campaign Angular clients from the validated OpenAPI 0.4 contract and add transport/UI regression coverage.
- Began the v0.4 saved-campaign foundation with explicit account ownership, private-by-default visibility, refresh semantics, export, deletion, storage limits, and tenant-safe lookup invariants.
- Accepted the GitHub identity, opaque session, CSRF, PostgreSQL ownership, export, and deletion contract for connected campaigns.
- Added opt-in PostgreSQL persistence with Flyway migrations, jOOQ owner-scoped campaign storage, database-cascade account deletion, bounded connection pooling, and Testcontainers coverage.
- Added opt-in GitHub OAuth identity transport with no requested scopes, one-time state, PKCE, immediate provider-token disposal, rotated opaque sessions, hardened cookies, CSRF/origin enforcement, logout, and account deletion.
- Added a guarded web control for permanent account and imported-campaign deletion.
- Added explicit GitHub account choice through the provider's official account picker, with post-deletion choice and fast ordinary reconnect paths.

### Fixed

- Aligned the OpenAPI campaign mapping constant and generated Angular client with mapping algorithm v6.
- Prevented narrow mobile viewports from clipping hero copy, campaign metadata, navigation, and action controls.
- Recover the private campaign vault automatically from Render Free cold starts and provide an explicit retry state when wake-up exceeds the bounded retry window.
- Replace the cold-start placeholder with a responsive, reduced-motion-aware vault-awakening experience that keeps the public campaign usable while connected services recover.

## [0.3.0] - 2026-08-17

### Added

- Public GitHub repository URL input with accessible loading, result, failure, retry, and bundled-fixture fallback states.
- Java 25/Spring Boot 4 public-repository preview API with bounded GitHub reads and no persistence.
- Deterministic mapping from provider-neutral evidence into six source-linked campaign regions, quests, encounters, chapters, and metrics.
- OpenAPI 3.1 contract validation and generated Angular API client.
- API domain, adapter fixture, application-context, and architecture boundary tests plus a Java 25 CI gate.
- A Java 25 container, Render Free blueprint, Vercel API proxy, and automated zero-cost hosting guard.
- A live zero-cost release candidate at `https://commitquest-web.vercel.app`, backed by the Render Free API.
- Repository-authored `ROADMAP.md` candidate extraction with explicit provenance.
- Full, history, foundation, and archive campaign classification.
- Clearly labelled commit expeditions, tag milestones, and foundation chapters when verified PR or Release evidence is absent.
- A bounded ten-minute in-memory provider cache and a 17-repository compatibility matrix.

### Changed

- Established an independent Git-connected Vercel project and pinned the web build runtime to Node 24.
- Made campaign summary labels and repository evidence links reflect the active ephemeral or bundled campaign.
- Superseded the payable AWS v0.3 deployment with a Vercel Hobby and Render Free hosting contract.
- Upgraded the campaign projection to schema v3 and mapping algorithm v5 so sparse repositories remain actionable without fabricating verified achievements.
- Expanded merged-PR sampling and made capped evidence metrics explicit.
- Advanced the mapping model to v6, composed concise theme-aware campaign names, and added a staged repository-to-world transformation.
- Clarified that the hero illustration is a campaign crest and mapping algorithm versions are not progression scores.
- Added bounded README-derived repository descriptions when GitHub metadata is empty.
- Formalized component ownership, pre-edit baselines, focused regression tests, full affected-application gates, and explicit release-tag approval.
- Adopted evidence-backed engineering progression as the product thesis, refined the v0.4–1.0 return loop, selected a cost-bounded connected architecture, and specified the portfolio-native live demo.

## [0.2.0] - 2026-08-12

### Added

- Canonical project guide and repository contributor instructions derived from initial planning.
- Accepted production-stack ADR.
- Pull-request template, code ownership, dependency automation, release packaging, and stronger CI governance.
- Expanded Portfolio Citadel projection with six regions, candidate quests, verified encounters, and release chapters.
- Keyboard-accessible campaign navigation and explicit history-campaign evidence states.
- Responsive region navigator and fully scaled Phaser overview for readable world-map exploration.
- Distinct landmark architecture, layered terrain, animated beacons, and crisp HTML map labeling for the Portfolio Citadel north-star experience.

## [0.1.0] - 2026-08-11

### Added

- Product, MVP, architecture, versioning, security, and contribution contracts.
- Branded CommitQuest landing experience.
- Interactive, lazy-loaded Phaser Portfolio Citadel map.
- Versioned fixture with exact GitHub primary-evidence links.
- Accessible Angular evidence controls and detail presentation.
- Responsive and reduced-motion behavior.
- Project-specific social sharing artwork.
- Node 24 CI quality gate for formatting, linting, tests, and production builds.
