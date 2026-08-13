export type EvidenceLevel = 'verified' | 'repository-authored' | 'inferred' | 'owner-authored';
export type EvidenceTone = 'mint' | 'amber' | 'blue';
export type CampaignView = 'map' | 'quests' | 'encounters' | 'chapters';

export interface CampaignEvidence {
  id: string;
  level: EvidenceLevel;
  eyebrow: string;
  title: string;
  description: string;
  meta: string;
  evidenceUrl: string;
  status: string;
  tone: EvidenceTone;
  icon: string;
  position: { x: number; y: number };
}

export interface CampaignQuest {
  id: string;
  regionId: string;
  level: 'verified' | 'repository-authored' | 'inferred';
  title: string;
  summary: string;
  status: 'candidate' | 'recommended';
  sourceLabel: string;
  evidenceUrl: string;
}

export interface CampaignEncounter {
  id: string;
  title: string;
  summary: string;
  level?: 'verified' | 'inferred';
  kind?: 'pull-request' | 'commit';
  reference?: string;
  status: 'victory' | 'observed';
  pullRequest?: number;
  release?: string;
  evidenceUrl: string;
}

export interface CampaignChapter {
  version: string;
  level?: 'verified' | 'repository-authored' | 'inferred';
  kind?: 'release' | 'tag' | 'foundation';
  title: string;
  summary: string;
  status: 'unlocked' | 'current';
  evidenceUrl: string;
}

export interface CampaignProjection {
  schemaVersion: 2 | 3;
  mappingAlgorithmVersion: 2 | 3 | 4 | 5;
  scoringRulesetVersion: 1;
  slug: string;
  title: string;
  repository: string;
  mode: 'full' | 'history' | 'foundation' | 'archive' | 'preview';
  currentChapter: string;
  metrics: ReadonlyArray<{ value: string; label: string }>;
  evidence: ReadonlyArray<CampaignEvidence>;
  quests: ReadonlyArray<CampaignQuest>;
  encounters: ReadonlyArray<CampaignEncounter>;
  chapters: ReadonlyArray<CampaignChapter>;
}
