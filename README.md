# CommitQuest

CommitQuest turns real repository work into a playable, evidence-backed engineering campaign. Issues and roadmap items become quests, pull requests become encounters, tests and CI become defenses, and releases unlock chapters.

The product remains separate from GitHub: GitHub is the source of truth, and every verified claim links back to its evidence.

## Current tagged release

Version `0.3.0` accepts public GitHub repositories and generates temporary, evidence-aware campaigns:

- Paste and validate one public GitHub repository URL
- Fetch GitHub evidence only through a Java 25/Spring Boot 4 API
- Return a deterministic, independently versioned campaign projection
- Keep GitHub credentials and provider payloads out of the browser and domain model
- Model invalid, unavailable, private, missing, and rate-limited repositories distinctly
- Perform no authentication, persistence, background processing, GitHub App installation, or AI work
- Keep the bundled Portfolio Citadel demo available when GitHub or the API is unavailable

See the [0.3.0 milestone contract](docs/milestones/0.3.0.md) for its verified acceptance criteria and exclusions.

## 0.4.0 release candidate

The hosted stack is constrained to zero-cost resources: Vercel Hobby for the static web, one explicitly pinned Render Free API service, and a dedicated Neon Free PostgreSQL project. Free-tier cold starts are expected and handled as a visible, recoverable application state.

The production-verified, still-untagged `0.4.0` release candidate separates GitHub identity from later repository authorization and includes durable owner-scoped storage, secure OAuth/session transport, authenticated saved-campaign APIs, and an Angular private-vault interface. Connected controls keep free-host and OAuth wake-up inside CommitQuest, distinguish quick same-account reconnect from explicit GitHub account selection, and report genuinely absent configuration honestly; the bundled campaign and public preview remain independent. See the [0.4.0 milestone contract](docs/milestones/0.4.0.md) and [ADR 0006](docs/adr/0006-github-identity-and-saved-campaign-ownership.md).

## Repository layout

```text
apps/
  web/        Angular application and isolated Phaser map adapter
  api/        Java 25/Spring Boot public-repository preview API
docs/         Product, architecture, deployment, testing, and ADR records
packages/     Future generated contracts, design tokens, and shared assets
```

## Local development

```powershell
npm install
# terminal 1
cd apps/api
./gradlew.bat bootRun

# terminal 2, from the repository root
npm start
```

The Angular development server proxies `/api` to the API on port `8081`. A server-side
`COMMITQUEST_GITHUB_TOKEN` is optional for public repositories and increases the provider rate limit.

## Verification

```powershell
npm run verify
cd apps/api
./gradlew.bat check
```

## Documentation

- [Canonical project guide](docs/PROJECT_GUIDE.md)
- [Product definition](docs/product/PRODUCT.md)
- [MVP boundary](docs/product/MVP.md)
- [Architecture](docs/ARCHITECTURE.md)
- [World map and progression](docs/product/WORLD_MAP.md)
- [Product strategy and return loop](docs/product/STRATEGY.md)
- [Portfolio live-demo integration](docs/product/PORTFOLIO_INTEGRATION.md)
- [Cost model and hosting alternatives](docs/COST_MODEL.md)
- [Component and regression contracts](docs/engineering/COMPONENT_CONTRACTS.md)
- [Deployment](docs/DEPLOYMENT.md)
- [Testing](docs/TESTING.md)
- [Versioning](docs/VERSIONING.md)
- [0.2.0 milestone contract](docs/milestones/0.2.0.md)
- [0.3.0 milestone contract](docs/milestones/0.3.0.md)
- [0.4.0 milestone contract](docs/milestones/0.4.0.md)
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
