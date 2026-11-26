# ADR-0018: Report statuses and report targets

Status: Accepted (2026-11-25)

## Context
Q3/Q11: allowed `status` values and existence of comment/user report tables unknown; name-change table absent.

## Decision
Statuses written by the app: `pending` (default), `reviewed`, `resolved`, `dismissed` (the `ReportStatus` enum); the legacy `confirmed` is never sent, and an unknown value read from the server displays as pending with the raw text hidden. Report targets in 1.0.0: posts (`post_reports`) and chat messages/users (`message_reports`). Comment and user reports from the UI are hidden (flag `commentAndUserReports=false`; FEED-19 partially deferred; a comment report is NOT overloaded onto a post report reason). Name-change requests: flag off, no UI (PROF-06, ADM-34).

## Consequences
Accurate data; owner decides on the schema.

Inventory open question: Q3, Q11
