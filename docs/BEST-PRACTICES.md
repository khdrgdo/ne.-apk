# NEXUS 1.0.0: Researched best practices and the decision applied

Research date: 2026-11-25. Method: official documentation pages were fetched and read (marked **Read**). Where the fetch tool was rate-limited or a page was only located through search, the item is marked **Located** (URL known, content not read in this session) or **Judgment** (no authoritative source found; engineering judgment, stated as such). Nothing here is quoted from memory as if it were read.

## A. Architecture and state

| # | Practice (what the source says) | Source | Applied in NEXUS |
|---|---|---|---|
| A1 | **Read.** Strongly recommended: clear data layer holding business logic and UI layer; expose data through repositories even with one source; UDF (ViewModel exposes state, receives actions by method calls); coroutines/Flow between layers; collect with `collectAsStateWithLifecycle`; keep ViewModels free of Activity/Context/Resources; ViewModels at screen level only. Recommended: domain layer only when needed; single `uiState` StateFlow; avoid `AndroidViewModel`; Hilt for complex apps, manual DI fine for simple; map models per layer in complex apps. | https://developer.android.com/topic/architecture/recommendations | Layers and dependency rule (ARCH 2); repositories per area; ViewModels hold `UiText`, never Context (ARCH 15.2); manual DI (ADR-0001); no use-case layer, `domain.rules` for shared pure logic only; Supabase JSON mapped to domain models in `data/remote/Rows.kt`. |
| A2 | **Read.** Treat ViewModel events as UI state, consume them explicitly; avoid Channels/reactive streams for ViewModel to UI events because delivery is not guaranteed when the consumer is gone; navigation after validation is state the UI reacts to. | https://developer.android.com/topic/architecture/ui-layer/events | `EffectViewModel` with consumable `UiEffect` list (ADR-0011, ARCH 6, 15.2). Supersedes SPEC's SharedFlow suggestion. |
| A3 | **Read.** `collectAsStateWithLifecycle` collects while STARTED, configurable with `minActiveState`; `LaunchedEffect` is tied to composition, not lifecycle, so lifecycle-bound work should use lifecycle APIs; `stateIn(WhileSubscribed(5_000))` survives configuration change. | https://developer.android.com/topic/libraries/architecture/coroutines | Pollers run under `repeatOnLifecycle`/lifecycle effects, not bare `LaunchedEffect` (ARCH 11, ADR-0005); streams from repositories use `stateIn(WhileSubscribed(5000))`. |
| A4 | **Read.** Coroutine tests: `runTest`, inject dispatchers and scopes, one shared scheduler, `Dispatchers.setMain` via a rule, `backgroundScope` for endless coroutines. | https://developer.android.com/kotlin/coroutines/test | `DispatcherProvider` exists; ViewModels take dispatchers and scopes by constructor; `MainDispatcherRule` in test utilities (TESTING.md). Pollers are tested with virtual time. |
| A5 | **Read.** DataStore replaces SharedPreferences for new storage. | https://developer.android.com/training/data-storage/shared-preferences | Settings, recent searches, chat last-seen, drafts in DataStore. The only SharedPreferences left are the existing encrypted session store (format must not change) and a tiny locale mirror needed synchronously in `attachBaseContext` (ADR-0015). |

## B. Compose stability and performance

