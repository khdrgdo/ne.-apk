# ADR-0026: Testing strategy

Status: Accepted (2026-11-25)

## Context
No local compiler; CI compiles and runs JVM tests only.

## Decision
Pure-Kotlin rules (`Permissions`, `Tier`, `NotificationRouter`, `UrlValidator`, `Routes`, mappers in `Rows`) and ViewModels with fakes are tested with JUnit + coroutines-test; repositories with MockWebServer. No instrumented/Compose UI tests in CI for 1.0.0 (manual checklist in TESTING.md). A test per ViewModel for load/error/retry and each mutating action's rollback is the minimum bar.

## Consequences
Fast CI; UI regressions caught by checklist.

Inventory open question: none
