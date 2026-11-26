# ADR-0008: Notifications: synthesized feed, local read state, no push in 1.0.0

Status: Accepted (2026-11-25)

## Context
Q7: no FCM/device table. The old app builds notifications from `notifications` plus synthesized comment/reaction/message/warning items with local read state.

## Decision
Keep the contract: `notifications` table rows plus synthesized items with stable ids (`comment_`, `react_`, `msg_`, `warn_` + source id). Read/deleted state of synthesized items lives in `LocalStateStore` (DataStore) keyed per user (cleared at sign-out). Synthesis is batched (one request per source type for the last 30 days, not per post) and cached 60 s. Unread count from `observeUnreadCount` polls every 60 s. Priority `important` is derived from `announcement` type or admin warnings. No push, no POST_NOTIFICATIONS permission, no background work (NOTIF-09/10/11 deferred, in-app only). Message-derived items use type `message`, never `comment_reply`.

## Consequences
Predictable cost; read state on one device only (documented).

Inventory open question: Q7
