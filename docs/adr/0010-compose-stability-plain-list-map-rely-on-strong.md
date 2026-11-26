# ADR-0010: Compose stability: plain List/Map, rely on strong skipping

Status: Accepted (2026-11-25)

## Context
Kotlin 2.0.21 enables strong skipping by default; official guidance says immutable collections and annotations can cost more than they save.

## Decision
UiState holds `List<T>`. ViewModels reuse list instances when unchanged. No `@Immutable/@Stable` unless a profile shows a regression (then only on small leaf models). Lazy lists always pass `key` and `contentType`. State reads are deferred (`derivedStateOf`, lambda-based modifiers) for scroll-driven UI. No stability config file.

## Consequences
Simplest code; if recomposition problems appear, use the Compose compiler reports (optional CI flag added by WP-01).

Inventory open question: none
