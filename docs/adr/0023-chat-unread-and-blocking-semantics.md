# ADR-0023: Chat unread and blocking semantics

Status: Accepted (2026-11-25)

## Context
CHAT-13/26/09: no `last_read` column known; old block rule disabled whole groups.

## Decision
Unread is local: `LocalStateStore.chatSeen[conversationId] = created_at` of the newest message viewed; a conversation is unread when its last message sender is not me and its `updated_at` is newer than seen. Total unread conversations drives the bottom bar badge. Blocking: DMs: either direction blocks sending (banner differentiates 'you blocked' vs 'blocked you'). Groups: blocking never prevents sending; messages from blocked users are collapsed with 'message from a blocked user, tap to show'. Report sheet offers 'also block'.

## Consequences
Fixes the group bug; multi-device unread differs (documented).

Inventory open question: Q7
