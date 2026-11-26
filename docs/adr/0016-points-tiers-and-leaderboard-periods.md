# ADR-0016: Points, tiers and leaderboard periods

Status: Accepted (2026-11-25)

## Context
Q4: points formula unknown; week/month RPC unknown.

## Decision
Points are displayed only (server-owned; admin adjusts via RPC). Tier thresholds in `Tier.kt` (0, 100, 300, 700, 1500 points = 5 tiers, names string resources) are a client presentation choice documented as such. Leaderboard ALL uses `get_leaderboard_profiles`; WEEK/MONTH are labelled 'activity' and computed from `contributions` in window when the RPC supports it, else the chips are hidden (feature detection: first call failing with 404/400 means unsupported, remembered for the session). Admin/sub_admin/teacher accounts are excluded by role (not by name, SEC-12).

## Consequences
Honest labeling; no invented point math.

Inventory open question: Q4
