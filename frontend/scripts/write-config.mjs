import { writeFileSync } from 'node:fs';
const apiUrl = process.env.API_URL || 'http://localhost:8080';
writeFileSync('public/config.js', `window.__SALARY_API_URL__=${JSON.stringify(apiUrl)};\n`);

