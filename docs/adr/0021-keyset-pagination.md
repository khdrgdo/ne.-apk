# ADR-0021: Keyset pagination

Status: Accepted (2026-11-25)

## Context
Offset pagination degrades and duplicates under inserts; PostgREST caps rows at max-rows.

## Decision
`created_at` plus `id` cursor (`<ts>|<id>`), `or=(created_at.lt.<ts>,and(created_at.eq.<ts>,id.lt.<id>))` built through `PostgrestQuery`. Counts via `Prefer: count=exact` and `Range: 0-0`. Page sizes in ARCHITECTURE.md section 10.

## Consequences
Stable lists; slightly more complex filter, covered by unit tests.

Inventory open question: none
