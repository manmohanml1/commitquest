# Security

## Supported versions

Only the latest deployed minor line receives security fixes before version 1.0.

## Initial boundary

The 0.1 demo contains no credentials, authentication, persistent user data, or runtime GitHub access.

## Connected-product requirements

- GitHub identity OAuth is authentication only; repository authorization remains a separate minimum-permission GitHub App boundary
- Opaque, random, server-side sessions with only a protected cookie token in the browser
- One-time, expiring OAuth state and allowlisted relative return paths
- S256 PKCE derived from one-time state without persisting the verifier
- No requested GitHub OAuth scopes; reject unexpected returned permissions
- Origin and CSRF validation on every state-changing browser request
- Stateless Spring Security filter chain with application-owned opaque sessions and explicit CSRF enforcement
- Minimum GitHub App permissions and repository-specific installation access
- HMAC-SHA256 webhook verification over the unmodified request body
- Delivery deduplication using `X-GitHub-Delivery`
- Short-lived installation tokens stored only server-side
- Tenant authorization on every campaign and repository operation
- Sanitized Markdown and strict content security policy
- Allowlisted outbound hosts and SSRF protection
- Separate allowlisted public-showcase projection
- Secret scanning, dependency scanning, and data-deletion support

An owner-scoped lookup must not reveal whether another account's campaign exists. OAuth access tokens used only to fetch the signed-in profile are not retained after account reconciliation. Session tokens, OAuth state, provider credentials, and deletion secrets are never logged.

Do not open a public issue for a vulnerability that could expose credentials or private repository data.
