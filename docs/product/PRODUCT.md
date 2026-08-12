# Product definition

## Vision

CommitQuest makes repository maintenance understandable, motivating, and shareable by translating engineering evidence into a persistent RPG campaign.

## Primary users

- Individual developers presenting and maintaining real projects
- Open-source maintainers onboarding contributors
- New contributors looking for appropriately scoped work
- Educators teaching testing, review, and release practices
- Small teams seeking a playful delivery overview

## Core loop

1. A user tries the bundled demo or supplies a public repository.
2. CommitQuest classifies the repository and normalizes available evidence.
3. The mapping engine builds regions from meaningful project modules.
4. Verified issues become quests; repository-authored roadmap items become candidate quests.
5. Pull requests, reviews, checks, and merges drive encounters.
6. Releases complete chapters and preserve campaign snapshots.
7. A sanitized showcase can be shared without granting repository access.

## Evidence levels

| Level | Source | Product treatment |
| --- | --- | --- |
| Verified | GitHub issue, PR, review, check, workflow, release | Eligible for normal progression |
| Repository-authored | Roadmap, changelog, architecture or testing document | Source-linked; owner confirmation may be required |
| Inferred | Commit grouping or detected structural change | Suggestion only |
| Owner-authored | Manual milestone or external work | Clearly labeled self-reported |

The interface distinguishes failed, unavailable, private, not configured, not applicable, and unknown signals.

## Campaign modes

- **Full campaign:** issues, PRs, checks, and releases are available.
- **History campaign:** merged PRs and releases exist but there is no current issue backlog.
- **Foundation campaign:** source, documentation, and CI exist with little workflow history.
- **Archive campaign:** the repository is completed, educational, experimental, or inactive.

## Non-goals

- Replacing GitHub Issues, Projects, Jira, or Linear
- Rewarding commit count, lines changed, comments, or time online
- Running arbitrary repository code
- Claiming private or self-reported work is GitHub-verified
- Autonomous GitHub writes in the initial product
- Global competitive leaderboards
