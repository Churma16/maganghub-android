import test from 'node:test';
import assert from 'node:assert';
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import {
  parseSemVer,
  resolveBaseVersion,
  incrementVersion,
  detectBumpTypeFromSubjects,
  updateGradleVersion,
  updateChangelog
} from '../.github/scripts/bump-version.js';
import {
  hasVersionModifications
} from '../.github/scripts/verify-no-manual-version-bump.js';
import {
  sanitizeTitle,
  extractReleaseSnippet,
  requestOpenRouterTitle,
  generateReleaseTitle
} from '../.github/scripts/generate-release-title.js';

test('parseSemVer correctly splits versions', () => {
  assert.deepStrictEqual(parseSemVer('1.0.0'), { major: 1, minor: 0, patch: 0 });
  assert.deepStrictEqual(parseSemVer('v2.4.1'), { major: 2, minor: 4, patch: 1 });
});

test('resolveBaseVersion prioritizes previous git tag when valid SemVer', () => {
  assert.strictEqual(resolveBaseVersion('1.0.0', 'v1.0.0'), '1.0.0');
  assert.strictEqual(resolveBaseVersion('1.0.1', '1.0.0'), '1.0.0');
  assert.strictEqual(resolveBaseVersion('1.0.0', 'v2.1.0'), '2.1.0');
});

test('resolveBaseVersion falls back to currentVersion when prevTag is missing or invalid', () => {
  assert.strictEqual(resolveBaseVersion('1.0.0', ''), '1.0.0');
  assert.strictEqual(resolveBaseVersion('1.0.0', 'invalid-tag'), '1.0.0');
  assert.strictEqual(resolveBaseVersion('1.0.0', undefined), '1.0.0');
});

test('incrementVersion calculates semver bumps', () => {
  assert.strictEqual(incrementVersion('1.0.0', 'patch'), '1.0.1');
  assert.strictEqual(incrementVersion('1.0.0', 'minor'), '1.1.0');
  assert.strictEqual(incrementVersion('1.0.0', 'major'), '2.0.0');
});

test('detectBumpTypeFromSubjects identifies major, minor, and patch', () => {
  // Major breaking changes (scoped and unscoped)
  assert.strictEqual(detectBumpTypeFromSubjects(['refactor!: rewrite scheduler engine']), 'major');
  assert.strictEqual(detectBumpTypeFromSubjects(['perf(sync)!: drop legacy protocol']), 'major');
  assert.strictEqual(detectBumpTypeFromSubjects(['feat!: new notification model']), 'major');
  assert.strictEqual(detectBumpTypeFromSubjects(['chore: update dependencies', 'fix: BREAKING CHANGE in config']), 'major');

  // Minor feature additions
  assert.strictEqual(detectBumpTypeFromSubjects(['feat: add Kemnaker SSO auto-login', 'fix: minor typo']), 'minor');
  assert.strictEqual(detectBumpTypeFromSubjects(['feat(ui): dark mode toggle']), 'minor');

  // Patch fixes and chores
  assert.strictEqual(detectBumpTypeFromSubjects(['fix: resolve crash on null bundle']), 'patch');
  assert.strictEqual(detectBumpTypeFromSubjects(['chore: update readme', 'docs: fix links']), 'patch');
  assert.strictEqual(detectBumpTypeFromSubjects([]), 'patch');
});

test('updateGradleVersion modifies versionCode and versionName in build.gradle.kts', () => {
  const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'gradle-test-'));
  const tempGradleFile = path.join(tempDir, 'build.gradle.kts');

  const initialContent = `
    defaultConfig {
        applicationId = "id.churma.maganghubcompanion"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
  `;
  fs.writeFileSync(tempGradleFile, initialContent);

  updateGradleVersion(tempGradleFile, 2, '1.0.1');

  const updatedContent = fs.readFileSync(tempGradleFile, 'utf8');
  assert.match(updatedContent, /versionCode = 2/);
  assert.match(updatedContent, /versionName = "1\.0\.1"/);

  fs.rmSync(tempDir, { recursive: true, force: true });
});

test('updateChangelog prepends new release entry', () => {
  const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'changelog-test-'));
  const tempChangelog = path.join(tempDir, 'CHANGELOG.md');

  updateChangelog(tempChangelog, '1.0.1', '* feat: Kemnaker SSO auto-login');

  const content = fs.readFileSync(tempChangelog, 'utf8');
  assert.match(content, /## \[1\.0\.1\]/);
  assert.match(content, /\* feat: Kemnaker SSO auto-login/);

  fs.rmSync(tempDir, { recursive: true, force: true });
});

