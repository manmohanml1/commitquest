import { PORTFOLIO_CITADEL } from './portfolio-citadel.fixture';

describe('Portfolio Citadel fixture', () => {
  it('is explicitly versioned and evidence-backed', () => {
    expect(PORTFOLIO_CITADEL.schemaVersion).toBe(2);
    expect(PORTFOLIO_CITADEL.mappingAlgorithmVersion).toBe(2);
    expect(PORTFOLIO_CITADEL.scoringRulesetVersion).toBe(1);
    expect(PORTFOLIO_CITADEL.mode).toBe('history');
    expect(PORTFOLIO_CITADEL.evidence.every((item) => item.level === 'verified')).toBe(true);
    expect(
      PORTFOLIO_CITADEL.evidence.every((item) =>
        item.evidenceUrl.startsWith('https://github.com/manmohanml1/portfolio-website/'),
      ),
    ).toBe(true);
    expect(new Set(PORTFOLIO_CITADEL.evidence.map((item) => item.id)).size).toBe(
      PORTFOLIO_CITADEL.evidence.length,
    );
  });

  it('keeps candidate quests distinct from verified outcomes', () => {
    expect(PORTFOLIO_CITADEL.quests).toHaveLength(8);
    expect(
      PORTFOLIO_CITADEL.quests.every(
        (quest) => quest.level === 'repository-authored' && quest.status === 'candidate',
      ),
    ).toBe(true);
    expect(
      PORTFOLIO_CITADEL.quests.every((quest) =>
        PORTFOLIO_CITADEL.evidence.some((region) => region.id === quest.regionId),
      ),
    ).toBe(true);
  });

  it('links every encounter and chapter to primary GitHub evidence', () => {
    const historicalEvidence = [
      ...PORTFOLIO_CITADEL.encounters.map((encounter) => encounter.evidenceUrl),
      ...PORTFOLIO_CITADEL.chapters.map((chapter) => chapter.evidenceUrl),
    ];
    expect(historicalEvidence.every((url) => url.startsWith('https://github.com/'))).toBe(true);
    expect(
      PORTFOLIO_CITADEL.chapters.filter((chapter) => chapter.status === 'current'),
    ).toHaveLength(1);
  });
});
