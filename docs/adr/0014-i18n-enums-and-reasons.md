# ADR-0014: i18n: enums and reasons

Status: Accepted (2026-11-25)

## Context
I18N-05/06: domain enums carry Arabic labels; reasons are sent to the server as Arabic text.

## Decision
UI never displays `ThemeMode.label`, `SubAdminPermission.label`, `majorLabel` literals; it uses string-resource mappers in `ui/common/Labels.kt`. Report reasons: the stored value is a fixed canonical Arabic string from `ReportReasons` regardless of UI language (so the admin sees one vocabulary); the displayed text is localized. Server messages that are already Arabic are shown as-is.

## Consequences
Admin screens see Arabic reason strings in English UI; acceptable and documented.

Inventory open question: none
