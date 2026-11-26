# ADR-0013: No new dependencies for 1.0.0

Status: Accepted (2026-11-25)

## Context
SPEC list is sufficient; each library adds size, R8 rules and risk without a local compiler.

## Decision
No additions. Explicitly not added: appcompat (see ADR-0015), kotlinx-serialization (ADR-0003), Room, Hilt, WorkManager, FCM, Paging, lifecycle-process, accompanist, immutable collections. WP-01 verifies that `lifecycle-viewmodel-compose` resolves (the existing shell already calls `viewModel()`; if CI shows it missing, add it as the same-family artifact `androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7` and record it here).

## Consequences
Less capability (no Paging, no offline DB); everything is hand-rolled but small.

Inventory open question: none
