# ADR 0005: Zero-cost connected-product architecture

**Status:** Accepted

## Context

Version 0.3 proved that bounded GitHub evidence can produce an honest ephemeral campaign. Versions 0.4–1.0 add user-controlled persistence, a GitHub App, durable webhook ingestion, progression, and public sharing. ADR 0002 originally made AWS, SQS, SSE, and Terraform part of the production baseline, while ADR 0004 later required deployed CommitQuest resources to remain free and unable to create automatic overage charges.

The connected product must preserve the Angular, Phaser, Java, Spring Boot, PostgreSQL, and deterministic-domain decisions without introducing infrastructure theater or silently converting a portfolio project into a payable service.

## Decision

Use the following initial connected-product deployment:

- Vercel Hobby serves the static Angular application and same-origin transport adapters.
- Render Free continues hosting the Java 25/Spring Boot modular monolith.
- A separate CommitQuest Neon Free PostgreSQL project becomes the transactional source of truth when v0.4 first needs persistence.
- GitHub sign-in supplies user identity; a minimum-permission GitHub App supplies repository access.
- A thin Vercel Function may receive GitHub webhooks, verify the signature, insert the immutable delivery into the PostgreSQL inbox, and return immediately.
- Spring Boot alone normalizes provider events, applies progression rules, writes the audit/reward ledger, and updates projections.
- While no continuously running free worker exists, Spring Boot drains bounded inbox work during an active campaign poll, refresh, or explicit reconciliation request. Unprocessed events remain durable.
- GitHub reconciliation repairs missed or incomplete event history and remains a correctness mechanism, not an exceptional migration tool.
- Client updates use bounded polling or refresh-on-visit. SSE is optional after a measured need and hosting fit.
- SQS, Redis, a continuously running worker, object storage, and paid observability are not v1 requirements.

No provider secret crosses its required boundary. The webhook secret exists only at webhook ingress. GitHub App private material remains server-side. The browser receives only an explicit sanitized projection.

## Cost controls

- Every hosted provider stays on a free plan that stops or rejects work instead of automatically charging.
- CommitQuest uses a separate database project and deployment identity rather than reusing the owner's portfolio or wearable resources.
- Repository reads, webhook payload size, processing batches, polling, campaign storage, and public API requests are bounded and rate-limited.
- No keep-alive traffic is used to defeat free-tier sleeping.
- Paid resources, billing-enabled migration, or commercial hosting require a new owner-approved ADR and deployment action.

## Migration path

Cloud Run is the preferred Java-container migration target when Render cold starts, availability, webhook processing latency, commercial use, or measured traffic makes the free host unsuitable. A migration keeps request-based billing, minimum instances zero, a small maximum-instance cap, concurrency limits, and explicit spend controls unless an owner-approved availability requirement changes them.

AWS Lambda/SQS or an always-on container remains a valid later scale path. It is not required to call the v1 architecture production-style: the domain already uses idempotency, inbox/outbox, immutable evidence, modular boundaries, and provider-neutral contracts.

## Consequences

- The product retains a substantial Angular and Java architecture without a rewrite to TypeScript functions.
- Webhook receipt remains fast and durable even while Render sleeps.
- Progression may update on the next active poll rather than continuously in the background.
- Free tiers provide no availability SLA and may change limits.
- Neon storage and restore limits require retention policies, export, deletion, and rebuild-from-GitHub procedures.
- Multi-provider operations are documented and tested, but the number of services remains deliberately small.

## Superseded decisions

This ADR supersedes the mandatory hosted SQS, SSE, AWS, and Terraform portions of ADR 0002 and extends ADR 0004 beyond v0.3. It does not supersede the modular monolith, PostgreSQL, jOOQ, Flyway, inbox/outbox, OpenAPI, OpenTelemetry-compatible instrumentation, or GitHub Actions decisions.
