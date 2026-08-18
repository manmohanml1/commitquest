# CommitQuest API

Java 25 and Spring Boot 4 boundary for the ephemeral public-repository preview introduced in milestone `0.3.0`.

## Run locally

```powershell
./gradlew.bat bootRun
```

The service listens on `http://localhost:8081`. A GitHub token is optional for public repositories but recommended to increase the provider rate limit:

```powershell
$env:COMMITQUEST_GITHUB_TOKEN = "..."
./gradlew.bat bootRun
```

The token remains server-side. Preview requests are bounded and are never persisted. Provider-neutral evidence is retained only in a 256-entry, ten-minute in-memory cache to prevent repeated public requests from exhausting GitHub's shared IP limit.

The v0.3 adapter reads bounded repository metadata, root structure, open issues, `ROADMAP.md`, merged pull requests, Releases, tags, workflows, and recent default-branch commits. The mapper preserves whether each result is verified, repository-authored, or inferred.

## Unreleased v0.4 foundation

The API source includes provider-neutral account and saved-campaign domain/application types for the active `0.4.0` milestone. They enforce private defaults, owner-scoped access, refresh-in-place, export, deletion, and a 25-campaign account limit. When connected mode is enabled, authenticated `/api/v1/campaigns` routes expose create-or-refresh, list, read, explicit refresh, private/unlisted preference, versioned export, and deletion without accepting an owner identifier from the client.

PostgreSQL persistence is present but disabled by default. Connected mode requires complete, explicit configuration:

```powershell
$env:COMMITQUEST_PERSISTENCE_ENABLED = "true"
$env:COMMITQUEST_DATABASE_URL = "jdbc:postgresql://localhost:5432/commitquest"
$env:COMMITQUEST_DATABASE_USERNAME = "commitquest"
$env:COMMITQUEST_DATABASE_PASSWORD = "..."
$env:COMMITQUEST_DATABASE_POOL_SIZE = "3"
./gradlew.bat bootRun
```

Enabling persistence runs forward-only Flyway migrations before exposing the jOOQ adapters. Incomplete configuration fails startup. Leaving it disabled creates no `DataSource`, Flyway, jOOQ context, identity controller, or saved-campaign controller, so the released v0.3 startup and deployment behavior remains unchanged. No hosted database is provisioned by this source change.

GitHub identity transport is also opt-in and additionally requires:

```powershell
$env:COMMITQUEST_IDENTITY_ENABLED = "true"
$env:COMMITQUEST_GITHUB_OAUTH_CLIENT_ID = "..."
$env:COMMITQUEST_GITHUB_OAUTH_CLIENT_SECRET = "..."
$env:COMMITQUEST_IDENTITY_HMAC_SECRET = "<Base64-encoded 32+ random bytes>"
$env:COMMITQUEST_PUBLIC_BASE_URL = "http://localhost:4200"
```

Identity mode requires persistence mode. It requests no GitHub OAuth scopes, uses one-time state plus S256 PKCE, discards the access token immediately after loading `/user`, and stores only HMAC digests for CommitQuest session and CSRF tokens. The production base URL must use HTTPS; loopback HTTP is accepted only for local development. No value from this example belongs in source control.

Saved-campaign mutations require the session cookie, matching CSRF cookie/header, and an allowlisted `Origin` or `Referer`. Reads derive ownership from the session. Unknown campaign IDs and campaigns belonging to another account have the same not-found response.

## Free deployment

The root `render.yaml` deploys this Dockerfile as exactly one Render Free web service. Vercel proxies same-origin `/api` requests to that service. Render Free sleeps when idle, so the first request after inactivity can take approximately one minute.

## Verify

```powershell
./gradlew.bat check bootJar
```

The PostgreSQL integration suite uses Testcontainers. It is skipped when Docker is unavailable locally; CI is required to execute it and fails if it is skipped.
