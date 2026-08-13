# Contributing

Read `docs/PROJECT_GUIDE.md` and `AGENTS.md` before making changes. Repository records are the source of truth when earlier discussions or implementation details disagree.

## Flow

1. Create a short-lived branch such as `codex/public-repository-preview`.
2. Use Conventional Commit pull-request titles such as `feat(map): add chapter encounters` or `fix(evidence): preserve unavailable state`.
3. Include purpose, scope, screenshots for visual changes, verification, and risk notes.
4. Keep `main` releasable.
5. Update the changelog and roadmap when user-visible behavior changes.
6. Add an ADR before changing a consequential architecture, provider, security, evidence, or scoring decision.
7. Before editing a component, run and record its current focused tests. After the edit, rerun those focused tests and the full affected application gate. Follow the [component regression contract](docs/engineering/COMPONENT_CONTRACTS.md).

## Engineering rules

- Organize backend code by business capability, not global technical layer.
- Keep mapping and scoring deterministic and independently testable.
- Never create a version tag or GitHub Release without the owner's explicit release-and-tag instruction.
- Do not reward commit count, lines changed, or engagement volume.
- Do not expose GitHub tokens or private repository evidence to the browser.
- Preserve the distinction between missing, failed, unavailable, private, and not applicable data.
- Add an architecture decision record for consequential or difficult-to-reverse choices.
