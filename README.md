# CommitQuest

CommitQuest turns real repository work into a playable, evidence-backed engineering campaign. Issues and roadmap items become quests, pull requests become encounters, tests and CI become defenses, and releases unlock chapters.

The product remains separate from GitHub: GitHub is the source of truth, and every verified claim links back to its evidence.

## Current milestone

Version `0.1.0` is the demo-first vertical slice:

- Product landing experience
- Interactive Portfolio Citadel campaign
- Repository regions and evidence detail panel
- Historical encounter and release metrics
- Responsive, keyboard-accessible presentation
- Social sharing artwork
- No authentication, persistence, GitHub credentials, or AI required

## Repository layout

```text
apps/
  web/        Angular application and isolated Phaser map adapter
  api/        Reserved boundary for the Spring Boot modular monolith
docs/         Product, architecture, deployment, testing, and ADR records
packages/     Future generated contracts, design tokens, and shared assets
```

## Local development

```powershell
npm install
npm start
```

## Verification

```powershell
npm run verify
```

## Documentation

- [Canonical project guide](docs/PROJECT_GUIDE.md)
- [Product definition](docs/product/PRODUCT.md)
- [MVP boundary](docs/product/MVP.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Deployment](docs/DEPLOYMENT.md)
- [Testing](docs/TESTING.md)
- [Versioning](docs/VERSIONING.md)
- [Roadmap](ROADMAP.md)
- [Contributing](CONTRIBUTING.md)
- [Security](SECURITY.md)
- [Changelog](CHANGELOG.md)

The canonical guide records the product, stack, sequencing, coding patterns, quality gates, and source-precedence rules established during initial planning. Repository records supersede chat history for future decisions.

## Planned architecture

The demo begins as an Angular application with a lazy-loaded Phaser world map. Public repository analysis adds a Java 25 and Spring Boot modular monolith under `apps/api`. Live campaigns add PostgreSQL, an asynchronous webhook worker, a GitHub App, and an immutable reward ledger. Infrastructure is introduced only when the product milestone uses it.

## Product principles

1. Evidence over vanity metrics.
2. Missing data is not failed engineering.
3. Deterministic behavior before AI enhancement.
4. Reward outcomes, never activity volume.
5. Public showcases expose only explicitly approved evidence.
