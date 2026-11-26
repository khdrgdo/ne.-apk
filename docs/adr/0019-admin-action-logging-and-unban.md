# ADR-0019: Admin action logging and unban

Status: Accepted (2026-11-25)

## Context
Q5: unknown whether `admin_*` RPCs write `admin_actions` and whether `admin_unban` lifts suspensions.

## Decision
The client does not write `admin_actions` (SEC-05: server authority) by default: `AdminRepository.logAdminAction` exists but is OFF (`adminClientLogging=false`) until the owner confirms the RPCs do not log. The Activity tab only reads. 'Lift restriction' and 'unban' are both the single `admin_unban` call; after success the profile is re-read; if `suspended_until` is still in the future the UI shows the server state and a notice (no second RPC).

## Consequences
No duplicate or forged log rows.

Inventory open question: Q5
