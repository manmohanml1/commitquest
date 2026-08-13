const baseUrl = (process.env.COMMITQUEST_AUDIT_BASE_URL ?? 'http://127.0.0.1:8081').replace(
  /\/$/,
  '',
);

const repositories = [
  ['commitquest', 'full', true],
  ['Leetcode-Practice', 'history', false],
  ['portfolio-website', 'full', true],
  ['novel-browser-glass', 'full', true],
  ['checkmate-glass-mrbd', 'foundation', true],
  ['autonomous-travel-guide-mrbd', 'full', true],
  ['glass-tube', 'foundation', true],
  ['glass-search-meta-display', 'foundation', true],
  ['Scalable-Data-Processing-System-for-High-Volume-Workloads', 'history', false],
  ['Typescript-Practice', 'history', false],
  ['OpenGL_Glut_Game', 'foundation', false],
  ['Langchain-Project-1', 'history', false],
  ['Movies-API', 'foundation', false],
  ['Fitness-exercises-app', 'foundation', false],
  ['Research-Papers', 'foundation', false],
  ['Software-Engineering-Design-Patterns', 'foundation', false],
  ['CSCI-174-Team-4', 'foundation', false],
];

const results = [];
for (const [repository, expectedMode, expectsRoadmap] of repositories) {
  const response = await fetch(`${baseUrl}/api/v1/repository-previews`, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ repositoryUrl: `https://github.com/manmohanml1/${repository}` }),
  });
  if (!response.ok) {
    throw new Error(`${repository}: HTTP ${response.status} ${await response.text()}`);
  }
  const campaign = await response.json();
  const failures = [];
  if (campaign.schemaVersion !== 3) failures.push(`schema ${campaign.schemaVersion}`);
  if (campaign.mappingAlgorithmVersion !== 5)
    failures.push(`mapping ${campaign.mappingAlgorithmVersion}`);
  if (campaign.mode !== expectedMode)
    failures.push(`mode ${campaign.mode}, expected ${expectedMode}`);
  if (campaign.evidence?.length !== 6) failures.push(`regions ${campaign.evidence?.length ?? 0}`);
  if (!campaign.quests?.length) failures.push('no candidate or clearly labelled recommendation');
  if (!campaign.encounters?.length) failures.push('no encounter or inferred expedition');
  if (!campaign.chapters?.length) failures.push('no release, tag, or foundation chapter');
  if (expectsRoadmap && !campaign.quests?.some((quest) => quest.sourceLabel === 'ROADMAP.md')) {
    failures.push('ROADMAP.md candidates missing');
  }
  if (failures.length) throw new Error(`${repository}: ${failures.join('; ')}`);
  results.push({
    repository,
    mode: campaign.mode,
    quests: campaign.quests.length,
    encounters: campaign.encounters.length,
    chapters: campaign.chapters.length,
  });
}

console.table(results);
console.log(`Public portfolio compatibility audit passed for ${results.length} repositories.`);