| # | Practice | Source | Applied |
|---|---|---|---|
| B1 | **Read.** Strong skipping is default since Kotlin 2.0.20: composables with unstable params become skippable (instance equality), lambdas are memoized. Caveat: annotating/immutable collections switches to element-wise equals, can be slower on big lists. | https://developer.android.com/develop/ui/compose/performance/stability/strongskipping | Project is on Kotlin 2.0.21: no `ImmutableList`, no blanket `@Immutable` (ADR-0010). |
| B2 | **Read.** Fixing stability: keep the same collection instance across emissions; use `@Immutable/@Stable` only for small classes with cheap equals that the compiler cannot infer; stability config file only when needed; do not over-optimize, measure first. | https://developer.android.com/develop/ui/compose/performance/stability/fix | ViewModels reuse unchanged list instances; compiler metrics optional flag in CI (WP-01), no stability config file. |
| B3 | **Read.** Best practices: `remember` for expensive work, stable unique `key` and `contentType` in lazy lists (no index/hashCode), `derivedStateOf` for fast-changing state, defer state reads with lambdas/lambda modifiers, avoid backwards writes. | https://developer.android.com/develop/ui/compose/performance/bestpractices | Rule in WORK-PACKAGES DoD: every `LazyColumn` item has `key = { it.id }` and `contentType`; scroll-dependent UI (FAB, scroll-to-bottom) uses `derivedStateOf`; transformations (sorting, grouping by day) are done in the ViewModel, not in composition. |

## C. Navigation

| # | Practice | Source | Applied |
|---|---|---|---|
| C1 | **Read.** Type-safe navigation (2.8.0+) requires `@Serializable` routes and the Kotlin Serialization plugin; `toRoute()` in composables and `SavedStateHandle.toRoute()` in ViewModels; it is more robust than string routes. | https://developer.android.com/guide/navigation/design/type-safety | Cannot adopt: the serialization plugin is outside the allowed dependencies. Mitigation: only `Routes` builders, String-only args, a route-pattern unit test (ADR-0003). Arguments read from `SavedStateHandle` by name inside ViewModel initializers. |
| C2 | **Located.** Deep links for Navigation Compose (`navDeepLink`). | https://developer.android.com/guide/navigation/design/deep-link | Not used in 1.0.0: the only external link is the login callback handled in `MainActivity`/`SessionManager`; in-app notification links go through `NotificationRouter` (ADR-0012). |

## D. Supabase and PostgREST

| # | Practice | Source | Applied |
|---|---|---|---|
| D1 | **Read.** RLS on every exposed table, policy per operation with `to` role, `anon` vs `authenticated` pitfalls, `auth.uid()` null for signed-out, wrap in `(select auth.uid())`, index policy columns, views bypass RLS unless `security_invoker`, `security definer` needs `search_path=''`, UPDATE needs a SELECT policy, service key bypasses RLS, never authorize with `user_metadata`. | https://supabase.com/docs/guides/database/postgres/row-level-security.md | The app only ever holds the publishable key (SEC-16). The server cannot be changed by this project, so the app (a) never sends `role` in metadata (ADR-0027), (b) treats all client-side permission checks as UX only (ADR-0002, ADR-0004), (c) maps RLS denial (`42501`, 401/403) to a safe message and rolls back. A "server follow-ups" list is kept in RELEASE.md: verify RLS on `messages`, `blocked_users`, `post_reports`, `notifications`, `profiles.must_change_password`. |
| D2 | **Read.** Refresh tokens are single use, with a 10 s reuse window; reuse outside it terminates the whole session; access tokens are short lived (default 1 h). | https://supabase.com/docs/guides/auth/sessions | `SessionManager` refreshes under one Mutex with a single in-flight refresh and persists the new pair before using it; a terminal refresh failure signs out (AUTH-10/21); tokens are refreshed before long uploads. Unit-tested (existing). |
| D3 | **Read.** PostgREST pagination: `limit/offset` or `Range` headers, `Content-Range` may be partial, `Prefer: count=exact|planned|estimated`, exact counts get slower on big tables. | https://docs.postgrest.org/en/v12/references/api/pagination_count.html | Keyset cursor for lists (ADR-0021) because offset shifts under inserts; `count=exact` with `Range: 0-0` only for small tables (admin counts); never infer totals from list length (X-13). |
| D4 | **Located** (search result list; page not read): PostgREST `Prefer: return=representation` semantics. https://docs.postgrest.org/en/stable/references/api/preferences.html | | Writes that need the created row (DM fallback, post create) send `Prefer: return=representation` and parse a JSON array (SPEC rule, report-data). |
| D5 | **Read.** Private bucket objects need a signed URL or an authenticated GET; signed URLs expire and cannot be revoked individually; CDN note applies to public buckets. | https://supabase.com/docs/guides/storage/serving | DB stores paths; `SignedUrlCache` (50 min) and batch `signedUrls`; re-sign once on image error (ARCH 12). Do not persist signed URLs. |
| D6 | **Located:** Storage security and signing keys. https://supabase.com/docs/guides/storage/security/signing-keys | | Nothing client-side; documented as server follow-up (rotate keys to revoke). |

