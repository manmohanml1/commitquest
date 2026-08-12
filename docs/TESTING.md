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

Run all current gates with:

```powershell
npm run verify
```

## Connected-product gates

The backend milestone adds domain unit tests, property-based scoring invariants, Spring Modulith boundary verification, Testcontainers integration tests, GitHub adapter contract fixtures, authorization matrices, migration tests, and duplicated/out-of-order webhook scenarios.

The release-critical journey will prove that issue creation, PR association, CI, review, merge, reward issuance, map projection, duplicate delivery, showcase sanitization, and tenant authorization behave correctly end to end.
