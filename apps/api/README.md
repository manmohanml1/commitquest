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

The token remains server-side. Preview requests are bounded and are never persisted.

## Verify

```powershell
./gradlew.bat check
```
