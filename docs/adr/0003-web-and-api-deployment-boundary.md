# ADR 0003: Web and API deployment boundary

**Status:** Accepted

## Context

Version 0.3 introduces the first server-side provider integration while the released Portfolio Citadel remains a static, provider-independent demonstration. CommitQuest needs its own deployment identity and must not inherit configuration, domains, or credentials from the owner's unrelated portfolio and wearable projects.

## Decision

Deploy the Angular application as a static artifact in a new Vercel project named `commitquest-web`. Build it from the monorepo root with the repository lockfile and `vercel.json` contract.

Deploy the Spring Boot API separately as a container on AWS ECS Fargate when the v0.3 public-repository endpoint is ready. The web receives an environment-specific API base URL. GitHub credentials and provider communication remain exclusively within the API deployment.

Preview and production are separate environments. Delivery identifies artifacts by commit SHA and promotes the approved artifact rather than rebuilding different source. The bundled campaign must remain usable if the API, GitHub, or either deployment provider is unavailable.

## Alternatives considered

- **Reuse an existing Vercel project:** rejected because it would mix unrelated products, domains, build settings, and deployment history.
- **Host the Spring Boot API in Vercel functions:** rejected because the accepted production baseline uses a containerized Java service on AWS and should preserve that operational path.
- **Deploy the web and API together on AWS immediately:** deferred because static Vercel hosting gives the web inexpensive preview deployments while v0.3 validates the API boundary.
- **Provision the full AWS data platform now:** rejected because v0.3 has no persistence, queue, worker, or connected campaign.

## Consequences

- CommitQuest gains an independent web project, deployment history, and future domain boundary.
- Web and API releases can be verified independently but must maintain a versioned HTTP contract.
- Cross-origin policy, API base URL configuration, health checks, timeouts, and correlation become explicit API delivery concerns.
- Vercel contains no GitHub secret and can continue serving the bundled demonstration during backend outages.
- ECS infrastructure is added only when deployable v0.3 API code exists.
