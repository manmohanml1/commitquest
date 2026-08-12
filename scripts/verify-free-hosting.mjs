import { readFileSync } from 'node:fs';

const renderBlueprint = readFileSync(new URL('../render.yaml', import.meta.url), 'utf8');
const vercelConfig = JSON.parse(readFileSync(new URL('../vercel.json', import.meta.url), 'utf8'));

const failures = [];

if (!/^\s*plan:\s*free\s*$/m.test(renderBlueprint)) {
  failures.push('render.yaml must pin the API service to plan: free.');
}

if (/^\s*(databases|disks|previews):\s*$/m.test(renderBlueprint)) {
  failures.push('The v0.3 free-hosting blueprint must not provision databases, disks, or previews.');
}

const apiRewrite = vercelConfig.rewrites?.find((rewrite) => rewrite.source === '/api/:path*');
if (apiRewrite?.destination !== 'https://commitquest-api-manmohanml1.onrender.com/api/:path*') {
  failures.push('Vercel must proxy /api to the pinned Render Free API origin.');
}

if ('functions' in vercelConfig) {
  failures.push('The v0.3 Vercel deployment must remain static and use no billable function configuration.');
}

if (failures.length > 0) {
  throw new Error(`Zero-cost hosting contract failed:\n- ${failures.join('\n- ')}`);
}

console.log('Zero-cost hosting contract verified: Vercel Hobby + one Render Free web service.');
