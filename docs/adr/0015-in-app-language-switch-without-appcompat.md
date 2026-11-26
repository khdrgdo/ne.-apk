# ADR-0015: In-app language switch without appcompat

Status: Accepted (2026-11-25)

## Context
`AppCompatDelegate.setApplicationLocales` requires appcompat (not allowed).

## Decision
Persist `AppLanguage` in DataStore. API 33+: `LocaleManager.setApplicationLocales`. Below 33: `MainActivity.attachBaseContext` wraps the context with the stored locale (read synchronously from a tiny SharedPreferences mirror written by `SettingsStore`), and the Activity is recreated on change. Manifest declares `android:localeConfig` (res/xml/locales_config.xml: ar, en) so the system per-app language screen works. Default: Arabic.

## Consequences
Small amount of platform code; verified in the WP-10 checklist (not verified against docs in this session).

Inventory open question: none
