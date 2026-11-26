# NEXUS docs (1.0.0)

Architecture, decisions, plans and test/release procedures for the NEXUS Android app (Kotlin, Compose, Supabase, Arabic RTL).

| Document | Purpose |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | layers, packages, dependency rule, data flow, DI, UDF, navigation (all routes), error model, caching, pagination, polling, files, security, theming/i18n/a11y, and section 15 **Contracts** (AppContainer, UiText, common composables, routes, rules) |
| [BEST-PRACTICES.md](BEST-PRACTICES.md) | researched practices with source URLs and the decision applied (read vs located vs judgment is stated) |
| [adr/](adr/README.md) | 27 architecture decision records, including a safe default for every open question of inventory section 23 |
| [SCREEN-MAP.md](SCREEN-MAP.md) | every screen and every one of the 432 inventory IDs mapped to route, ViewModel, repositories and owning work package |
| [WORK-PACKAGES.md](WORK-PACKAGES.md) | 12 independent work packages with files, interfaces, order and definition of done |
| [TESTING.md](TESTING.md) | test strategy, required tests, manual checklist |
| [RELEASE.md](RELEASE.md) | versioning, CI signing/release, checklists, owner action items |
| [BUILD-GRADLE-UPDATE.md](BUILD-GRADLE-UPDATE.md) | exact version and signing edits for versionName 1.0.0 / versionCode 10000 |
| [CHANGELOG.md](CHANGELOG.md) | changelog starting at 1.0.0 |
| [FEATURE-INVENTORY.md](FEATURE-INVENTORY.md) | authoritative 432-feature inventory (Arabic) |

## Inventory section 23: open questions and where they are decided

| Q | Topic | ADR |
|---|---|---|
| 1 | Registration, account creation, self-delete | 0006, 0027 |
| 2 | Unknown RLS capabilities | 0002 |
| 3 | Reactions set, report statuses, comment/user reports | 0017, 0018 |
| 4 | Points and periods | 0016 |
| 5 | admin_actions logging, unban semantics | 0019 |
| 6 | Banned words enforcement | 0020 |
| 7 | Push, Realtime, chat read state | 0005, 0008, 0023 |
| 8 | Web link patterns, pinned theme/image | 0012, 0024 |
| 9 | Sub-admin access to reports, messaging | 0002, 0004 |
| 10 | Sub-admin flags enforcement | 0004 |
| 11 | Name-change requests | 0018 |
| 12 | WEB_AUTH_URL, recovery state | 0006, 0012 |

Reading order for an implementer: ARCHITECTURE.md (esp. 15) -> your WP in WORK-PACKAGES.md -> your rows in SCREEN-MAP.md -> the ADRs named there.
