# ADR-0005: Chat uses 4 second foreground polling, no Realtime

Status: Accepted (2026-11-25)

## Context
Q7: Realtime/push not known to be enabled; SPEC requires polling every 4 s.

## Decision
While the chat screen is RESUMED and the device online: poll `created_at=gt.<lastCreatedAt>` every 4 s (single request, merge by id, no list replace). Backoff 4/8/16/30 s on failure, 'reconnecting' strip after 2 failures, immediate catch-up fetch on resume or connectivity regained. Conversation list refreshes every 15 s on the Messages tab; unread totals every 60 s while the app is started. Optimistic send with temp id, failed bubbles offer retry. No WorkManager/foreground service. History: newest page (50) then older on scroll (fixes the old 'oldest 300' bug).

## Consequences
Battery cost only while the screen is open. Latency up to 4 s. Realtime is a post-1.0 candidate (BE-12) behind the same repository interface.

Inventory open question: Q7
