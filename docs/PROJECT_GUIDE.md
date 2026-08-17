# CommitQuest project guide

This is the canonical index for the product and engineering decisions established during CommitQuest's initial planning. It converts the original conversations into repository-owned guidance so future work does not depend on chat memory.

## Source precedence

When two sources disagree, use this order:

1. Accepted architecture decision records in `docs/adr/`
2. Security and product invariants in `SECURITY.md`, `docs/ARCHITECTURE.md`, and `docs/product/PRODUCT.md`
3. The active milestone and acceptance boundary in `ROADMAP.md` and `docs/product/MVP.md`
4. This guide, `docs/TESTING.md`, `docs/DEPLOYMENT.md`, and `docs/VERSIONING.md`
5. Current implementation and tests
6. Issues, pull requests, and chat history

If implementation contradicts a higher-priority record, either correct the implementation or write an ADR that deliberately changes the decision. Do not silently drift.

## Product commitment

CommitQuest is a standalone evidence-backed engineering progression product presented as an evolving RPG world. GitHub remains the source of truth; CommitQuest adds a deterministic, explainable projection and never replaces repository workflow tools. The game layer makes engineering evidence understandable and motivating, while progression, recommendations, and proof are the lasting product value.

The bundled Portfolio Citadel campaign is the permanent zero-account demonstration and portfolio integration point. It must continue working without GitHub, authentication, a database, AI, or another provider. Later, users can preview a public repository, connect one repository through a minimum-permission GitHub App, and publish a sanitized read-only showcase.

### Primary users

- Individual developers presenting and maintaining real work
- Open-source maintainers and new contributors
- Educators teaching issue-to-release practices
- Small teams wanting a playful delivery overview

### Product principles

- Evidence over vanity metrics
- Missing data is not failed engineering
- Deterministic behavior before AI enhancement
- Reward outcomes, never activity volume
- Explain every verified state and score
- Keep self-reported, inferred, repository-authored, and verified evidence visibly distinct
- Make the public demo resilient to every connected-provider outage

## Stable scope decisions

- One repository per campaign through `1.0.0`
- GitHub first; GitLab and Bitbucket are post-1.0
- Read-only GitHub integration through 1.0; no autonomous issue or PR writes
- No global competitive leaderboard, chat, or multiplayer movement
- AI can explain or suggest but cannot mutate progression and must have a deterministic fallback
- Public showcases use an explicit allowlisted projection; they are never filtered private API responses
- The standalone application owns the complete Phaser experience; the portfolio owns a compact, native live demo backed by a sanitized CommitQuest projection
- Cross-repository mastery, organization analytics, team competition, and billing are post-1.0

## Delivery sequence

| Phase     | Product proof                                                 | Infrastructure allowed                                            |
| --------- | ------------------------------------------------------------- | ----------------------------------------------------------------- |
| `0.1–0.2` | Compelling bundled campaign and complete interaction language | Angular, Phaser, versioned fixtures, static hosting               |
| `0.3`     | Ephemeral public-repository preview                           | Java 25/Spring Boot API, Render Free, and Vercel Hobby            |
| `0.4–0.5` | Persistent, user-controlled connected campaign                | PostgreSQL, GitHub App, jOOQ, Flyway, GitHub identity              |
| `0.6–0.8` | Reliable progression, improvement quests, and public sharing  | Durable webhook inbox/outbox, bounded polling, sanitized API       |
| `0.9–1.0` | Operable evidence-backed progression product                  | Load/security/recovery hardening and cost-bounded telemetry        |

Do not scaffold databases, queues, caches, cloud resources, or placeholder services before their first milestone uses them. No payable resource may be provisioned. ADR 0005 defines the approved zero-cost connected-product path. Redis, Kafka, SQS, and a continuously running worker are not default dependencies.

## Technology baseline

### Web

- Node.js 24 LTS and npm workspaces
- Angular standalone components with strict TypeScript
- Signals for local synchronous state; RxJS at asynchronous and streaming boundaries
- Phaser 4 isolated behind a lazy-loaded map adapter
- Semantic HTML and CSS for navigation, dashboards, evidence, and accessible controls
- OpenAPI-generated API client when the backend contract exists
- Bounded polling or refresh-on-visit for connected progress; SSE only after a measured need and hosting fit
- Vitest/Angular unit testing, Playwright for release-critical journeys, Storybook when reusable UI components justify it, and automated accessibility checks

Angular owns routing, focus, keyboard behavior, reduced motion, evidence state, and responsive layout. Phaser renders the world; it does not become the application shell.

### API and processing

- Java 25 LTS and Gradle Kotlin DSL
- Spring Boot 4 modular monolith with Spring Modulith boundaries
- Spring Security and a minimum-permission GitHub App
- Bean Validation at transport boundaries
- jOOQ for explicit SQL and PostgreSQL access
- Flyway forward-only migrations
- OpenAPI 3.1 as the HTTP contract
- Testcontainers and ArchUnit for integration and architecture tests
- OpenTelemetry for traces, metrics, and structured-log correlation

Code is organized by business capability. Provider DTOs terminate in adapters. Modules communicate through explicit public interfaces and domain events, not cross-module table access.

### Data and hosting

- PostgreSQL is the transactional source of truth; the hosted zero-cost path uses a separate CommitQuest Neon Free project
- Transactional inbox/outbox for webhook deduplication and reliable delivery
- A transport-only Vercel Function may verify and durably store GitHub webhook deliveries while Render is asleep
- Spring Boot owns normalization, progression, and all domain transitions; transport adapters never duplicate domain rules
- A durable queue only after a zero-cost, hard-capped provider is accepted and measured throughput requires it
- S3-compatible object storage only when a shipped generated-asset or snapshot requirement justifies it
- Redis only when justified by a measured requirement
- Vercel Hobby for the static web and one Render Free container for the v0.3 API
- Repository-owned deployment configuration and GitHub Actions for delivery

## Coding patterns

- Pure deterministic functions for mapping, scoring, progression, and evidence classification
- Immutable domain facts and append-only reward/audit records
- Idempotency keys on every webhook-derived transition
- Version fixture schema, mapping algorithm, scoring ruleset, event schema, API, database migration, configuration, and prompt independently
- Model failed, unavailable, private, not configured, not applicable, and unknown as distinct states
- Prefer ports and adapters at provider boundaries; do not leak GitHub types into campaign types
- Use expand-and-contract database changes and backward-compatible events
- Record historical rewards with the ruleset that created them

## Quality and release contract

Every pull request must:

- explain purpose, scope, risk, and verification
- use a Conventional Commit title
- pass formatting, zero-warning lint, tests, accessibility rules, and production build
- include desktop/mobile evidence for material visual changes
- update roadmap, changelog, contracts, and ADRs when affected

Releases use Semantic Versioning and annotated `vMAJOR.MINOR.PATCH` tags. The commit approved in staging is the commit promoted to production. Artifacts are identified by immutable commit SHA; environments are deployment metadata rather than product versions.

Milestone completion, documentation readiness, a merged pull request, and a production deployment do not authorize a tag or GitHub Release. Create either only after an explicit owner instruction naming the release action.

## Decision change process

Write an ADR before changing a difficult-to-reverse decision, including the database, queue, authentication model, hosting provider, public evidence boundary, core framework, module boundaries, or scoring philosophy. An ADR states context, decision, alternatives, consequences, migration, and status. Update this guide only after the ADR is accepted.
