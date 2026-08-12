export type EvidenceLevel = 'verified' | 'repository-authored' | 'inferred' | 'owner-authored';
export type EvidenceTone = 'mint' | 'amber' | 'blue';

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
  position: { x: number; y: number };
}

export interface CampaignProjection {
  schemaVersion: 1;
  mappingAlgorithmVersion: 1;
  scoringRulesetVersion: 1;
  slug: string;
  title: string;
  repository: string;
  metrics: ReadonlyArray<{ value: string; label: string }>;
  evidence: ReadonlyArray<CampaignEvidence>;
}
