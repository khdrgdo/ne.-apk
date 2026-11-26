# ADR-0011: One-shot UI events as consumed state

Status: Accepted (2026-11-25)

## Context
Android UI-events guidance prefers state with explicit consumption over Channel/SharedFlow to avoid lost events.

## Decision
`EffectViewModel` keeps `effects: StateFlow<List<UiEffect>>`; the screen shows/handles them and calls `consume(id)`. SPEC mentions SharedFlow; this ADR supersedes it for new code. Snackbars use liveRegion for TalkBack (A11Y-08).

## Consequences
Events survive rotation; slightly more boilerplate, centralized in the base class.

Inventory open question: none
