# Deployment

## Milestone 0.1–0.2

The Angular output in `apps/web/dist/commitquest-web/browser` is a static, provider-independent site. Every pull request must pass formatting, linting, unit tests, and a production build before a preview deployment. The Portfolio Citadel fixture has no runtime dependency on GitHub, authentication, a database, or AI.

## Milestone 0.3 deployment boundary

The web and API are independent deployment units:

- **Web:** a new Vercel project named `commitquest-web`, connected only to this repository. It must not reuse the existing portfolio or wearable projects.
- **API:** one containerized Java 25/Spring Boot 4 Render Free web service. The repository pins `plan: free`; a free, read-only GitHub credential exists only as the non-synced `COMMITQUEST_GITHUB_TOKEN` API secret.

The web remains provider-independent and retains the bundled Portfolio Citadel projection as an outage-safe demonstration. Its API base URL is supplied through environment-specific configuration; no GitHub token or other server secret may be exposed in a `NEXT_PUBLIC_`, `VITE_`, Angular environment file, or browser bundle.

### Vercel web project

Create the project from the repository root because the npm workspace and lockfile live there. The repository-owned `vercel.json` defines the build contract:

- **Production URL:** <https://commitquest-web.vercel.app>
- **Vercel project ID:** `prj_wVva1V8Vp8MC8HUzJJvf3aHkgOGx`
- **Git source:** `manmohanml1/commitquest`

| Setting          | Value                                          |
| ---------------- | ---------------------------------------------- |
| Project name     | `commitquest-web`                              |
| Framework        | Angular                                        |
| Node.js          | 24 LTS                                         |
| Install command  | `npm ci --ignore-scripts --no-audit --no-fund` |
| Build command    | `npm run build`                                |
| Output directory | `apps/web/dist/commitquest-web/browser`        |

Version 0.2 requires no runtime environment variables. Preview deployments are branch-scoped; production deploys from the protected release branch after verification.

### Local v0.3 integration

The API listens on port `8081`; Angular proxies same-origin `/api` requests to it. Local CORS is restricted to the documented `localhost` and `127.0.0.1` Angular ports. `COMMITQUEST_GITHUB_TOKEN` is read only by Spring Boot. It must be a fine-grained, read-only credential for public repositories; it is entered directly in Render, is never committed, and is never copied into Vercel or the browser bundle.

The Vercel project proxies `/api/*` to `commitquest-api-manmohanml1.onrender.com` through the repository-owned rewrite. The Render service sleeps after 15 idle minutes and can take approximately one minute to wake; the bundled Portfolio Citadel campaign remains usable during a cold start or provider outage.

The verified release-candidate resources are:

- **Public application:** <https://commitquest-web.vercel.app>
- **Render Blueprint:** `commitquest-zero-cost` (`exs-d9ubmpijobas73e5hovg`)
- **Render API service:** `commitquest-api-manmohanml1` (`srv-d9ubmvvlk1mc73eeg09g`)
- **Health endpoint:** <https://commitquest-api-manmohanml1.onrender.com/actuator/health>

Production smoke verification covers the health endpoint, a direct repository-preview request, the same request through the Vercel rewrite, desktop browser generation, browser console errors, and mobile horizontal overflow.

### Zero-cost contract

- Vercel stays on Hobby. It caps or pauses included usage instead of billing overages.
- Render is pinned to one `free` web service in `render.yaml`; databases, disks, workers, and paid preview environments are forbidden for v0.3.
- The only allowed Render environment key is the non-synced, server-only `COMMITQUEST_GITHUB_TOKEN`; a GitHub credential does not create a paid service.
- `npm run check:free-hosting` fails if the free-plan pin or API proxy is removed or replaced.
- No AWS, paid database, queue, cache, monitoring, domain, or AI service is provisioned.
- Production is published on provider subdomains, avoiding domain-registration cost.

Render Free is a hobby demonstration tier, not an availability guarantee. No payment method or paid upgrade is required by this repository contract.

## Environments

- **Development:** local Angular server, Spring Boot API, and bundled fallback fixture.
- **Staging:** immutable preview artifact with no production credentials.
- **Production:** the exact approved web artifact on Vercel Hobby plus the stateless API container on Render Free.

Environment names are deployment metadata and never part of the product version.

## Later milestones

Connected campaigns remain local-only until a later ADR selects persistence and asynchronous infrastructure with hard zero-spend limits. No paid cloud resource may be introduced implicitly.

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
