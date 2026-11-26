# ADR-0003: String routes with Routes builders instead of type-safe @Serializable routes

Status: Accepted (2026-11-25)

## Context
Navigation 2.8 type-safe routes need the kotlinx.serialization plugin and library, which the allowed-dependency rule forbids.

## Decision
Keep Navigation Compose 2.8.5 string routes. Every route and builder lives in `Routes`; arguments are Strings or nullable Strings only; features receive `NavActions` lambdas, never `NavController`. A unit test builds each route and matches it against its pattern. Revisit when serialization may be added.

## Consequences
No compile-time safety for argument names; mitigated by builders and the test. Argument types stay trivial so there are no NavType bugs.

Inventory open question: none
