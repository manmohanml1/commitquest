# Contributing

## Flow

1. Create a short-lived branch such as `codex/public-repository-preview`.
2. Use Conventional Commit-style pull-request titles.
3. Include purpose, scope, screenshots for visual changes, verification, and risk notes.
4. Keep `main` releasable.
5. Update the changelog and roadmap when user-visible behavior changes.

## Engineering rules

- Organize backend code by business capability, not global technical layer.
- Keep mapping and scoring deterministic and independently testable.
- Do not reward commit count, lines changed, or engagement volume.
- Do not expose GitHub tokens or private repository evidence to the browser.
- Preserve the distinction between missing, failed, unavailable, private, and not applicable data.
- Add an architecture decision record for consequential or difficult-to-reverse choices.
