# ADR-0020: Banned words enforcement

Status: Accepted (2026-11-25)

## Context
Q6: where words are enforced and the error text are unknown.

## Decision
Enforcement is server-side. The client shows server error messages that contain Arabic as-is (ADM-29) and otherwise a generic 'content not allowed' when the error code is a check/trigger violation (`P0001`, `23514`). No client-side word filtering. Admin CRUD of words per ADM-28, minimum 2 chars, trimmed, duplicates rejected case-insensitively.

## Consequences
Single enforcement point.

Inventory open question: Q6
