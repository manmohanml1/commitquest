# Modern and Chronicle design modes

## Decision

Version 0.5 redesigns the authenticated CommitQuest experience and ships two complete presentation modes over one product, information architecture, and evidence model:

1. **Modern mode** is a redesigned, calm engineering workspace. It retains CommitQuest's current dark-product direction, but does not preserve the current private-vault layout merely for compatibility.
2. **Chronicle mode** is a traditional medieval-fantasy game presentation. It treats a connected repository as a living illuminated campaign: repositories become realms, saved campaigns become chronicles, evidence becomes seals and ledger entries, and GitHub App permissions become guarded gates.

The current v0.4 interface is an accepted release baseline, not the v0.5 design target.

## Shared product contract

The modes are visual and presentational variants, not separate applications.

- Angular owns one accessible semantic DOM, routing model, command hierarchy, forms, dialogs, validation, and account state.
- Domain services, GitHub App authorization, persistence, evidence labels, destructive confirmations, and API contracts are shared.
- A versioned design-token layer controls color, type, spacing, surface, border, elevation, icon treatment, map materials, and motion.
- Components expose purposeful variants instead of branching whole page trees by theme.
- The selected mode persists to the signed-in account when available and falls back to local preference before sign-in. System preference may choose an initial light/dark treatment but never silently changes the selected product mode.
- Switching modes preserves route, selected campaign, focused task where practical, filters, unsaved form values, and scroll context.
- No mode may hide evidence provenance, turn an inferred claim into a verified one, or change score and progression rules.

## Authenticated information architecture

The v0.5 redesign replaces the long landing-page continuation after login with an application shell:

- **Campaign switcher:** change repository or create/connect a campaign.
- **Realm overview:** identity, current state, repository health summary, recent verified change, and one recommended next quest.
- **World:** interactive campaign map with an equivalent HTML region navigator and ledger.
- **Quest log:** verified issues, repository-authored candidates, and CommitQuest recommendations kept visibly distinct.
- **Chronicle:** encounters, releases, defenses, and evidence timeline.
- **Vault:** saved campaigns, visibility, refresh, export, and deletion controls.
- **Connection and account:** GitHub identity, GitHub App installations, repository permissions, design mode, accessibility, and data deletion.

The public repository preview remains a focused acquisition flow. Signing in moves the user into the application shell instead of inserting private-vault controls into the public marketing page.

## Modern mode direction

Modern mode optimizes for engineering clarity and repeat use:

- compact app shell with clear navigation and campaign context;
- restrained dark and light surfaces, strong hierarchy, and limited accent colors;
- legible data cards, timelines, source badges, tables, and command-oriented controls;
- motion used only to explain ingestion, state change, map transformation, and completion;
- the world map remains distinctive without competing with day-to-day campaign management.

GitHub Primer is a useful systems reference: its token model supports multiple color modes, while its accessibility and layout guidance emphasizes consistency, responsive behavior, contrast, and focused reading order. CommitQuest will borrow those principles, not GitHub's visual identity.

## Chronicle mode direction

Chronicle mode should feel authored as a medieval game, not like Modern mode with a parchment background:

- illuminated-manuscript framing, restrained woodcut textures, heraldic seals, map ink, metal, timber, stone, wax, and vellum materials;
- book, ledger, map-table, quest-board, chapter, and gate metaphors mapped consistently to real product actions;
- readable modern body type by default, with decorative display type limited to short headings;
- optional atmospheric motion such as candle glow, ink reveal, banner movement, and map-route tracing, all disabled or simplified by reduced-motion preference;
- sound is out of scope unless it is separately approved, off by default, and fully controllable;
- status is never communicated by color, texture, heraldry, or animation alone.

Reference lessons:

- **Pentiment:** illuminated manuscripts and woodcut prints can create a historically grounded world while still offering easy-read fonts, adjustable text size, high contrast, steady presentation, and full keyboard support.
- **Baldur's Gate 3:** party, character, class, skill, and story information demonstrates how dense RPG identity can be layered without making every surface equally prominent.
- **Darkest Dungeon:** strong silhouettes, restrained palettes, and explicit campaign pressure show how atmosphere can reinforce state; CommitQuest must avoid importing its horror tone or punitive framing.
- **Diablo IV:** regions, quests, progression, and landmark-driven world framing show how a large campaign can feel explorable; CommitQuest will avoid loot-store patterns, visual noise, and reward mechanics unsupported by repository evidence.
- **Old School RuneScape:** a map, quest vocabulary, and earned long-term progression can remain understandable and community-shaped; CommitQuest will use the clarity of recognizable destinations without copying its interface or grind loops.

References are for interaction and art-direction study only. No proprietary artwork, trademarks, characters, typefaces, UI assets, or copied layouts enter the product.

### Research references

- [GitHub Primer foundations](https://primer.style/product/getting-started/), [color modes](https://primer.style/product/getting-started/foundations/color-usage/), and [accessibility](https://primer.style/accessibility/)
- [Pentiment official site and accessibility features](https://pentiment.obsidian.net/)
- [Baldur's Gate 3 official site](https://baldursgate3.game/)
- [Darkest Dungeon official site](https://www.darkestdungeon.com/darkest-dungeon/)
- [Diablo IV official site](https://diablo4.blizzard.com/en-us/)
- [Old School RuneScape official site](https://oldschool.runescape.com/)

## Acceptance contract

- Modern and Chronicle cover the complete signed-in journey, not isolated demo screens.
- Both modes pass the same functional, authorization, CSRF, ownership, and destructive-action tests.
- WCAG 2.2 AA contrast, keyboard operation, visible focus, landmarks, labels, zoom, reflow, and screen-reader meaning are release requirements.
- Decorative fonts have a one-action easy-read alternative; essential text never appears only inside an image or Phaser canvas.
- Desktop, tablet, 390 x 844 mobile, 320 CSS-pixel reflow, reduced motion, forced colors, and 200% zoom receive explicit acceptance passes.
- Every component state is covered: loading, cold start, empty, partial evidence, unavailable provider, expired session, success, error, and destructive confirmation.
- Visual-regression fixtures cover public preview plus full, history, foundation, archive, sparse, connected, and disconnected campaigns in both modes.
- Performance budgets are measured per mode. Chronicle decorations load progressively and cannot block authentication, navigation, evidence, or vault actions.
- Theme switching has automated state-preservation coverage and produces no API request except preference persistence.
- A design review approves mobile hierarchy, readability, originality, and semantic parity before implementation is considered complete.

## Delivery sequence

1. Audit the authenticated v0.4 journey and approve wireframes for the shared application shell.
2. Define semantic design tokens and component contracts before visual implementation.
3. Build the Modern shell and migrate the complete authenticated journey.
4. Build Chronicle variants and original scalable assets on the same components.
5. Integrate GitHub App installation and permission-aware repository states into both modes.
6. Add accessibility, responsive, visual-regression, performance, and cross-mode state-preservation gates.
7. Run preview QA with representative repository fixtures and obtain explicit visual acceptance before v0.5 release authorization.
