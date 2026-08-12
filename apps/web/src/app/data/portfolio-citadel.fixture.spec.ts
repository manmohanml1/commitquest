import { PORTFOLIO_CITADEL } from './portfolio-citadel.fixture';

describe('Portfolio Citadel fixture', () => {
  it('is explicitly versioned and evidence-backed', () => {
    expect(PORTFOLIO_CITADEL.schemaVersion).toBe(1);
    expect(PORTFOLIO_CITADEL.mappingAlgorithmVersion).toBe(1);
    expect(PORTFOLIO_CITADEL.scoringRulesetVersion).toBe(1);
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
});
