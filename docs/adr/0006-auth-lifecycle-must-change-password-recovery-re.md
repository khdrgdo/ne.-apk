# ADR-0006: Auth lifecycle: must_change_password, recovery, registration, account deletion

Status: Accepted (2026-11-25)

## Context
Q1 and Q12: registration openness, initial passwords, recovery link behavior, self-delete RPC unknown.

## Decision
No self-registration in the app (login footer text explains accounts are issued by the administration or the website, AUTH-18). `must_change_password=true` gates the whole app with a forced change screen; success = PUT /auth/v1/user then PATCH own profile `must_change_password=false`; if the PATCH is refused the app stores a per-user local override so the user is not trapped (AUTH-14). Recovery: request through `/auth/v1/recover` with a neutral message; identifiers ending `@subadmin.edu` show 'contact the administrator' because the address is not deliverable (AUTH-11). A recovery deep link (`type=recovery` in the fragment) opens SetNewPassword without current password, then the session continues (AUTH-12, 28). The app never assumes the web app sends `state`: web-auth handoff keeps state/nonce validation from SessionManager (existing); recovery links carry no state and are accepted only when the tokens verify against `/auth/v1/user` and `type=recovery`. Account deletion: no RPC is assumed; Settings shows 'Request account deletion' which opens `<WEB_AUTH_URL>/account` (or text telling to contact the administration) and states the data retention; flag `accountSelfDelete` stays false (AUTH-19, SET-10). Provides the in-app path Google Play expects as a link, with a web URL to be provided before store release.

## Consequences
Safe against trapping users; recovery accepted only after token verification. The store-listing deletion URL is an owner action item (RELEASE.md).

Inventory open question: Q1, Q12
