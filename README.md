# CommitQuest

CommitQuest turns real repository work into a playable, evidence-backed engineering campaign. Issues and roadmap items become quests, pull requests become encounters, tests and CI become defenses, and releases unlock chapters.

The product remains separate from GitHub: GitHub is the source of truth, and every verified claim links back to its evidence.

## Current release

Version `0.3.0` accepts public GitHub repositories and generates temporary, evidence-aware campaigns:

- Paste and validate one public GitHub repository URL
- Fetch GitHub evidence only through a Java 25/Spring Boot 4 API
- Return a deterministic, independently versioned campaign projection
- Keep GitHub credentials and provider payloads out of the browser and domain model
- Model invalid, unavailable, private, missing, and rate-limited repositories distinctly
- Perform no authentication, persistence, background processing, GitHub App installation, or AI work
- Keep the bundled Portfolio Citadel demo available when GitHub or the API is unavailable

See the [0.3.0 milestone contract](docs/milestones/0.3.0.md) for its verified acceptance criteria and exclusions.

The hosted v0.3 stack is constrained to zero-cost resources: Vercel Hobby for the static web and one explicitly pinned Render Free API service. Free-tier cold starts are expected; no database or other paid cloud resource is provisioned.

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
- [Deployment](docs/DEPLOYMENT.md)
- [Testing](docs/TESTING.md)
- [Versioning](docs/VERSIONING.md)
- [0.2.0 milestone contract](docs/milestones/0.2.0.md)
- [0.3.0 milestone contract](docs/milestones/0.3.0.md)
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
