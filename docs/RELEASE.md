# NEXUS 1.0.0: Versioning, signing, release

## 1. Versioning

* `versionName = "1.0.0"`, `versionCode = 10000` (scheme: major*10000 + minor*100 + patch; 1.0.0 = 10000; 1.2.3 = 10203). Replaces `2.0.0-preview.1` (code 20000). Because the preview line used 20000, installing 1.0.0 over a preview build would be refused as a downgrade: uninstall the preview first (documented in CHANGELOG).
* Single source: two `val`s at the top of `app/build.gradle.kts` (see BUILD-GRADLE-UPDATE.md). The About screen reads `BuildConfig.VERSION_NAME` and `BuildConfig.VERSION_CODE`; no hardcoded version strings anywhere (BLD-02).
* Git tags `v1.0.0`; pre-releases `v1.0.0-rc.N` (versionName `1.0.0-rc.N`, same code range plus N).
* Keep a Changelog format in `CHANGELOG.md`, updated in every PR that changes behavior.

## 2. CI behavior (workflow changes, WP-01)

Current workflow builds on `main` and `rebuild/nexus-2.0`, runs tests and `assembleDebug`, uploads `NEXUS-1.2.0-debug-apk`. Change:

1. Triggers: push to `main`, `rebuild/nexus-2.0`, tags `v*`; pull_request; workflow_dispatch.
2. Rename artifacts to `NEXUS-${versionName}-debug-apk` (read from Gradle: `gradle -q printVersionName` task or `grep` of the constant).
3. Debug APK on every run (debug-signed with the Android debug keystore: installable, not for the store).
4. Release job (only when the four secrets exist; otherwise skipped with a notice):
   * Secrets: `NEXUS_KEYSTORE_BASE64`, `NEXUS_KEYSTORE_PASSWORD`, `NEXUS_KEY_ALIAS`, `NEXUS_KEY_PASSWORD`.
   * Decode the keystore to `$RUNNER_TEMP/nexus.jks`, pass paths through Gradle properties `-Pnexus.keystore=... -Pnexus.keystorePassword=... -Pnexus.keyAlias=... -Pnexus.keyPassword=...`; `signingConfigs.create("release")` in `app/build.gradle.kts` is created only if these properties are present (so contributors and CI without secrets still build); release buildType uses it when present.
   * Run `:app:assembleRelease` (R8 and shrink resources already enabled) and verify with `apksigner verify` (build-tools on the runner).
   * Upload `app-release.apk` and `mapping.txt`; on tags create a GitHub Release (non-prerelease for `v1.0.0`, prerelease for `-rc`/`-preview`) with notes taken from the matching CHANGELOG section, attach the signed APK when present, otherwise the debug APK flagged "debug-signed".
5. Never print secrets; `::add-mask::` for Supabase values is already used. Keystore file is removed after the job.
6. Fonts download step stays (Amiri, Tajawal).

Optional (not required): Compose compiler metrics report artifacts for stability review.

## 3. Release checklist

1. All WPs merged, `FeatureStubs.kt` empty/removed, CI green.
2. CHANGELOG 1.0.0 dated; version constants correct.
3. Manual checklist of TESTING.md section 4 passed on the R8 release APK.
4. Tag `v1.0.0`; CI publishes the release.
5. Keystore: generate once (`keytool -genkeypair -v -keystore nexus.jks -alias nexus -keyalg RSA -keysize 4096 -validity 10000`), store only in a password manager and in repository secrets (base64). Losing it prevents updates of installed signed builds.

## 4. Owner action items (cannot be decided by the app; defaults in ADRs)

* Provide `WEB_AUTH_URL` (enables web-auth and share links) and the account-deletion page URL (ADR-0006); Play Store requires it.
* Server follow-ups (no change made by this project): verify RLS on `messages`, `blocked_users`, `post_reports`, `message_reports`, `notifications`, `profiles` (own-row update of `must_change_password`, `theme`), `post_reactions` CHECK values, `post_reports.status` CHECK, whether `admin_*` RPCs log to `admin_actions`, banned-word trigger error text, existence of `admin_broadcast` RPC, a self-delete RPC, `anonymous_authors` visibility (SEC-04/05).
* Provide a privacy policy URL and Play Data Safety answers (account, content, messages stored on Supabase; no analytics, no ads, no push in 1.0.0).
