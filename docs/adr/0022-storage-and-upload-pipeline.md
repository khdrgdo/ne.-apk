# ADR-0022: Storage and upload pipeline

Status: Accepted (2026-11-25)

## Context
SEC-14, X-17..X-24: path layout, size/type limits, token refresh before upload, orphans.

## Decision
As in ARCHITECTURE.md section 12. MIME allow lists: images png/jpeg/webp (gif rejected with a message, no conversion), course files pdf, doc(x), ppt(x), xls(x), txt, zip, images, mp4/webm/mov. File names are sanitized to `[A-Za-z0-9._-]` plus the original name kept in `title|||note` (CRS-38). `x-upsert: false`. Orphan cleanup best-effort only.

## Consequences
Predictable limits; very large video uploads rely on the upload client without call timeout.

Inventory open question: none
