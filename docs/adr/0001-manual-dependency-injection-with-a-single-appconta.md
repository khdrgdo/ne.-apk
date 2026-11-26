# ADR-0001: Manual dependency injection with a single AppContainer

Status: Accepted (2026-11-25)

## Context
Hilt/Koin are not in the allowed dependency list and the app has one process and about 12 repositories. The old app created repositories in several places (AUTH-29).

## Decision
One `AppContainer` owned by `NexusApplication` exposes every repository by interface-typed `val`. ViewModels are built with `viewModelFactory { initializer { } }` in each screen's Route composable and receive interfaces only. Exact shape in ARCHITECTURE.md 15.1. Android guidance accepts manual DI for simple apps.

## Consequences
Easy to read, no codegen. Discipline needed: a ViewModel must never take `AppContainer`. Tests use fakes.

Inventory open question: none (design)
