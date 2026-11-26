# ADR-0009: Offline behavior: in-memory stale-while-revalidate, no disk cache

Status: Accepted (2026-11-25)

## Context
Spec requires offline-tolerant UX; Room is forbidden and content is large.

## Decision
`SwrCache` in memory per repository; screens show last data while refreshing, a `stale` hint and an OfflineBanner when `ConnectivityMonitor` reports offline (ConnectivityManager network callback, needs ACCESS_NETWORK_STATE). Writes while offline fail fast with `err_network` except chat messages, which are queued in memory (failed bubble with retry, CHAT-24 partial). Settings and drafts persist via DataStore. Everything cleared at sign-out.

## Consequences
After process death the app starts empty until online. Accepted for 1.0.0.

Inventory open question: none
