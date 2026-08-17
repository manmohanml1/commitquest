# Versioning and releases

CommitQuest uses one product version for the web, API, and worker until they acquire independent release cycles.

## Product progression

```text
0.1.0  Demo foundation
0.2.0  Expanded campaign map, encounters, chapters, and candidate quests
0.3.0  Public repository preview
0.4.0  Persistent campaigns
0.5.0  GitHub App installation
0.6.0  Live webhook synchronization
0.7.0  Progression and repository health
0.8.0  Public showcases and portfolio embed
0.9.0  Closed beta and production hardening
1.0.0  Stable public product
```

Annotated release tags use `vMAJOR.MINOR.PATCH`. Prereleases use `-alpha.N`, `-beta.N`, and `-rc.N`. Environments are deployment metadata and never appear in a product version.

A completed milestone, updated release record, merged pull request, or production deployment is not release authorization. Tags and GitHub Releases are created only after the owner explicitly requests that release action.

## Independent compatibility versions

- HTTP API version
- Database migration version
- Event schema version
- Fixture and campaign projection version
- `.commitquest.yml` schema version
- Mapping algorithm version
- Scoring ruleset version
- AI prompt version

Container artifacts are tagged with their immutable Git commit SHA. The identical built artifact is promoted from staging to production.

## Change policy

- `feat:` normally produces a minor release.
- `fix:` and `perf:` normally produce a patch release.
- `feat!:` or `BREAKING CHANGE:` produces a major release after 1.0.
- Documentation, tests, CI, build, and chore changes appear in release history without forcing a product release alone.
