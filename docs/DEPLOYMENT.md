# Deployment

## Milestone 0.1–0.2

The Angular output in `apps/web/dist/commitquest-web/browser` is a static, provider-independent site. Every pull request must pass formatting, linting, unit tests, and a production build before a preview deployment. The Portfolio Citadel fixture has no runtime dependency on GitHub, authentication, a database, or AI.

## Milestone 0.3 deployment boundary

The web and API are independent deployment units:

- **Web:** a new Vercel project named `commitquest-web`, connected only to this repository. It must not reuse the existing portfolio or wearable projects.
- **API:** a containerized Java 25/Spring Boot 4 service on AWS ECS Fargate when the public-repository endpoint is ready to host. GitHub credentials exist only in the API environment.

The web remains provider-independent and retains the bundled Portfolio Citadel projection as an outage-safe demonstration. Its API base URL is supplied through environment-specific configuration; no GitHub token or other server secret may be exposed in a `NEXT_PUBLIC_`, `VITE_`, Angular environment file, or browser bundle.

### Vercel web project

Create the project from the repository root because the npm workspace and lockfile live there. The repository-owned `vercel.json` defines the build contract:

- **Production URL:** <https://commitquest-web.vercel.app>
- **Vercel project ID:** `prj_wVva1V8Vp8MC8HUzJJvf3aHkgOGx`
- **Git source:** `manmohanml1/commitquest`

| Setting | Value |
| --- | --- |
| Project name | `commitquest-web` |
| Framework | Angular |
| Node.js | 24 LTS |
| Install command | `npm ci --ignore-scripts --no-audit --no-fund` |
| Build command | `npm run build` |
| Output directory | `apps/web/dist/commitquest-web/browser` |

Version 0.2 requires no runtime environment variables. Preview deployments are branch-scoped; production deploys from the protected release branch after verification.

### Local v0.3 integration

The API listens on port `8081`; Angular proxies same-origin `/api` requests to it. Local CORS is restricted to the documented `localhost` and `127.0.0.1` Angular ports. The optional `COMMITQUEST_GITHUB_TOKEN` is read only by Spring Boot.

The Vercel web project can deploy the v0.3 interface independently, but live repository generation remains unavailable there until the API is deployed to its new ECS service and the same-origin gateway route is configured. The bundled Portfolio Citadel campaign remains fully usable in that state.

## Environments

- **Development:** local Angular server, Spring Boot API, and bundled fallback fixture.
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
