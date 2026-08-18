# ADR 0006: GitHub identity and saved-campaign ownership

**Status:** Accepted

## Context

V0.4 introduces the first user-controlled data. Authentication, repository authorization, session durability, tenant isolation, export, and deletion must be decided before HTTP handlers or database tables make accidental security policy. CommitQuest must also preserve its database-free bundled demo and public preview until a separately configured connected deployment exists.

GitHub OAuth identity and GitHub App repository installation solve different problems. Combining them would request repository permissions before v0.5 and make account access depend on an installation.

## Decision

### Identity

- Use GitHub OAuth authorization-code login for authentication only.
- Request no OAuth scopes. The default public identity response is sufficient to reconcile a stable GitHub numeric user ID; CommitQuest does not store email.
- Let an ordinary CommitQuest sign-out reconnect the active GitHub browser account without repeating consent. Offer a separate account-choice action that adds GitHub's documented `prompt=select_account` authorization parameter.
- After CommitQuest account deletion, present GitHub account choice as the primary next action. Deleting CommitQuest data does not sign the browser out of GitHub or mutate GitHub's own multi-account sessions.
- Store an internal UUID account ID, GitHub numeric ID, current login, display name, avatar URL, and timestamps.
- Treat the numeric GitHub user ID as the stable external identity; login changes update metadata rather than creating another account.
- Discard the GitHub OAuth access token after retrieving and reconciling the user profile. Repository access begins separately through the v0.5 GitHub App.

### OAuth request safety

- Generate at least 256 bits of random state, store only its protected digest, and expire it after ten minutes.
- Derive a PKCE verifier from the returned state with the server HMAC secret, send an S256 challenge, and never persist the verifier.
- Consume state exactly once and bind it to an allowlisted relative return path.
- Reject absolute, cross-origin, repeated, expired, or unmatched returns.
- Reject an OAuth token response that unexpectedly contains any granted scope.

### Session

- Issue at least 256 bits of random session material after successful account reconciliation.
- Put the raw token only in a `__Host-commitquest_session` cookie with `Secure`, `HttpOnly`, `SameSite=Lax`, `Path=/`, and no `Domain` attribute.
- Store only an HMAC-SHA-256 digest, account ID, creation time, expiry, and revocation metadata in PostgreSQL.
- Revoke existing sessions and issue new session material on login, revoke on logout and account deletion, and use a seven-day absolute lifetime for v0.4.
- Validate allowed Origin/Referer and a session-bound CSRF token on state-changing browser requests.
- Place the session-bound CSRF value in a separate Secure, SameSite=Lax, browser-readable cookie and require the same value in `X-CommitQuest-CSRF`; persist only its HMAC digest.

### Saved-campaign ownership

- Every application operation accepts an authenticated internal account ID supplied by the session boundary.
- Repository ports query by both account ID and campaign ID. Missing and cross-tenant records produce the same not-found result.
- Saving the same normalized repository for one owner refreshes the existing row and preserves its ID and creation time.
- New campaigns are private. An unlisted preference may be stored, but v0.4 exposes no anonymous campaign response.
- Limit an account to 25 campaigns until storage and abuse measurements justify a versioned change.
- Store the provider-neutral campaign projection as JSONB together with projection, mapping, scoring, and export versions. Raw GitHub payloads are never persisted.

### Export and deletion

- Export is deterministic JSON containing account-owned campaign metadata, version identifiers, and the provider-neutral projection.
- Campaign deletion hard-deletes that campaign.
- Account deletion transactionally revokes sessions and deletes OAuth state and imported campaigns before deleting the account.
- Later aggregate analytics cannot block deletion and must not retain repository or identity fields after deletion.

### Activation boundary

- The public preview and bundled campaign remain database-free and enabled by default.
- Identity, session, persistence, and saved-campaign web adapters activate only when complete connected configuration is present.
- No hosted database, OAuth application, secret, or migration execution is authorized by this ADR alone.

## Data model direction

```text
account
  1 -> many oauth_state (short lived)
  1 -> many user_session (revocable)
  1 -> many saved_campaign (maximum 25)

saved_campaign
  unique (account_id, repository_owner, repository_name)
  projection jsonb
  visibility private | unlisted
  projection_schema_version
  mapping_algorithm_version
  scoring_ruleset_version
  export_schema_version
```

All foreign keys use deletion behavior consistent with transactional account deletion. Timestamps are stored as UTC instants. Case-insensitive repository identity is normalized by application code and protected by a database uniqueness constraint.

## Alternatives

- **Use GitHub App installation as login:** rejected because identity and repository authorization have different lifecycles and minimum permissions.
- **Persist GitHub OAuth tokens:** rejected for v0.4 because identity reconciliation does not require long-lived provider access.
- **Browser-stored JWT:** rejected because revocation and account deletion must take effect immediately without placing durable identity claims in browser-readable storage.
- **Force a GitHub logout or depend on a browser popup:** rejected because CommitQuest must not terminate the user's provider session, popup blockers and mobile browsers make a separate window unreliable, and GitHub already owns the authoritative account picker.
- **Provider-managed auth:** deferred because GitHub-only identity is small, Spring Security remains the application boundary, and another vendor would expand cost and data-processing scope.
- **Enable JDBC unconditionally:** rejected because it would break the released database-free preview before connected deployment exists.

## Consequences

- Sign-in and repository connection are visibly separate product actions.
- A database leak does not directly expose reusable raw sessions or provider tokens.
- Session lookup adds one bounded database operation to authenticated requests.
- PostgreSQL availability affects saved campaigns but not the bundled or ephemeral preview.
- Public sharing still waits for the separately sanitized v0.8 showcase boundary.
