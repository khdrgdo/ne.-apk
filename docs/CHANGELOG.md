# Changelog

All notable changes to NEXUS for Android. Format: Keep a Changelog; versions follow SemVer (see docs/RELEASE.md).

## [1.0.0] - Unreleased

First release of the rebuilt app (replaces the 1.2.0 line and the 2.0.0-preview.1 builds; uninstall preview builds before installing: versionCode is lower than 20000).

### Added
- New architecture: layered (ui/domain/data/core), manual DI container, unidirectional data flow, string-route navigation with typed builders.
- Direction A ("الجريدة") design system, Arabic-first RTL UI with English translation, light/dark/system theme, language switch.
- Auth completion: forced password change, password recovery link flow, banned/suspended screens, web-auth handoff with state/nonce validation, session restore that survives transient errors.
- Home with pinned cards (poll, participation, event), courses with materials upload (progress/cancel), discussions, announcements and schedule.
- Community feed with infinite scroll, composer (up to 4 images), anonymous posting, reactions, save, report, share, post detail with comment tree.
- Chat: newest-first history, 4 s polling with backoff, optimistic send with retry, unread badge, block/report, groups.
- Notifications with filters, day grouping, swipe delete; global search with recents; leaderboard with podium and my rank.
- Profile and public profile, avatar upload/remove, bio, profile frame, warnings, settings, blocked users, about.
- Admin panel: overview, users, reports, courses, teachers, sub-admins (flag defaults deny), banned words, activity, pinned card, broadcast, content digest.
- Stale-while-revalidate in-memory caching, offline banner, skeleton loaders, error states with retry.

### Fixed (vs 1.2.0)
- Chat showed the oldest 300 messages; DM fallback broken; group blocked by one blocked member; wrong "blocked by me" state.
- `verified` flag bug; anonymous authors leaking id/points/major through posts, search and notifications.
- Session wiped on transient decrypt errors; logout signed out all devices; deep link accepted without state/nonce; no refresh before upload.
- PostgREST filter injection through unescaped search text; unsafe link schemes.
- `must_change_password` ignored; recovery link ignored `type=recovery`.
- Admin flag key/default inconsistencies; report status values outside the allowed set.

### Security
- Cleartext disabled, backups disabled, R8 enabled, tokens and signed URLs never logged.

### Known limitations
- No push notifications, no offline disk cache, no Realtime (polling).
- Some actions are behind feature flags until server policies are confirmed (see ADR-0002): post/comment edit, group management, comment/user reports, name-change requests, in-app account deletion.
