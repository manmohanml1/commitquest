# V0.5 shared design system

## Purpose

Modern and Chronicle use one semantic component tree. Tokens may change presentation; they cannot change evidence, control order, labels, authorization, or available actions.

## Token layers

Tokens are versioned in three layers:

1. **Primitive:** palette ramps, font families, numeric spacing, radii, border widths, shadows, and motion durations.
2. **Semantic:** application concepts such as canvas, surface, text, evidence status, focus, danger, verified, repository-authored, inferred, and owner-authored.
3. **Component:** rare local aliases for shell navigation, campaign cards, ledgers, dialogs, map framing, and status banners.

Components consume semantic or component tokens. They never consume a mode's raw palette value directly.

## Semantic token contract

| Token group | Modern expression | Chronicle expression | Invariant |
| --- | --- | --- | --- |
| Canvas | Quiet neutral engineering workspace | Dark timber/map-table field | Minimum contrast and reading order |
| Surface | Restrained panels with subtle elevation | Vellum, ledger, stone, or metal framing | Same semantic grouping |
| Primary text | High-contrast neutral | Ink or light vellum text | Never decorative-only |
| Accent/action | Focused cyan/mint family | Heraldic blue/aged gold family | Same action priority |
| Verified | Stable success treatment plus icon/text | Wax seal treatment plus icon/text | Always names verified source |
| Repository-authored | Distinct informational treatment | Ledger annotation treatment | Never promoted to verified |
| Inferred | Muted/dashed treatment | Pencil/map-sketch treatment | Always labelled inferred |
| Owner-authored | Personal accent plus label | Scribe mark plus label | Always labelled self-reported |
| Warning | Amber with explicit message | Ochre with explicit message | Never color-only |
| Danger | Red family, bounded destructive region | Red wax/broken-seal treatment | Same confirmation sequence |
| Focus | Strong visible outline | Strong visible outline adapted to material | WCAG-visible in every state |

## Typography

- Body, forms, evidence, tables, and long descriptions use a highly legible modern family in both modes.
- Chronicle display type is limited to short page titles, campaign names, and decorative labels.
- An easy-read control removes Chronicle display typography without changing mode or layout.
- Monospace is limited to repository identifiers, versions, timestamps, and source metadata.
- Text remains selectable and semantic; essential labels never live only inside images or canvas.

## Spacing and layout

- Use a four-pixel base scale with named steps; arbitrary component spacing requires review.
- Shell content width, responsive breakpoints, touch targets, and focus offsets are shared across modes.
- Chronicle framing may occupy reserved decoration areas but cannot reduce the content column below the Modern width at the same viewport.
- Component density is a user preference independent of design mode if introduced later.

## Motion

| Motion purpose | Allowed | Reduced-motion behavior |
| --- | --- | --- |
| Route or panel transition | Short orientation aid | Immediate state change |
| Provider ingestion | Bounded progress explanation | Static staged status |
| Map transformation | Explain repository-to-world mapping | Final map plus text summary |
| Chronicle atmosphere | Optional candle, ink, banner, route trace | Disabled |
| Reward/unlock | Explain evidence-linked state change | Static before/after announcement |

Continuous decorative motion cannot run behind forms, evidence reading, or destructive confirmation.

## Shared components

Every component specifies semantic markup, keyboard behavior, loading, empty, unavailable, error, success, destructive, narrow-screen, forced-colors, and reduced-motion states before a mode variant is accepted.

| Component | Responsibility |
| --- | --- |
| `CampaignCommandBar` | Campaign context, switcher, freshness, mode control, account entry |
| `PrimaryNavigation` | Route destinations and current-location semantics |
| `EvidenceBadge` | Provenance level with icon, label, and source explanation |
| `StatusBanner` | Waking, unavailable, stale, suspended, expired, and recovery states |
| `RecommendedQuest` | Exactly one applicable next action with rationale and evidence boundary |
| `CampaignCard` | Repository identity, mode, freshness, visibility, and selection |
| `EvidenceTimeline` | Encounters, checks, releases, and chapters without collapsing types |
| `ConfirmationDialog` | Focus-managed, labelled, cancellable destructive confirmation |
| `EmptyState` | Honest absence, cause, and valid next action |
| `ModeControl` | Modern/Chronicle selection and easy-read access without route loss |

## Implementation rules

- Apply the active mode on the document root before the authenticated shell paints to avoid a flash of the wrong mode.
- Persist a versioned local preference before sign-in; synchronize an account preference only after its API contract exists.
- Keep the public acquisition route visually stable during the authenticated-shell migration.
- Extract behavior from the v0.4 root only after the equivalent focused regression test exists.
- Introduce no third-party component or theme dependency for the first implementation slice.
- Keep each component stylesheet within the existing warning budget; a mode variant does not justify duplicating the stylesheet.
