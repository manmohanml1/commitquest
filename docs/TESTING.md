# Testing

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

Run all current gates with:

```powershell
npm run verify
cd apps/api
./gradlew.bat check
```

## Connected-product gates

Later connected-product milestones add property-based scoring invariants, Spring Modulith verification, Testcontainers integration tests, authorization matrices, migration tests, and duplicated/out-of-order webhook scenarios.

The release-critical journey will prove that issue creation, PR association, CI, review, merge, reward issuance, map projection, duplicate delivery, showcase sanitization, and tenant authorization behave correctly end to end.
