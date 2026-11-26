# ADR-0007: Anonymity handling

Status: Accepted (2026-11-25)

## Context
Anonymous posts/comments (`is_anonymous || author_id == null`) leaked authorId, points, verified, major in the old app (SEC-04, NOTIF-17, SRCH-15).

## Decision
The data layer masks at the boundary: every repository that returns posts, comments, search results, notifications or profiles-of-authors applies `forViewer(viewerIsStaff)` before returning, so UI code never sees masked data. `isMyAnonymous` is true only when the session user is the author (so the owner can delete). Notifications about anonymous actors use a neutral actor name and no actor id. Staff reveal is an explicit call `FeedRepository.revealAnonymousAuthors` that uses RPC `get_anonymous_authors` and is exposed only to `Permissions.canRevealAnonymous`; the reveal is shown with `AnonymousRevealRow`. Search never matches by author name for anonymous content. The server remains the final guard: if it returns author ids to ordinary users, the client still masks.

## Consequences
UI cannot leak by mistake; one place to audit. Cost: repositories need the viewer role (injected via `currentProfile`).

Inventory open question: none
