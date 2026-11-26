# ADR-0012: Deep links, App Links and sharing

Status: Accepted (2026-11-25)

## Context
Q8/Q12: web link patterns and domain unknown; NAV-05/07 are P2/P3.

## Decision
Only `com.nexus.app://login-callback` is registered (host and scheme strictly validated). In-app sharing builds `<shareBaseUrl>/posts/<id>` only when `FeatureFlags.shareBaseUrl` is set (derived from WEB_AUTH_URL origin), else shares the post text with the app name. Notification links of the forms listed in ARCHITECTURE.md 7.3 are routed inside the app. App Links (verified https) and ACTION_SEND receiving are deferred to post-1.0 pending the domain and assetlinks file.

## Consequences
No dead links. One owner item: confirm the web path patterns.

Inventory open question: Q8, Q12
