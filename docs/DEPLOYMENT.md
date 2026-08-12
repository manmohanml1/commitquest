# Deployment

## Milestone 0.1–0.2

The Angular output in `apps/web/dist/commitquest-web/browser` is a static, provider-independent site. Every pull request must pass formatting, linting, unit tests, and a production build before a preview deployment. The Portfolio Citadel fixture has no runtime dependency on GitHub, authentication, a database, or AI.

## Environments

- **Development:** local Angular server and bundled fixture.
- **Staging:** immutable preview artifact with no production credentials.
- **Production:** the exact artifact approved in staging.

Environment names are deployment metadata and never part of the product version.

## Later milestones

Public repository analysis adds a containerized Spring Boot API. Connected campaigns add PostgreSQL, SQS, a separately scalable worker, Secrets Manager, and OpenTelemetry. Development, staging, and production use separate GitHub Apps, credentials, and databases.

## Promotion contract

```text
Pull request checks
  -> immutable build tagged by commit SHA
  -> staging deployment
  -> smoke verification
  -> production approval
  -> promote the identical artifact
  -> production smoke verification
  -> annotated Git tag and GitHub Release
```

Database changes use forward-only Flyway migrations and expand-and-contract compatibility. A failed provider integration must not make the bundled public demo unavailable.
