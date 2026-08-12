import { CampaignProjection } from '../domain/campaign';

export const PORTFOLIO_CITADEL: CampaignProjection = {
  schemaVersion: 1,
  mappingAlgorithmVersion: 1,
  scoringRulesetVersion: 1,
  slug: 'manmohan/portfolio-citadel',
  title: 'Portfolio Citadel',
  repository: 'manmohanml1/portfolio-website',
  metrics: [
    { value: '13', label: 'merged encounters' },
    { value: '13', label: 'released chapters' },
    { value: '2', label: 'active CI defenses' },
    { value: '8', label: 'candidate quests' },
  ],
  evidence: [
    {
      id: 'presentation-capital',
      level: 'verified',
      eyebrow: 'REGION · APPLICATION',
      title: 'Presentation Capital',
      description:
        'Five responsive portfolio worlds share one verified evidence model without duplicating content or behavior.',
      meta: 'Source: src/ · PR #12 · Release v1.8.0',
      evidenceUrl: 'https://github.com/manmohanml1/portfolio-website/pull/12',
      status: 'Restored',
      tone: 'mint',
      position: { x: 155, y: 145 },
    },
    {
      id: 'neon-vault',
      level: 'verified',
      eyebrow: 'REGION · DATA',
      title: 'Neon Vault',
      description:
        'Environment-scoped configuration, audited moderation, and database-authoritative publishing live behind the service boundary.',
      meta: 'Source: db/ · PR #6 · PR #12',
      evidenceUrl: 'https://github.com/manmohanml1/portfolio-website/pull/6',
      status: 'Fortified',
      tone: 'blue',
      position: { x: 585, y: 130 },
    },
    {
      id: 'guardian-barracks',
      level: 'verified',
      eyebrow: 'DEFENSE · QUALITY',
      title: 'Guardian Barracks',
      description:
        'Automated behavior, content, security, and release checks defend every production promotion.',
      meta: 'Source: tests/ · 153 reported tests',
      evidenceUrl: 'https://github.com/manmohanml1/portfolio-website/pull/12',
      status: 'Online',
      tone: 'mint',
      position: { x: 330, y: 340 },
    },
    {
      id: 'evidence-age',
      level: 'verified',
      eyebrow: 'ENCOUNTER · MERGED PR',
      title: 'The Evidence Age',
      description:
        'A publishing platform, evidence discovery, project curation, and owner-controlled delivery converged in one major encounter.',
      meta: 'GitHub PR #12 · Release v1.8.0',
      evidenceUrl: 'https://github.com/manmohanml1/portfolio-website/pull/12',
      status: 'Victory',
      tone: 'amber',
      position: { x: 610, y: 335 },
    },
  ],
};
