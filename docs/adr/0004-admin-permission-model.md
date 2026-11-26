# ADR-0004: Admin permission model

Status: Accepted (2026-11-25)

## Context
Q9/Q10: inconsistent flag keys and defaults in the old app (`warn` vs `can_warn`, defaults true in lists but false in loadProfile); unknown whether flags are enforced server-side.

## Decision
Roles: `admin` can do everything; `sub_admin` only what `subadmin_permissions` grants; `teacher` and `student` have no admin access. Missing row or null flag means DENY (false). Flags map 1:1 to columns (`can_warn, can_suspend, can_courses, can_reports, can_words, can_teachers`) via `SubAdminPermission.column`; no other key is used. Tab visibility: Overview and Activity (read) for any admin/sub_admin; Users shows warn and suspend controls per flag; ban, unban, verify, adjust points, set year, delete user, role change, sub-admin management, pinned, broadcast are admin only; Reports needs `can_reports`; Courses `can_courses`; Teachers `can_teachers`; Words `can_words`. Anonymous reveal: admin, or sub_admin with `can_reports`. Protected accounts: a sub_admin cannot act on admin or other sub_admin users; nobody can act on themselves (delete, ban). New sub-admin accounts default to all flags false until the admin ticks them (ADM-47). Flags are refreshed on resume (ADM-43). The UI is a convenience; the server decides, errors map to `err_forbidden`. Implemented once in `domain.rules.Permissions` with unit tests (ADM-42).

## Consequences
One truth source; tests lock the matrix. A sub_admin without `can_reports` sees no report tab even if RLS would allow it (conservative, matches the audit note that live RLS blocks it).

Inventory open question: Q9, Q10
