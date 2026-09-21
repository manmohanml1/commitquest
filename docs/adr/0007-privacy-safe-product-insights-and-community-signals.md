# ADR 0007: Privacy-safe product insights and community signals

**Status:** Accepted

## Context

CommitQuest needs to learn whether visitors successfully generate a repository preview, establish a campaign, return to it, and recover from availability failures. It also needs an honest way to show that the product is active once public sharing is safe. Raw activity volume is not engineering quality, and public repository or identity data is sensitive even when a repository itself is public.

The approved v1 platform remains cost-bounded: Vercel Hobby, Render Free, Neon Free, and no paid analytics service. Vercel Web Analytics can provide anonymous traffic insight, but Hobby does not provide the custom event capability needed for the product funnel. OpenTelemetry-compatible instrumentation already serves operational diagnostics; it must not become a hidden product-surveillance dataset.

## Decision

### Product-event foundation

Beginning in v0.6, the Spring Boot application may record a minimised first-party event only after a completed or safely classified outcome. The initial vocabulary is:

- `preview_generated`, `preview_failed`, and `preview_recovered`
- `sign_in_started` and `sign_in_completed`
- `campaign_saved`, `campaign_refreshed`, `campaign_exported`, and `campaign_deleted`
- `vault_unavailable` and `vault_recovered`

The application records an event type, UTC time bucket, anonymous lifecycle key or signed-in internal account reference where strictly required for aggregation, outcome classification, and a bounded non-identifying campaign category. It must not record GitHub OAuth tokens, session tokens, email addresses, GitHub handles, raw repository URLs, raw GitHub payloads, private campaign content, free-form text, or browser fingerprints.

The system rolls events into daily aggregate projections. Owner dashboards query aggregates, not raw event streams. Retention, aggregation schedule, lifecycle-key design, and account-deletion semantics require explicit implementation acceptance criteria before data collection begins.

### Community signals

Public Community Beacon figures begin no earlier than v0.8 and only from delayed, thresholded aggregate projections. They may state aggregate counts such as campaigns forged or repositories explored. A public campaign, featured frontier, or personal badge requires explicit campaign-owner approval and an immediate reversal path.

The product may show explainable personal milestones tied to evidence, such as a release or an approved showcase. It must not publish a global developer leaderboard, activity-volume ranking, opaque quality score, or a ranking that implies one repository owner is a better engineer than another.

Any post-1.0 seasonal activity board requires a separate ADR covering consent, qualification, anti-spam controls, score explanation, moderation, and opt-out behavior.

### Operational telemetry boundary

OpenTelemetry-compatible traces, metrics, and logs remain for service health, latency, errors, database activity, and provider requests. Operational records do not become a source for public counts or behavioral profiles. Product-event collection must not block preview, authentication, save, refresh, export, deletion, or public-demo behavior.

### Cost boundary

Vercel Web Analytics is optional for anonymous page traffic within the Hobby limit. First-party product aggregates use the existing Spring Boot and PostgreSQL path. PostHog, Plausible hosting, session replay, and another paid analytics service are not v1 dependencies.

## Consequences

- The product can measure activation, return, recovery, and campaign use without equating activity with engineering quality.
- Public social proof remains honest and does not leak individual or repository-level activity.
- The private owner dashboard can evolve independently from public Community Beacon presentation.
- Analytics data cannot be treated as a substitute for GitHub evidence, campaign scores, or progression rules.
- The data model and testing burden increase only when v0.6 implementation is explicitly started; this ADR authorizes no runtime collection, vendor configuration, database migration, or deployment.

## Alternatives

- **Global leaderboard:** rejected because it invites gaming, implies an unsupported engineering-quality ranking, and conflicts with the evidence-over-vanity principle.
- **Third-party product analytics as the system of record:** rejected for v1 because it introduces another data processor, paid-plan risk, and an external dependency for core product understanding.
- **Session replay:** rejected because repository, account, and campaign interactions can contain sensitive data.
- **Only Vercel Web Analytics:** rejected because anonymous page views cannot prove the server-confirmed product funnel or persistence outcomes.
