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

CommitQuest is a standalone product that translates trustworthy repository evidence into an understandable RPG campaign. GitHub remains the source of truth; CommitQuest adds a deterministic, explainable projection and never replaces repository workflow tools.

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
- Portfolio integration is a lightweight card/embed linking to a standalone hosted campaign

## Delivery sequence

| Phase     | Product proof                                                 | Infrastructure allowed                                            |
| --------- | ------------------------------------------------------------- | ----------------------------------------------------------------- |
| `0.1–0.2` | Compelling bundled campaign and complete interaction language | Angular, Phaser, versioned fixtures, static hosting               |
| `0.3`     | Ephemeral public-repository preview                           | Java 25/Spring Boot API and server-side GitHub adapter            |
| `0.4–0.5` | Persistent, user-controlled connected campaign                | PostgreSQL, GitHub App, jOOQ, Flyway, authentication              |
| `0.6–0.8` | Reliable live progression and public sharing                  | Webhooks, durable inbox/outbox, SQS, SSE, object storage          |
| `0.9–1.0` | Operable public product                                       | Load/security/recovery hardening, Terraform, production telemetry |

Do not scaffold databases, queues, caches, cloud resources, or placeholder services before their first milestone uses them. Redis is permitted only after measurements demonstrate shared ephemeral coordination is necessary. SQS is the initial hosted queue; Kafka is not part of the first production architecture.

## Technology baseline

### Web

- Node.js 24 LTS and npm workspaces
- Angular standalone components with strict TypeScript
- Signals for local synchronous state; RxJS at asynchronous and streaming boundaries
- Phaser 4 isolated behind a lazy-loaded map adapter
- Semantic HTML and CSS for navigation, dashboards, evidence, and accessible controls
- OpenAPI-generated API client when the backend contract exists
- SSE for server-to-browser progress unless bidirectional communication becomes necessary
- Vitest/Angular unit testing, Playwright for release-critical journeys, Storybook when reusable UI components justify it, and automated accessibility checks

Angular owns routing, focus, keyboard behavior, reduced motion, evidence state, and responsive layout. Phaser renders the world; it does not become the application shell.

### API and worker

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

- PostgreSQL 18 is the transactional source of truth
- Transactional inbox/outbox for webhook deduplication and reliable delivery
- SQS for hosted asynchronous processing
- S3-compatible object storage for immutable generated assets and snapshots
- Redis only when justified by a measured requirement
- AWS ECS Fargate, RDS, ECR, Secrets Manager, and S3 for the planned production deployment
- Terraform for reproducible infrastructure and GitHub Actions for delivery

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

## Decision change process

Write an ADR before changing a difficult-to-reverse decision, including the database, queue, authentication model, hosting provider, public evidence boundary, core framework, module boundaries, or scoring philosophy. An ADR states context, decision, alternatives, consequences, migration, and status. Update this guide only after the ADR is accepted.
