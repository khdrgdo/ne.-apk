# ADR-0025: Version, signing and release

Status: Accepted (2026-11-25)

## Context
BLD-02/06: version must come from one source and releases must be installable.

## Decision
1.0.0 (code 10000). Single source constants in `app/build.gradle.kts`; About reads `BuildConfig.VERSION_NAME/CODE`. CI builds a debug-signed APK on every push, and on tags `v*` publishes a GitHub Release. If repository secrets `NEXUS_KEYSTORE_BASE64, NEXUS_KEYSTORE_PASSWORD, NEXUS_KEY_ALIAS, NEXUS_KEY_PASSWORD` exist, a signed `assembleRelease` (R8 on) is produced and attached; otherwise only the debug APK is attached with a clear note. Details in RELEASE.md.

## Consequences
Works with or without a release keystore.

Inventory open question: none
