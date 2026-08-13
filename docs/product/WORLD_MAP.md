# World map and progression

## The three visual surfaces

CommitQuest deliberately separates three things that currently look similar:

1. **Campaign crest:** the citadel illustration in the hero card is a branded summary. In v0.3 its accent reflects full, history, foundation, or archive mode. It is not the interactive world and never represents a score.
2. **World map:** the Phaser surface is the navigable repository projection. Angular owns its authoritative labels, keyboard controls, selected evidence, and mobile layout.
3. **Campaign ledgers:** quests, encounters, and chapters explain the evidence behind each region. They remain useful when canvas rendering is unavailable.

`MAPPING MODEL V5` identifies the deterministic evidence-to-campaign algorithm. It does not mean map level five, player level five, or a score of five.

## What v0.3 has

- A stable six-region topology: repository gate, codebase province, quest board, encounter archive, defense bastion, and chapter beacon.
- Repository-specific titles, descriptions, evidence links, language, modes, statuses, tones, quests, encounters, and chapters.
- Distinct full, history, foundation, and archive interpretations.
- Mode-accented campaign crests and a rebuilt Phaser scene for each pasted repository.
- No persistent state, cumulative score, inventory, or unlock economy.

The v0.3 world reuses one readable topology because the product is proving evidence classification first. This is why two repositories can have different content while retaining a recognizably similar silhouette.

## Why the frontier crest remains mostly stable

The hero crest is the product's visual signature and an immediate summary, while the detailed map appears below. Changing its towers based on unversioned heuristics would imply progression that v0.3 does not calculate. It may vary safely by campaign mode now; structural unlocks wait for an accepted scoring and progression contract.

## Planned evolution

| Milestone | World change                                                                                                                |
| --------- | --------------------------------------------------------------------------------------------------------------------------- |
| 0.4       | Save a user-controlled campaign, visibility, and deletion state. The same repository can retain its world between sessions. |
| 0.5       | GitHub App synchronization identifies connected evidence and permission-aware regions.                                      |
| 0.6       | Webhook events update encounters, defenses, and chapter state without rebuilding the whole campaign manually.               |
| 0.7       | Explainable scoring, health, ruleset versioning, anti-gaming controls, and deterministic presentation unlocks.              |
| 0.8       | Sanitized public worlds, social cards, and portfolio embeds expose selected unlocked presentation safely.                   |

## Candidate scoring and unlock direction for v0.7

This is a design direction, not the accepted formula. An ADR is required before implementation.

- Score a small set of evidence dimensions rather than raw activity volume: project explanation, actionable planning, reviewed delivery, automated quality, release discipline, and maintenance.
- Every point must link to evidence and explain why it was awarded.
- Suggestions must identify a concrete next step, such as adding a useful repository description, opening a scoped issue, adopting pull-request review, configuring CI, or publishing a tagged release.
- Repeated low-value commits, mass-created issues, or cosmetic releases must not farm score.
- Repository-authored and inferred evidence can guide improvements but cannot silently earn the same progression as verified outcomes.

Possible presentation unlocks include roads between established regions, landmark upgrades, chapter gates, banners, beacons, and additional map districts. Unlocks are visual explanations of verified maturity, never randomized loot and never a substitute for the evidence ledger.

## Acceptance rules for future visual work

- Essential text and evidence remain in Angular HTML, never only in Phaser canvas.
- Desktop and mobile expose equivalent campaign meaning.
- A new visual state requires a deterministic fixture and regression test.
- Existing full, history, foundation, archive, bundled, empty-evidence, and provider-failure paths must remain usable.
- Mapping changes version the mapping algorithm; scoring changes version the scoring ruleset. Neither version is marketed as a player score.
