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

The Vercel project proxies `/api/*` to `commitquest-api-manmohanml1.onrender.com` through the repository-owned rewrite. The Render service sleeps after 15 idle minutes. A cold start commonly takes approximately one minute and has exceeded two minutes during production verification; the bundled Portfolio Citadel campaign remains usable during a cold start or provider outage.

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

ADR 0005 defines the connected-product zero-cost path. A transport-only Vercel Function may verify and store GitHub webhook deliveries when v0.6 begins. Render remains the Java host until measured availability needs justify a separately approved Cloud Run migration. No paid cloud resource may be introduced implicitly.

The unreleased v0.4 API contains an opt-in persistence mode controlled by `COMMITQUEST_PERSISTENCE_ENABLED`. It additionally requires a JDBC PostgreSQL URL, username, password, and a connection-pool size between 1 and 10. The default remains disabled. Enabling connected mode with partial configuration fails startup instead of silently falling back to ephemeral behavior.

Identity transport is separately gated by `COMMITQUEST_IDENTITY_ENABLED` and requires the GitHub OAuth client ID and secret, a Base64-encoded HMAC secret containing at least 32 random bytes, and the public web base URL. Identity cannot start without the persistence adapters. Production uses the same-origin web URL as the OAuth callback base; only loopback development may use HTTP. Registering an OAuth App or placing these secrets in Render remains an explicit external deployment action.

### Staged v0.4 connected resources

The following free-tier resources were explicitly approved and configured on 2026-08-17. They are staged for verification; saving their configuration did not deploy the unreleased v0.4 code.

- **Database:** dedicated Neon Free project `commitquest-db` in `iad1` (Washington, D.C.). Neon Auth is disabled because CommitQuest owns its GitHub OAuth and session boundary.
- **Vercel connection:** the database integration is attached only to the `commitquest-web` Development environment with the `COMMITQUEST_DATABASE` prefix. It is not attached to Preview or Production.
- **Render database configuration:** persistence enablement, JDBC credentials, and a pool size of three are stored as server-only environment variables on `commitquest-api-manmohanml1`.
- **GitHub identity:** a dedicated `CommitQuest` OAuth App uses `https://commitquest-web.vercel.app` as its homepage and `https://commitquest-web.vercel.app/api/v1/auth/github/callback` as its exact callback URL. Device Flow is disabled and the application requests no scopes.
- **Render identity configuration:** identity enablement, OAuth credentials, a generated HMAC secret, and the public base URL are stored as server-only environment variables.
- **Secret handling:** no database credential, OAuth secret, token, or HMAC material is committed, copied into Vercel browser variables, or documented here. The initially exposed OAuth client secret was rotated and deleted before use.

Render configuration was saved with **Save only**. The running production service remains on the previously deployed v0.3 code until a separate commit, review, merge, and deployment are authorized. The v0.4 connected journey therefore cannot be considered production-verified yet.

### v0.4 preview verification

Pull request #17 was verified on 2026-08-17 without changing production:

- Vercel built the exact branch commit as an immutable web preview.
- A temporary Render Free service ran the same branch API against the dedicated Neon Free database.
- The signed-in journey covered save, return, explicit refresh, visibility, export, campaign deletion, logout, permanent account-data deletion, and reauthentication with an empty vault.
- The OAuth callback was temporarily pointed at loopback only for same-origin browser QA, then restored to the exact production callback.
- The temporary Render service was deleted after verification; its service, resources, and deploy hook no longer exist.
- Production Render database credentials were corrected and saved without triggering a deployment. Production remains on v0.3 until owner approval.

This isolated preview is acceptance evidence for the implementation, not production verification or release authorization.

## Promotion contract

```text
Pull request checks
  -> immutable build tagged by commit SHA
  -> staging deployment
  -> smoke verification
  -> production approval
  -> promote the identical artifact
  -> production smoke verification
  -> explicit owner approval for an annotated Git tag and GitHub Release
```

The promotion and verification steps can finish without creating a tag or GitHub Release. Those external release actions are never inferred from milestone completion.

Database changes use forward-only Flyway migrations and expand-and-contract compatibility. A failed provider integration must not make the bundled public demo unavailable.
