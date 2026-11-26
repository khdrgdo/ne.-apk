# ADR-0002: Unknown RLS: client guards plus mapped server errors

Status: Accepted (2026-11-25)

## Context
Inventory section 23 Q2/Q9/Q10: we cannot know which writes RLS allows (edit post/comment, delete message, leave/rename group, theme without points, insert notifications, sub-admin reading reports).

## Decision
1) Parity actions known to work in 1.2.0 are enabled. 2) Actions the old app never performed (edit post/comment, delete message, leave/rename group, comment and user reports, name-change requests, self account delete) are implemented in the data layer but hidden behind `FeatureFlags` (default false). 3) The client never relies on RLS for UX: `Permissions` hides controls by role and ownership, the server still enforces. 4) A 401/403/42501 response is mapped to a safe Arabic/English message (`err_forbidden`) and the optimistic change is rolled back. 5) No client code inserts rows on behalf of other users (SEC-05): ids like `user_id`, `sender_id` come from the session only and are re-validated server-side. 6) Flags are flipped in `FeatureFlags` once the owner confirms the policy.

## Consequences
Safe default: nothing silently breaks; maximum parity. Cost: a few features ship dark. Owner can enable each flag with a one-line change.

Inventory open question: Q2, Q9, Q10
