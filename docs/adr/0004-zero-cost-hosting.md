# ADR 0004: Zero-cost hosting constraint

**Status:** Accepted

## Context

CommitQuest is an individual portfolio and open-source project. Its owner requires the deployed project to have no payable infrastructure and no automatic overage charges. The AWS deployment in ADR 0003 would violate that constraint even at low usage.

## Decision

Keep the accepted Angular, Java 25, Spring Boot 4, Phaser, Gradle, OpenAPI, and GitHub Actions application stack. For version 0.3, host only these two resources:

- the static Angular site on Vercel Hobby, whose included usage is capped rather than billed for overage;
- one Docker-based Render Free web service for the Spring Boot API, pinned with `plan: free` in `render.yaml`.

Vercel proxies `/api/*` to the Render service so the browser retains a same-origin contract. No database, disk, worker, queue, cache, paid preview environment, or paid observability service is provisioned. CI verifies the free-plan pin and proxy destination.

This ADR supersedes the AWS ECS hosting decision in ADR 0003 and the AWS resource choices in ADR 0002. It does not replace the application architecture. Later milestones may introduce persistence or asynchronous processing only after a separate ADR identifies a provider with a hard zero-spend cap; otherwise those milestones remain local-only.

## Consequences

- The selected deployment resources cannot generate usage overage charges on their pinned plans.
- Render Free sleeps after 15 idle minutes, so the first request can take approximately one minute to wake.
- Render Free is suitable for a hobby demonstration, not an availability-sensitive commercial production service.
- The API remains stateless and ephemeral, so sleep and filesystem loss do not compromise product data.
- Deployment protection must be disabled for the public production Vercel URL; branch previews may remain protected.
