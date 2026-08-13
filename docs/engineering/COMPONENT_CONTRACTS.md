# Component and regression contracts

## Change discipline

Before editing a component, run its focused test or gate and record the baseline. After editing, run the focused test again, then the full gate for every affected application. Cross-component changes require all intersecting gates. A green test from an earlier unrelated change is not a substitute.

No tag or GitHub Release may be created unless the owner explicitly instructs CommitQuest to release and tag a named version.

## Ownership and required gates

| Component                                 | Owns                                                  | Pre-edit and focused regression                                                                                                   | Full post-edit gate                                          |
| ----------------------------------------- | ----------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------ |
| GitHub adapter and README/roadmap parsers | Provider calls, bounds, transport normalization       | `gradlew.bat test --tests "*GitHubRepositoryEvidenceAdapterTest" --tests "*ReadmeSummaryParserTest" --tests "*RoadmapParserTest"` | `gradlew.bat check --no-daemon`                              |
| Campaign mapper and domain projection     | Modes, regions, quests, encounters, chapters, metrics | `gradlew.bat test --tests "*CampaignProjectionMapperTest" --tests "*PublicPortfolioCompatibilityTest"`                            | API check plus the 17-repository audit when semantics change |
| OpenAPI and generated client              | HTTP compatibility                                    | `gradlew.bat openApiValidate openApiGenerate` and review generated diff                                                           | API check plus `npm run verify`                              |
| Angular application shell                 | Input, state, tabs, accessible evidence, hero summary | `npm test`                                                                                                                        | `npm run verify` and browser QA                              |
| Phaser world adapter                      | World geometry, landmarks, selection bridge           | fixture and application component tests                                                                                           | `npm run verify`, desktop browser QA, 390px overflow QA      |
| Bundled fixture                           | Provider-independent flagship campaign                | fixture specification                                                                                                             | `npm run verify` and fallback browser journey                |
| Hosting configuration                     | Free-plan and proxy boundaries                        | `npm run check:free-hosting`                                                                                                      | CI, provider preview, health and proxy smoke tests           |
| Documentation only                        | Product and engineering contracts                     | Prettier on touched files and link/path review                                                                                    | CI documentation checks                                      |

## Mandatory cross-component scenarios

Changes to evidence ingestion, mapping, the world, or campaign ledgers preserve:

- bundled Portfolio Citadel fallback;
- full, history, foundation, and archive campaign modes;
- verified, repository-authored, inferred, and recommended provenance labels;
- nonempty honest quest, encounter, and chapter fallbacks;
- keyboard navigation and primary evidence links;
- API failure, rate-limit, private/missing repository, and cold-start messaging;
- no horizontal overflow at the supported mobile viewport;
- no browser console errors;
- no credentials or provider-native payloads in the browser.

## Review handoff

A review-ready pull request reports the pre-edit baseline, focused regressions, full gates, browser paths, affected contracts, deployment impact, and any intentionally deferred work. “Ready for review” never authorizes a tag or release.
