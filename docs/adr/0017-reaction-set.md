# ADR-0017: Reaction set

Status: Accepted (2026-11-25)

## Context
Q3: whether `care`/`angry` are allowed by the CHECK constraint is unknown.

## Decision
Send only `like, love, haha, wow, sad` (the `Reaction` enum). Rows with other values are displayed as `like` and counted, never written. One reaction per user per post (toggle same = remove, other = replace as in 1.2.0).

## Consequences
Cannot violate the constraint.

Inventory open question: Q3