## E. Chat polling

| # | Practice | Source | Applied |
|---|---|---|---|
| E1 | **Judgment** (no authoritative Android source on REST polling intervals). Poll only while visible, fetch deltas by monotonic cursor, merge by id, exponential backoff with cap, catch-up on resume/reconnect, optimistic send with retry. Combined with A3 for lifecycle gating. | n/a | ADR-0005 and ADR-0023: 4 s foreground delta polling, 4/8/16/30 s backoff, reconnecting strip, history newest-first, merge by id, failed bubbles with retry. |

## F. Accessibility, RTL, i18n

| # | Practice | Source | Applied |
|---|---|---|---|
| F1 | **Read.** Touch targets at least 48dp (`minimumInteractiveComponentSize`, `sizeIn`); `contentDescription` on Image/Icon, `null` for decorative; `clickable` merges descendants; `onClickLabel` for specific actions; custom toggles use `toggleable` modifiers. (Headings and contrast are not on that page.) | https://developer.android.com/develop/ui/compose/accessibility/api-defaults | ARCH 14 and the DoD checklist: 48dp targets, localized descriptions (avatar = person's name), `onClickLabel` on card rows, `toggleable/selectable` for chips, `semantics { heading() }` for section headers, contrast rule documented in ARCH 14 (WCAG 4.5:1 stated as project requirement A11Y-03, not sourced here). |
| F2 | **Judgment/knowledge, not read this session:** `Arrangement.Start/End` and `start/end` paddings are layout-direction aware (the fetched Compose layout page did not cover RTL); mirrored icons use `Icons.AutoMirrored`. Reference to verify: https://developer.android.com/develop/ui/compose/layouts/basics (no RTL content) and the API reference. | | ARCH 14: use start/end only, AutoMirrored icons, no forced RTL, test with `fontScale=2` and `ar`/`en` (TESTING.md manual checklist). |
| F3 | **Located:** per-app language and locale (the locale page returned an error when fetched). https://developer.android.com/develop/ui/compose/system/locale | | ADR-0015 uses platform `LocaleManager` (API 33+) plus context wrapping below because appcompat is not allowed; marked for manual verification (WP-10 checklist). |

## G. R8, release, testing

| # | Practice | Source | Applied |
|---|---|---|---|
| G1 | **Read.** Enable `isMinifyEnabled` and `isShrinkResources` for release; avoid broad keep rules; test the release build; full mode is stricter about reflection; keep mapping to retrace. | https://developer.android.com/topic/performance/app-optimization/enable-app-optimization | Already on in `build.gradle.kts`. The code base has no reflection or serialization library, so rules stay minimal (existing `proguard-rules.pro`); CI uploads `mapping.txt` with the release APK; manual smoke test of the release build is part of RELEASE.md. AGP is 8.7.3, so the legacy DSL stays. |
| G2 | **Read:** coroutine testing (A4). | as A4 | TESTING.md. |

## H. Decisions that go against or beyond the sources (and why)

1. String routes instead of type-safe routes (dependency rule). 2. Plain `List` collections (source B1/B2 itself recommends this under strong skipping). 3. No Hilt although the guidance suggests it for multi-screen apps: dependency rule and the container is small; recorded in ADR-0001. 4. No Paging library: keyset pagination written by hand (ADR-0021). 5. No offline database (ADR-0009).

## I. Unverified items to double-check on a device

Per-app language below API 33, TalkBack order on the chat list, dynamic color toggle on API 31+, predictive back animation on API 34+, release (R8) build smoke test.
