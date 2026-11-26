# ADR-0024: Pinned cards

Status: Accepted (2026-11-25)

## Context
Q8: `theme` values and image bucket unknown; PIN-06 vote privacy.

## Decision
Targeting by `target_year`/`target_major` on the client against the profile (null = everyone). Unknown `theme` falls back to the default Direction A card style; known keys mapped in one `when` with a default branch. Image path resolved through `signedUrl` against `post-images` first, then treated as an absolute https URL if it validates. The client shows only aggregated results the server returns (own vote highlighted); it never lists other voters. Action button URL passes `UrlValidator`. Votes are optimistic with rollback (PIN-12).

## Consequences
Safe against unknown data.

Inventory open question: Q8
