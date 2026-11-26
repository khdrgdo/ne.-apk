# ADR-0027: Admin account creation and destructive actions

Status: Accepted (2026-11-25)

## Context
SEC-10, ADM-23/25/36: the old app called signup from the client with role in metadata.

## Decision
Creating teacher/sub-admin/student accounts uses GoTrue signup with the admin's own session NOT replaced (a separate call with the publishable key and no session adoption), then `admin_set_user_role`/`admin_set_teacher_role` RPC to assign roles; `role` is never sent in user metadata. The sub-admin login id is stored as `sub_<id>` in `profiles.university_number` and the email is `<id>@subadmin.edu`; `sub_` prefix is stripped before building the email. If role assignment fails the new account is reported as 'created without role' with a retry button (no silent success). Initial passwords are generated client-side (12 chars, random) and shown once with copy; the account is flagged `must_change_password` by the admin RPC if available, otherwise the server default applies. Destructive actions (delete user) require typed confirmation of the user's university number.

## Consequences
Honest partial-failure reporting; no role escalation through metadata.

Inventory open question: Q1
