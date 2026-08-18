# Testing

Every change begins with a green pre-edit baseline for the component being touched. Focused tests run again during implementation; the full affected application gate and browser journey run before review. The required commands and ownership boundaries are defined in [Component contracts](engineering/COMPONENT_CONTRACTS.md).

## Current gates

- Angular component behavior
- Accessible evidence selection
- Exact primary-evidence links
- Fixture schema, mapping algorithm, and scoring ruleset versions
- Unique evidence identifiers
- Production bundle size budgets
- Prettier and zero-warning ESLint checks, including Angular template accessibility
- Desktop and 390×844 responsive browser smoke checks
- Node 24 CI verification
- Java 25 domain and application unit tests
- Fixture-driven GitHub adapter contract tests
- Spring application-context and ArchUnit boundary checks
- OpenAPI validation and generated-client compilation
- Full, history, foundation, and archive classification fixtures
- Repository-authored roadmap parsing and evidence-level invariants
- Bounded provider-cache behavior
- A 17-public-repository compatibility matrix based on the portfolio audit

Run all current gates with:

```powershell
npm run verify
cd apps/api
./gradlew.bat check bootJar
```

## Connected-product gates

V0.4 begins the connected-product gates with saved-campaign domain invariants, owner-scoped authorization tests, private-by-default visibility, deterministic export, account campaign limits, and OpenAPI/runtime version alignment. PostgreSQL migration and adapter integration tests become mandatory in the persistence slice. Later milestones add property-based scoring invariants, Spring Modulith verification, duplicated/out-of-order webhook scenarios, and the complete issue-to-release journey.

Identity gates cover relative return-path validation, one-time and expiring OAuth state, PKCE, zero requested scope, explicit `prompt=select_account` propagation, unexpected-scope rejection, stable GitHub numeric identity reconciliation, session rotation and expiry, HMAC-only persistence, Secure/HttpOnly/SameSite cookies, double-submit CSRF, Origin/Referer checks, logout, and cascading account deletion. Angular behavior distinguishes normal same-account reconnect, user-requested GitHub account choice, post-deletion account choice, Escape-dismissable modal behavior, and free-host vault awakening. The v0.4 component-style warning budget is capped at 16 KB with a 20 KB hard failure; raising either limit requires explicit review.

Saved-campaign transport gates cover owner derivation from the session, identical missing/cross-owner responses, CSRF and source checks for every mutation, owner-free response models, generated Angular-client compilation, connected-unavailable fallback, signed-out entry, owner-library loading, and private save behavior. Browser acceptance must retain the bundled map when the API is unavailable and must not present a fake successful sign-in or save state.

The v0.4 browser acceptance journey additionally covers guarded campaign deletion, guarded permanent account-data deletion, sign-out and reauthentication, and a 390×844 responsive pass. Mobile acceptance requires no horizontal clipping in the header, hero, campaign crest, repository form, vault controls, or confirmation states.

The PostgreSQL suite uses Testcontainers against PostgreSQL 17. A local machine without Docker may run the database-free gates with the integration suite reported as skipped; the GitHub Actions API job has Docker and fails explicitly if that suite does not execute. A skipped database suite is therefore never sufficient for merge or release evidence.

The release-critical journey will prove that issue creation, PR association, CI, review, merge, reward issuance, map projection, duplicate delivery, showcase sanitization, and tenant authorization behave correctly end to end.
