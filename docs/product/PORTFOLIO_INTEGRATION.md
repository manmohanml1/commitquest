# Portfolio live-demo integration

## Goal

CommitQuest appears inside the portfolio as a real, responsive demonstration rather than only an external link. The portfolio experience remains fast and visually consistent, while the standalone CommitQuest application retains the complete Phaser world and arbitrary-repository workflow.

## Decision

Build a portfolio-native miniature campaign backed by a sanitized CommitQuest projection. Do not iframe the complete CommitQuest application by default.

The project dialog initially shows a curated campaign poster and a **Start live demo** control. Activating it lazily requests one allowlisted public repository such as `manmohanml1/commitquest` or `manmohanml1/portfolio-website` through a same-origin portfolio function. The compact component renders semantic HTML and lightweight SVG/CSS for:

- campaign name and evidence mode;
- repository summary and mapping version;
- three to six representative landmarks;
- candidate quests, encounters, chapters, and evidence links;
- later, applicable maturity dimensions and unlock explanations;
- a **Try your repository in CommitQuest** link to the complete standalone product.

The portfolio owns presentation and accessibility. CommitQuest owns evidence semantics, mapping, scoring, provenance, and the sanitized API contract. Neither repository copies the other's domain logic.

## Delivery phases

### v0.3 portfolio entry

- Curated CommitQuest project card and case-study content.
- Current campaign screenshot or poster.
- Repository and standalone live-application actions.
- No dependency on the CommitQuest API during initial portfolio render.

### v0.4–0.7 contract preparation

- Define a stable, cacheable, sanitized public projection.
- Keep private installation identifiers, tokens, webhook details, owner controls, and raw provider payloads out of the projection.
- Add version negotiation and a deterministic unavailable fallback.

### v0.8 live mini-demo

- Add the lazy **Start live demo** interaction to the portfolio project dialog.
- Proxy the allowlisted request through the portfolio's own Vercel Function so the browser uses a same-origin contract.
- Apply strict URL validation, response-size limits, timeout, caching, and per-client rate limits.
- Render a portfolio-native mini world; do not load Phaser for the compact experience.
- Preserve a static poster and standalone link when CommitQuest, Render, GitHub, or the proxy is unavailable.

## Why the full iframe is deferred

A complete-site iframe loads a second navigation hierarchy and a heavy canvas experience, performs poorly on narrow screens, complicates focus and reduced-motion behavior, and requires coordinated content-security policies. It would also expose a visitor to a free-tier backend cold start before they understand the project.

If a future `/embed/{owner}/{repository}` route is justified, it must be read-only, omit credentials and owner controls, allow only explicit portfolio origins through `frame-ancestors`, use a strict `postMessage` contract for height and navigation, and retain equivalent semantic content outside the canvas.

## Acceptance contract

- The portfolio remains useful before JavaScript enhancement and during provider outages.
- The live demo loads only after visitor intent.
- No GitHub or CommitQuest secret reaches the browser.
- The embedded projection is safe to publish by construction, not a filtered private response.
- Mobile, keyboard, screen-reader, reduced-motion, and timeout behavior have automated coverage.
- The standalone CommitQuest application remains the authoritative complete experience.
