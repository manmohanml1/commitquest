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
  level: 'repository-authored';
  title: string;
  summary: string;
  status: 'candidate';
  sourceLabel: string;
  evidenceUrl: string;
}

export interface CampaignEncounter {
  id: string;
  title: string;
  summary: string;
  status: 'victory';
  pullRequest: number;
  release: string;
  evidenceUrl: string;
}

export interface CampaignChapter {
  version: string;
  title: string;
  summary: string;
  status: 'unlocked' | 'current';
  evidenceUrl: string;
}

export interface CampaignProjection {
  schemaVersion: 2;
  mappingAlgorithmVersion: 2 | 3;
  scoringRulesetVersion: 1;
  slug: string;
  title: string;
  repository: string;
  mode: 'history' | 'preview';
  currentChapter: string;
  metrics: ReadonlyArray<{ value: string; label: string }>;
  evidence: ReadonlyArray<CampaignEvidence>;
  quests: ReadonlyArray<CampaignQuest>;
  encounters: ReadonlyArray<CampaignEncounter>;
  chapters: ReadonlyArray<CampaignChapter>;
}