test('hasVersionModifications detects added versionCode and versionName lines', () => {
  const diffWithVersionCode = `
@@ -14,2 +14,2 @@
-        versionCode = 1
+        versionCode = 2
  `;
  assert.strictEqual(hasVersionModifications(diffWithVersionCode), true);

  const diffWithVersionName = `
@@ -15,2 +15,2 @@
-        versionName = "1.0.0"
+        versionName = "1.0.1"
  `;
  assert.strictEqual(hasVersionModifications(diffWithVersionName), true);
});

test('hasVersionModifications allows unrelated changes in build.gradle.kts', () => {
  const diffWithDependency = `
@@ -75,2 +75,3 @@
     implementation("com.google.code.gson:gson:2.10.1")
+    implementation("androidx.biometric:biometric:1.2.0")
  `;
  assert.strictEqual(hasVersionModifications(diffWithDependency), false);
  const diffWithVersionSuffix = `
@@ -3,2 +3,4 @@
+val versionSuffix = project.findProperty("versionSuffix") as? String
+versionNameSuffix = versionSuffix
  `;
  assert.strictEqual(hasVersionModifications(diffWithVersionSuffix), false);
  assert.strictEqual(hasVersionModifications(''), false);
  assert.strictEqual(hasVersionModifications(null), false);
});

test('sanitizeTitle strips reasoning tags, quotes, prefixes, and emojis', () => {
  // Use unicode escape sequence to verify emoji removal without placing raw emoji literals in source code
  const inputWithThink = '<think>\nThinking about changes...\n</think>\n"Release Title: Background Presensi Alerts \u{1F680}"';
  const result = sanitizeTitle(inputWithThink);

  assert.strictEqual(result, 'Background Presensi Alerts');
  assert.doesNotMatch(result, /<think>/);
  assert.doesNotMatch(result, /\u{1F680}/);
  assert.doesNotMatch(result, /Release Title:/i);
});

test('sanitizeTitle handles unclosed <think> tag gracefully', () => {
  const unclosedThink = '<think>\nToken limit reached before closing thought tag...\nStill thinking';
  const result = sanitizeTitle(unclosedThink);
  assert.strictEqual(result, '');
});

test('sanitizeTitle supports unicode smart quotes', () => {
  const input = '“Smart Quoted Title”';
  const result = sanitizeTitle(input);
  assert.strictEqual(result, 'Smart Quoted Title');
});

test('requestOpenRouterTitle handles missing key and mock responses', async () => {
  const noKeyResult = await requestOpenRouterTitle({
    apiKey: '',
    changelogSnippet: 'test'
  });
  assert.strictEqual(noKeyResult, null);

  const mockFetch = async () => ({
    ok: true,
    json: async () => ({
      choices: [{ message: { content: 'Kemnaker SSO Auto-Login' } }]
    })
  });

  const title = await requestOpenRouterTitle({
    apiKey: 'mock-key',
    changelogSnippet: '* feat: kemnaker sso auto-login',
    fetchFn: mockFetch
  });

  assert.strictEqual(title, 'Kemnaker SSO Auto-Login');
});

test('requestOpenRouterTitle handles HTTP 500 error and network timeout gracefully', async () => {
  const mockHttpError = async () => ({
    ok: false,
    status: 500
  });

  const errorResult = await requestOpenRouterTitle({
    apiKey: 'mock-key',
    changelogSnippet: 'test',
    fetchFn: mockHttpError
  });
  assert.strictEqual(errorResult, null);

  const mockNetworkError = async () => {
    throw new Error('ETIMEDOUT');
  };

  const timeoutResult = await requestOpenRouterTitle({
    apiKey: 'mock-key',
    changelogSnippet: 'test',
    fetchFn: mockNetworkError
  });
  assert.strictEqual(timeoutResult, null);
});

test('generateReleaseTitle formats and deduplicates release titles', async () => {
  const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'title-gen-test-'));
  const notesPath = path.join(tempDir, 'notes.md');
  const titlePath = path.join(tempDir, 'title.txt');
  fs.writeFileSync(notesPath, '* feat: test feature');

  const mockFetch = async () => ({
    ok: true,
    json: async () => ({
      choices: [{ message: { content: 'v1.0.1 - Background Presensi Engine' } }]
    })
  });

  const result = await generateReleaseTitle({
    apiKey: 'mock-key',
    tagName: 'v1.0.1',
    notesFile: notesPath,
    outputTitleFile: titlePath,
    fetchFn: mockFetch
  });

  assert.strictEqual(result.releaseName, 'v1.0.1 - Background Presensi Engine');
  assert.strictEqual(fs.readFileSync(titlePath, 'utf8'), 'v1.0.1 - Background Presensi Engine');

  fs.rmSync(tempDir, { recursive: true, force: true });
});
