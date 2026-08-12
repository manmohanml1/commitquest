# Security

## Supported versions

Only the latest deployed minor line receives security fixes before version 1.0.

## Initial boundary

The 0.1 demo contains no credentials, authentication, persistent user data, or runtime GitHub access.

## Connected-product requirements

- Minimum GitHub App permissions and repository-specific installation access
- HMAC-SHA256 webhook verification over the unmodified request body
- Delivery deduplication using `X-GitHub-Delivery`
- Short-lived installation tokens stored only server-side
- Tenant authorization on every campaign and repository operation
- Sanitized Markdown and strict content security policy
- Allowlisted outbound hosts and SSRF protection
- Separate allowlisted public-showcase projection
- Secret scanning, dependency scanning, and data-deletion support

Do not open a public issue for a vulnerability that could expose credentials or private repository data.
