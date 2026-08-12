# CommitQuest contributor guide

Read `docs/PROJECT_GUIDE.md` before changing product scope, architecture, dependencies, evidence semantics, release behavior, or deployment. Repository records supersede chat history and personal recollection.

## Required workflow

1. Work from the next unfinished milestone in `ROADMAP.md`; do not pull later infrastructure into an earlier milestone without an accepted ADR.
2. Preserve evidence provenance and the distinctions between verified, repository-authored, inferred, and owner-authored claims.
3. Keep Angular responsible for accessible product UI and Phaser isolated behind the map adapter.
4. Keep GitHub payloads and credentials behind the future API boundary; domain models never depend on provider payloads.
5. Add tests for behavior and invariants, then run `npm run verify` before committing frontend or shared changes.
6. Update `CHANGELOG.md`, `ROADMAP.md`, release records, and ADRs when their facts change.
7. Never commit secrets, generated build output, provider credentials, private repository evidence, or copied employer data.

## Change discipline

- Prefer small, short-lived branches and Conventional Commit pull-request titles.
- Keep `main` releasable.
- Avoid dependencies until a shipped requirement justifies them.
- Treat accessibility, security, observability, operability, and deletion as product behavior.
- Never reward commit count, lines changed, comments, or time online.
