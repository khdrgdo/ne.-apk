# NEXUS 1.0.0: Testing

CI (GitHub Actions) is the only compiler and test runner: `:app:testDebugUnitTest :app:assembleDebug`. No instrumented tests run in CI (ADR-0026). Dependencies already allowed: JUnit 4.13.2, coroutines-test 1.9.0, MockWebServer 4.12.0, `org.json:json` (JVM test only).

## 1. Pyramid

| Level | What | Tools | Owner |
|---|---|---|---|
| Pure rules | `Permissions`, `Tier`, `NotificationRouter`, `UrlValidator`, `Routes`, `Limits`, `LoginIdentifier`, `DateFormat`, `PostgrestQuery`, `ReportReasons` | JUnit | WP-01 (+existing) |
| Data | Supabase repositories against `MockWebServer`: URLs, headers (apikey always, Bearer except auth), query building, masking, JSON mapping, error mapping, request counts (no N+1) | MockWebServer, real `ApiClient` with a fake `AuthTokenProvider` | WP-02/03/04 |
| ViewModel | state transitions, one-shot effects, single-flight, optimistic rollback, polling with virtual time | `runTest`, `MainDispatcherRule`, Fake repositories | each UI WP |
| Architecture | forbidden imports (`ui` -> `data`, `domain` -> android) | `ArchitectureRulesTest` scanning source files | WP-01 |
| Manual | device checklist (section 4) | emulator/device | owner before release |

## 2. Conventions

* Names: `<Class>Test`, test method names in backticks describing behavior.
* Dispatchers/scopes injected (guidance read in BEST-PRACTICES A4); `MainDispatcherRule` declared first.
* Fakes live in `app/src/test/java/com/nexus/app/testutil/` (`FakeAuthRepository`, ...); each data WP owns the fake of its interfaces, with `var nextError: ApiException?` and call counters.
* Time: inject `Clock`; never `System.currentTimeMillis()` in code under test.
* Test data uses Arabic names and ids with `+00:00` timestamps at least once per area (tolerant parsing).
* Each ViewModel test class minimum: initial load success, load failure + retry, empty, one mutation success, same mutation failure with rollback and message effect, double-tap ignored.

## 3. Required test inventory (acceptance for WPs)

* WP-01: Permissions matrix (admin, each sub_admin flag on/off, missing row, teacher, student, banned, suspended past/future, no role), route patterns, NotificationRouter table, UrlValidator (`https` ok; `http`, `javascript:`, `intent:`, `file:`, `data:`, userinfo URLs rejected), SwrCache TTL/eviction/clear, Backoff sequence 4/8/16/30, UiText mapping per `ApiErrorKind`.
* WP-02: login gate parsing, avatar validation (size/type/magic), storage path sanitising, pinned targeting, settings defaults and migration, `completePasswordChange` PATCH-failure path.
* WP-03: cursor filter strings (including equal timestamps), `[course:` exclusion, anonymous masking matrix for post/comment/search, material `title|||note`, leaderboard timeframe fallback.
* WP-04: conversations request count constant in N, `loadMessages` newest page then reversed, `loadNewerMessages` by cursor, DM fallback parses array, block matrix, notification id stability and masking, report merge and status mapping, account-creation partial failure.
* UI WPs: as listed in WORK-PACKAGES.md.

## 4. Manual release checklist (device, Arabic and English)

1. Fresh install login with student number; email login; sub-admin id; wrong password message; 429 message.
2. Account with `must_change_password` is forced to change; after change goes to Home.
3. Recovery link opens SetNewPassword; forged login-callback without valid state is rejected.
4. Banned and suspended accounts see the blocked screen (date shown); sign out works.
5. Offline: airplane mode shows banner, last data remains, retry works, chat message fails with retry then succeeds.
6. Feed paging, reaction, save, anonymous post (as student you see no author; as admin reveal works), report.
7. Course: upload PDF and video with progress/cancel; open file; add link with bad scheme rejected.
8. Chat: two devices, 4 s polling, block in DM and in group, unread badge, back goes to origin.
9. Admin: each tab as admin and as sub_admin with a single flag; protected account actions disabled.
10. Settings: theme system/light/dark, language ar/en (API 33 and API 30), dynamic color on API 31+, sign out clears data (reopen shows login).
11. Accessibility: TalkBack through login, feed card, chat, admin sheet; font scale 2.0 without clipping; 48dp targets; contrast spot check.
12. RTL: arrows and send icon mirrored, chat bubble sides, progress bars direction.
13. Release (R8) APK smoke test of steps 1, 6, 8 and sign-out.
