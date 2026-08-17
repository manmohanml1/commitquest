# Cost model and hosting alternatives

Pricing and limits below were reviewed on August 17, 2026. Provider terms change; verify the linked primary pricing pages immediately before enabling billing or moving plans.

## Approved v1 zero-cost target

| Capability | Provider and plan | Expected cost | Failure boundary |
| ---------- | ----------------- | ------------- | ---------------- |
| Angular and Phaser web | Vercel Hobby | `$0` | Personal, non-commercial use; included features pause or reject work at limits |
| Java and Spring Boot API | Render Free | `$0` | Sleeps after 15 idle minutes, no production SLA, 750 shared free instance hours |
| PostgreSQL | Separate Neon Free project | `$0` | 0.5 GB per project, free compute and restore limits, scale to zero |
| Webhook ingress | Thin Vercel Function | `$0` within Hobby limits | Transport only; quota exhaustion delays new events but does not corrupt stored events |
| GitHub integration | GitHub OAuth and GitHub App | `$0` | API rate limits and installation permissions apply |
| CI/CD | GitHub Actions for the public repository | `$0` target | Workflow quotas and provider availability apply |
| Domain | Provider subdomains | `$0` | A custom domain remains optional |

The target monthly infrastructure bill is `$0`. No payment method, automatic overage, paid keep-alive, commercial workload, or unrelated-project resource reuse is part of this contract.

Primary references: [Vercel Hobby](https://vercel.com/docs/plans/hobby), [Render Free](https://render.com/docs/free), and [Neon pricing](https://neon.com/pricing).

## Alternatives

### Cloud Run Java container

Cloud Run is the preferred operational migration from Render. Request-based services with minimum instances set to zero do not charge for idle time. Google publishes a monthly free allowance including two million requests; its pricing example for ten million 400 ms requests at one vCPU, 512 MiB, and concurrency 20 is `$13.69` after the free tier.

At CommitQuest portfolio traffic the likely compute charge is `$0`, but Cloud Run requires a billing-enabled Google Cloud account and adjacent storage, logging, secrets, networking, or configuration mistakes can create charges. A migration therefore requires an owner-approved ADR, a maximum-instance cap, request-based billing, minimum instances zero, rate limits, artifact cleanup, and spend protection.

References: [Cloud Run pricing](https://cloud.google.com/run/pricing), [billing settings](https://docs.cloud.google.com/run/docs/configuring/billing-settings), and [Artifact Registry pricing](https://cloud.google.com/artifact-registry/pricing).

### Predictable always-on container

AWS Lightsail containers currently start at `$7/month` for 512 MB and `$10/month` for 1 GB. Combined with Vercel Hobby and Neon Free, this produces an approximately `$7–10/month` always-on personal deployment before optional domains or overages.

Reference: [Amazon Lightsail pricing](https://aws.amazon.com/lightsail/pricing/).

### Commercial small-product baseline

Vercel Pro currently starts at `$20/month` with `$20` of included usage credit. Neon presents approximately `$15/month` as a typical intermittent 1 GB Launch-plan workload. Adding a `$7–10` always-on container produces a rough `$42–45/month` starting point before usage overages, domains, enhanced observability, backups, support, or taxes.

References: [Vercel pricing](https://vercel.com/pricing) and [Neon pricing](https://neon.com/pricing).

### Cloudflare transport edge

Cloudflare Workers Free currently includes 100,000 requests per day; Workers Paid starts at `$5/month`. It is a viable webhook-ingress alternative if Vercel Functions become unsuitable, but another provider increases secrets, deployment, testing, and incident-response responsibilities.

Reference: [Cloudflare Workers pricing](https://developers.cloudflare.com/workers/platform/pricing/).

### Full AWS serverless

Lambda includes a recurring free allowance and SQS fits the logical inbox/outbox architecture, but API Gateway, logs, database, network, secret, and queue costs are distributed across services and do not create a simple hard-zero guarantee. Full AWS remains a scale and portfolio-story option after product validation, not the default v1 deployment.

Reference: [AWS Lambda pricing](https://aws.amazon.com/lambda/pricing/).

## Cost and availability triggers

Revisit the deployment when one of these is measured:

- cold starts materially prevent successful previews or connected updates;
- free database storage or restore limits threaten deletion, recovery, or audit requirements;
- the product becomes commercial and Vercel Hobby terms no longer apply;
- repeated quota exhaustion affects normal users;
- a security or availability requirement needs paid controls or an SLA;
- background throughput cannot be drained safely through bounded active-session processing.

Do not upgrade preemptively for architecture appearance. Preserve provider-neutral containers, PostgreSQL migrations, OpenAPI contracts, inbox/outbox semantics, and immutable evidence so a measured migration remains straightforward.
