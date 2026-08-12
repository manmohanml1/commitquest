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

### 0.4–1.0: connected campaigns

The production system uses:

- Angular web application with an isolated Phaser map adapter
- Java 25 and Spring Boot 4 modular monolith API
- Separate worker runtime from the same backend codebase
- PostgreSQL transactional inbox, outbox, projections, and reward ledger
- A durable queue only after a hard-capped zero-cost provider is accepted
- Redis only when shared ephemeral coordination is demonstrated necessary
- GitHub App with minimum read permissions
- OpenTelemetry across API, worker, database, and outbound GitHub requests

Later connected infrastructure remains an architectural design, not an authorized hosted resource. Under ADR 0004, it stays local-only until every selected provider has a hard zero-spend cap.

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

## Processing model

```text
GitHub webhook
  -> signature verification
  -> durable inbox keyed by X-GitHub-Delivery
  -> immediate 2xx response
  -> asynchronous normalization
  -> deterministic domain transition
  -> transactional outbox
  -> campaign projection and live update
```

## Invariants

- GitHub remains the source of truth for verified evidence.
- One evidence outcome can create at most one reward.
- Historical rewards record their scoring ruleset.
- Public showcases use an allowlisted projection, not filtered private responses.
- AI output cannot mutate state and always has a deterministic fallback.
- Mapping, evidence, fixture, configuration, scoring, and prompt versions are recorded independently.
