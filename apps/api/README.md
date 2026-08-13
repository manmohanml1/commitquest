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

## Free deployment

The root `render.yaml` deploys this Dockerfile as exactly one Render Free web service. Vercel proxies same-origin `/api` requests to that service. Render Free sleeps when idle, so the first request after inactivity can take approximately one minute.

## Verify

```powershell
./gradlew.bat check bootJar
```
