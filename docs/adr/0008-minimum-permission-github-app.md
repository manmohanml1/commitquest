# ADR 0008: Minimum-permission GitHub App connection

**Status:** Accepted

## Context

GitHub OAuth in v0.4 proves the CommitQuest account identity and deliberately requests no scopes. Version 0.5 needs separately authorized access to repositories selected by the owner, including private repositories, without expanding OAuth identity permissions or introducing repository writes.

GitHub Apps have no permissions by default. Repository permissions determine both REST access and which webhook subscriptions are available. Installation owners also choose which repositories the app may access. CommitQuest therefore needs an explicit permission, token, installation, and failure-state contract before registration or implementation.

Official references:

- [Choosing permissions for a GitHub App](https://docs.github.com/en/apps/creating-github-apps/registering-a-github-app/choosing-permissions-for-a-github-app)
- [Permissions required for GitHub Apps](https://docs.github.com/en/rest/authentication/permissions-required-for-github-apps)
- [Installing a GitHub App from a third party](https://docs.github.com/en/apps/using-github-apps/installing-a-github-app-from-a-third-party)
- [Using webhooks with GitHub Apps](https://docs.github.com/en/apps/creating-github-apps/registering-a-github-app/using-webhooks-with-github-apps)

## Decision

### Registration boundary

- Register a dedicated CommitQuest GitHub App; do not convert or reuse the GitHub OAuth identity app.
- Allow installation on user and organization accounts but request no organization, enterprise, or account permissions.
- Default installation guidance to **Only select repositories**. All-repository access is never required by CommitQuest.
- Request no repository write permission through 1.0.
- Disable user-to-server authorization for repository access; server operations use installation access tokens. GitHub OAuth remains the only CommitQuest sign-in mechanism.

### Repository permissions

| Permission | Level | Purpose |
| --- | --- | --- |
| Metadata | Read | Repository identity, visibility, default branch, and installation repository enumeration; GitHub grants metadata read with repository access |
| Contents | Read | README and roadmap evidence, tree/structure, commits, branches, tags, Releases, and workflow-file presence |
| Issues | Read | Candidate quests, issue state, labels, milestones, and source links |
| Pull requests | Read | Merged encounters, reviews, changed-file context, and merge evidence |
| Checks | Read | Check suites/runs and verified defense outcomes where the repository exposes them |

Actions, administration, deployments, environments, members, secrets, code-scanning alerts, security events, packages, discussions, pages, projects, and every write permission remain `none` unless a later accepted ADR proves a shipped requirement.

Before registration, an automated permission audit must exercise every selected endpoint and record GitHub's `X-Accepted-GitHub-Permissions` response header. If an endpoint requires broader access, either remove the endpoint or amend this ADR before changing the app registration.

### Installation lifecycle

- Reconcile installation identity and selected repositories after GitHub redirects to the allowlisted setup callback.
- Treat installation creation, repository-selection changes, suspension, unsuspension, deletion, and permission changes as explicit domain states.
- V0.5 may reconcile on callback, application visit, and deliberate refresh. Durable repository-activity webhook ingestion remains v0.6.
- A missing grant, suspended installation, removed repository, deleted installation, provider failure, and unknown state remain visibly different.
- One GitHub repository can connect to at most one owner campaign in the initial v0.5 model; cross-account installation conflicts return a non-enumerating failure.

### Token and secret handling

- Store the GitHub App ID, client-independent private key material, and webhook secret only in the server environment.
- Sign short-lived app JWTs only when requesting installation access tokens.
- Request installation tokens scoped to the selected repository set and the fixed permission map above.
- Keep installation tokens server-side and in memory only; never persist, log, export, or send them to the browser.
- Bound token reuse by its provider expiry and discard cached tokens on suspension, deletion, repository removal, or permission change.
- Store installation IDs and repository numeric IDs as external stable identifiers; repository owner/name remain mutable metadata.

### Data and deletion

- Persist installation ownership, selected repository identity, permission snapshot, lifecycle state, and reconciliation timestamps.
- Do not persist raw GitHub payloads as the connection record.
- Account deletion removes CommitQuest installation associations and imported campaign data. It cannot uninstall the GitHub App on the provider; the UI explains and links to GitHub's installation management when provider revocation is still required.
- Disconnecting a repository stops future reads and preserves or deletes the existing projection only through an explicit owner choice.

## Consequences

- Private repository access is explicit, selected, read-only, and independent from authentication.
- CommitQuest can explain exactly why each permission is requested.
- Some evidence may remain unavailable when a repository or plan does not expose it; missing checks are not failed engineering.
- Permission changes to the registered app require installation-owner approval and a new compatibility review.
- V0.5 adds installation and repository-connection persistence but no repository mutation and no background worker.

## Alternatives

- **Persist the v0.4 OAuth token and expand its scopes:** rejected because identity and repository authorization have different lifecycles and consent boundaries.
- **Request all read permissions:** rejected because unused read access still expands breach and trust impact.
- **Use a personal access token:** rejected because it is difficult to explain, rotate, scope, and revoke as a product onboarding flow.
- **Request write access for future features:** rejected because speculative permissions violate least privilege and CommitQuest is read-only through 1.0.
