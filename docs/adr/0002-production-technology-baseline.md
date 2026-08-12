# ADR 0002: Production technology baseline

**Status:** Accepted

## Context

CommitQuest must first prove its evidence-to-campaign experience, then grow into a secure connected product without replacing its architecture at every milestone. The stack should highlight the owner's Angular, Java, Spring, AWS, API, and event-driven strengths while remaining maintainable by a small team.

## Decision

Use an npm-workspace monorepo with Angular and an isolated Phaser map adapter for the web application. Add a Java 25/Spring Boot 4 modular monolith at the public-repository milestone. Use PostgreSQL with jOOQ and Flyway for persistence, a transactional inbox/outbox with SQS for hosted asynchronous processing, OpenAPI-generated clients, SSE for progress, OpenTelemetry for observability, Terraform for AWS infrastructure, and GitHub Actions for delivery.

Redis is not a default dependency. Kafka, microservices, Kubernetes, GraphQL, and bidirectional sockets require a demonstrated product or scale need and a separate ADR.

## Consequences

- The early product remains static, inexpensive, and provider-independent.
- Domain boundaries and versioned projections must be maintained before the API exists.
- A modular monolith keeps transactions and operational ownership simple while preserving future extraction seams.
- Explicit SQL, idempotency, observability, and generated contracts become engineering requirements rather than late hardening tasks.
- Infrastructure is introduced only when a shipped milestone exercises it.
