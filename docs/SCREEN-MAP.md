# NEXUS 1.0.0: Screen map and inventory coverage

Part 1 lists every screen (route, ViewModel, repositories, owner). Part 2 lists every one of the 432 inventory IDs (P0 to P3) with its place, the work package that owns the UI, the one that owns the data/repository part, and its 1.0.0 status. WP names are defined in WORK-PACKAGES.md: WP-01 Foundations, WP-02/03/04 Data A/B/C, WP-05 Auth, WP-06 Home+Pinned, WP-07 Courses, WP-08 Feed, WP-09 Chat, WP-10 Profile+Settings, WP-11 Notifications+Search+Leaderboard, WP-12 Admin. 'Deferred' rows are conscious scope decisions (ADR in the status text or post-1.0 backlog); 'guarded' rows are implemented behind a FeatureFlag. Feature names are in FEATURE-INVENTORY.md (same IDs).

## Part 1. Screens

| Screen | Route | ViewModel | Repositories (interfaces) | UI WP | Data WP |
|---|---|---|---|---|---|
| Login, recover sheet, web-auth | gate: SignedOut | AuthViewModel | SessionManager, AuthRepository | WP-05 | WP-02 |
| SetNewPassword (recovery) | gate: Recovery | ChangePasswordViewModel(mode=recovery) | AuthRepository | WP-05 | WP-02 |
| ChangePassword (forced / settings) | gate: MustChangePassword, `settings/password` | ChangePasswordViewModel | AuthRepository | WP-05 | WP-02 |
| AccountBlocked | gate: Blocked | AppGate state | ProfileRepository, SessionManager | WP-05 | WP-02 |
| Home | `home` | HomeViewModel, PinnedCardViewModel | CourseRepository, PinnedCardRepository, NotificationRepository | WP-06 | WP-03 / WP-02 |
| Courses list | `courses` | CoursesViewModel | CourseRepository | WP-07 | WP-03 |
| Course detail (files, links, discussions, updates, schedule) | `course/{courseId}?tab=` | CourseDetailViewModel | CourseRepository, StorageRepository | WP-07 | WP-03 / WP-02 |
| Feed | `community` | FeedViewModel | FeedRepository, PinnedCardRepository | WP-08 | WP-03 |
| Composer | `compose?course=&type=` | ComposerViewModel | FeedRepository, CourseRepository (question), StorageRepository | WP-08 | WP-03 / WP-02 |
| Post detail | `post/{postId}?comment=` | PostDetailViewModel | FeedRepository | WP-08 | WP-03 |
| Saved | `saved` | SavedViewModel | FeedRepository | WP-08 | WP-03 |
| Messages (conversations) | `messages` | MessagesViewModel | MessagingRepository | WP-09 | WP-04 |
| Find users | `messages/new` | FindUsersViewModel | SearchRepository, MessagingRepository | WP-09 | WP-03 / WP-04 |
| New group | `messages/new-group` | NewGroupViewModel | MessagingRepository, SearchRepository | WP-09 | WP-04 |
| Chat (+ group info, block, report sheets) | `chat/{conversationId}` | ChatViewModel | MessagingRepository | WP-09 | WP-04 |
| Profile (own) | `profile` | ProfileViewModel | ProfileRepository, StorageRepository | WP-10 | WP-02 |
| Public profile | `user/{userId}` | PublicProfileViewModel | ProfileRepository, MessagingRepository (start DM), FeedRepository | WP-10 | WP-02 / WP-03 |
| Warnings | `warnings` | WarningsViewModel | ProfileRepository | WP-10 | WP-02 |
| Settings | `settings` | SettingsViewModel | SettingsRepository, AuthRepository | WP-10 | WP-02 |
| Blocked users | `settings/blocked` | BlockedUsersViewModel | MessagingRepository | WP-10 | WP-04 |
| About | `settings/about` | none (BuildConfig) | none | WP-10 | - |
| Notifications | `notifications` | NotificationsViewModel | NotificationRepository | WP-11 | WP-04 |
| Search | `search?q=` | SearchViewModel | SearchRepository, SettingsRepository | WP-11 | WP-03 / WP-02 |
| Leaderboard | `leaderboard` | LeaderboardViewModel | LeaderboardRepository, ProfileRepository | WP-11 | WP-03 |
| Admin home / overview | `admin` | AdminHomeViewModel | AdminRepository, ProfileRepository | WP-12 | WP-04 |
| Admin users + user detail | `admin/users`, `admin/users/{userId}` | AdminUsersViewModel, AdminUserDetailViewModel | AdminRepository | WP-12 | WP-04 |
| Admin reports + detail | `admin/reports`, `admin/reports/{reportId}?type=` | AdminReportsViewModel, AdminReportDetailViewModel | AdminRepository, FeedRepository, MessagingRepository | WP-12 | WP-04 / WP-03 |
| Admin courses + editor | `admin/courses`, `admin/courses/edit?courseId=` | AdminCoursesViewModel, CourseEditorViewModel | AdminRepository, CourseRepository | WP-12 | WP-04 / WP-03 |
| Admin teachers | `admin/teachers` | AdminTeachersViewModel | AdminRepository | WP-12 | WP-04 |
| Admin sub-admins | `admin/subadmins` | AdminSubAdminsViewModel | AdminRepository | WP-12 | WP-04 |
| Admin words | `admin/words` | AdminWordsViewModel | AdminRepository | WP-12 | WP-04 |
| Admin activity | `admin/activity` | AdminActivityViewModel | AdminRepository | WP-12 | WP-04 |
| Admin pinned card | `admin/pinned` | AdminPinnedViewModel | AdminRepository, PinnedCardRepository | WP-12 | WP-04 / WP-02 |
| Admin broadcast | `admin/broadcast` | AdminBroadcastViewModel | AdminRepository | WP-12 | WP-04 |
| Admin content digest | `admin/content` | AdminContentViewModel | AdminRepository | WP-12 | WP-04 |
| Not found | `not-found?what=` | none | none | WP-01 | - |
| Shell (bottom bar, badges, offline banner, effect host) | `MainShell` | MainShellViewModel | NotificationRepository, MessagingRepository, ConnectivityMonitor | WP-01 | WP-04 |

## Part 2. Every inventory ID

Place = default screen group of the ID prefix (see Part 1) unless a specific place is given. 'all feature WPs' = every UI work package must satisfy it (DoD-C).

| ID | P | Place | UI WP | Data WP | 1.0.0 status |
|---|---|---|---|---|---|
| AUTH-01 | P2 | MainActivity splash (core-splashscreen) | WP-01 | - | 1.0.0 |
| AUTH-02 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-03 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-04 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-05 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-06 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-07 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-08 | P0 | core/session (exists; verify + tests) | WP-01 | - | 1.0.0 |
| AUTH-09 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-10 | P0 | core/session (exists; verify + tests) | WP-01 | - | 1.0.0 |
| AUTH-11 | P1 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-12 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-13 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-14 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-15 | P1 | ChangePasswordScreen (also settings/password) | WP-05 | WP-02 | 1.0.0 |
| AUTH-16 | P0 | SettingsScreen | WP-10 | WP-02 | 1.0.0 |
| AUTH-17 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-18 | P2 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-19 | P1 | SettingsScreen | WP-10 | WP-02 | 1.0.0 guarded: ADR-0006 |
| AUTH-20 | P3 | SettingsScreen | WP-10 | WP-02 | Deferred: post-1.0 backlog |
| AUTH-21 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-22 | P2 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-23 | P3 | SettingsScreen | WP-10 | WP-02 | Deferred: post-1.0 backlog |
| AUTH-24 | P2 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-25 | P3 | Auth gate screens | WP-05 | WP-02 | Deferred: post-1.0 backlog |
| AUTH-26 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-27 | P1 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-28 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-29 | P0 | AppContainer.onSignedOut | WP-01 | - | 1.0.0 |
| AUTH-30 | P1 | AppContainer.onSignedOut | WP-01 | - | 1.0.0 |
| AUTH-31 | P2 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-32 | P1 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-33 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| AUTH-34 | P0 | Auth gate screens | WP-05 | WP-02 | 1.0.0 |
| PROF-01 | P0 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-02 | P1 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-03 | P0 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-04 | P2 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-05 | P1 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-06 | P2 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 guarded: ADR-0018 flag off |
| PROF-07 | P1 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-08 | P1 | domain/rules/Tier.kt | WP-01 | - | 1.0.0 |
| PROF-09 | P0 | ui/common VerifiedBadge | WP-01 | - | 1.0.0 |
| PROF-10 | P1 | ui/common RoleBadge | WP-01 | - | 1.0.0 |
| PROF-11 | P0 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-12 | P1 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-13 | P1 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-14 | P1 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-15 | P2 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-16 | P0 | ProfileScreen to `admin` | WP-10 | - | 1.0.0 |
| PROF-17 | P1 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-18 | P1 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-19 | P1 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-20 | P2 | WarningsScreen `warnings` | WP-10 | WP-02 | 1.0.0 |
| PROF-21 | P3 | Profile / PublicProfile | WP-10 | WP-02 | Deferred: backlog |
| PROF-22 | P2 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-23 | P3 | Profile / PublicProfile | WP-10 | WP-02 | Deferred: backlog |
| PROF-24 | P2 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-25 | P1 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-26 | P1 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-27 | P0 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-28 | P1 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-29 | P2 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-30 | P1 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-31 | P2 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| PROF-32 | P1 | Profile / PublicProfile | WP-10 | WP-02 | 1.0.0 |
| HOME-01 | P1 | Home | WP-06 | WP-03 | 1.0.0 |
| HOME-02 | P1 | Home | WP-06 | WP-03 | 1.0.0 |
| HOME-03 | P1 | Home | WP-06 | WP-03 | 1.0.0 |
| HOME-04 | P1 | Home | WP-06 | WP-03 | 1.0.0 |
| HOME-05 | P1 | Home | WP-06 | WP-03 | 1.0.0 |
| HOME-06 | P0 | Home | WP-06 | WP-03 | 1.0.0 |
| HOME-07 | P2 | Home | WP-06 | WP-03 | 1.0.0 |
| HOME-08 | P2 | Home | WP-06 | WP-03 | 1.0.0 |
| HOME-09 | P2 | Home | WP-06 | WP-03 | 1.0.0 |
| HOME-10 | P1 | Home | WP-06 | WP-03 | 1.0.0 |
| CRS-01 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-02 | P1 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-03 | P1 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-04 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-05 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-06 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-07 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-08 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-09 | P1 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-10 | P1 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-11 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-12 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-13 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-14 | P2 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-15 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-16 | P0 | FeedRepository/SearchRepository query filter | WP-08 | WP-03 | 1.0.0 |
| CRS-17 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-18 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-19 | P1 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-20 | P1 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-21 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-22 | P1 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-23 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-24 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-25 | P0 | Routes.COURSE + NotificationRouter | WP-01 | - | 1.0.0 |
| CRS-26 | P2 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-27 | P3 | Courses / CourseDetail | WP-07 | WP-03 | Deferred: backlog |
| CRS-28 | P3 | Courses / CourseDetail | WP-07 | WP-03 | Deferred: ADR-0022 |
| CRS-29 | P1 | CoursesScreen (peekCourses) | WP-07 | WP-03 | 1.0.0 |
| CRS-30 | P2 | server-side | - | WP-04 | Deferred: server-side (ADR-0002) |
| CRS-31 | P0 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-32 | P2 | n/a (no enrolment in contract) | - | - | Deferred: no contract |
| CRS-33 | P2 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-34 | P2 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-35 | P2 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-36 | P1 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-37 | P1 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-38 | P2 | Courses / CourseDetail | WP-07 | WP-03 | 1.0.0 |
| CRS-39 | P3 | Courses / CourseDetail | WP-07 | WP-03 | Deferred: backlog |
| CRS-40 | P2 | server-side | - | WP-04 | Deferred: server-side (ADR-0002) |
| FEED-01 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-02 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-03 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-04 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-05 | P1 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-06 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-07 | P0 | PostCard/PostDetail (staff) + AnonymousRevealRow | WP-08 | WP-03 | 1.0.0 |
| FEED-08 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-09 | P3 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | Deferred: backlog |
| FEED-10 | P1 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-11 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-12 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-13 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-14 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-15 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-16 | P2 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 guarded: ADR-0002 flag |
| FEED-17 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-18 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-19 | P1 | ReportSheet in PostDetail/Chat (flag commentAndUserReports) | WP-08 | WP-03 | 1.0.0 |
| FEED-20 | P1 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-21 | P1 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-22 | P1 | FeedScreen (PinnedCardSection from WP-06) | WP-08 | WP-02 | 1.0.0 |
| FEED-23 | P2 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-24 | P0 | FeedRepository filter | WP-08 | WP-03 | 1.0.0 |
| FEED-25 | P1 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-26 | P1 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-27 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-28 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-29 | P2 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-30 | P1 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-31 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-32 | P1 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-33 | P2 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-34 | P2 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-35 | P1 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-36 | P1 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-37 | P1 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-38 | P1 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-39 | P1 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-40 | P2 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| FEED-41 | P3 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | Deferred: backlog |
| FEED-42 | P0 | Feed / Composer / PostDetail / Saved | WP-08 | WP-03 | 1.0.0 |
| CHAT-01 | P0 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-02 | P0 | FindUsersScreen `messages/new` | WP-09 | WP-03 | 1.0.0 |
| CHAT-03 | P0 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-04 | P0 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-05 | P0 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-06 | P0 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-07 | P0 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-08 | P0 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-09 | P0 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-10 | P0 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-11 | P1 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-12 | P2 | Messages / Chat | WP-09 | WP-04 | 1.0.0 guarded: ADR-0002 flag |
| CHAT-13 | P0 | MessagesScreen + bottom bar badge (MainShell) | WP-09 | WP-04 | 1.0.0 |
| CHAT-14 | P2 | Messages / Chat | WP-09 | WP-04 | 1.0.0 guarded: ADR-0002 flag |
| CHAT-15 | P1 | NotificationsScreen | WP-11 | WP-04 | 1.0.0 |
| CHAT-16 | P1 | BlockedUsersScreen `settings/blocked` | WP-10 | WP-04 | 1.0.0 |
| CHAT-17 | P0 | Routes.CHAT + NavActions.openChat | WP-01 | - | 1.0.0 |
| CHAT-18 | P1 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-19 | P3 | Messages / Chat | WP-09 | WP-04 | Deferred: no contract |
| CHAT-20 | P3 | Messages / Chat | WP-09 | WP-04 | Deferred: no contract |
| CHAT-21 | P3 | Messages / Chat | WP-09 | WP-04 | Deferred: backlog |
| CHAT-22 | P2 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-23 | P1 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-24 | P2 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-25 | P2 | FindUsersScreen | WP-09 | WP-03 | 1.0.0 |
| CHAT-26 | P0 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-27 | P1 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-28 | P1 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-29 | P2 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-30 | P1 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-31 | P2 | Messages / Chat | WP-09 | WP-04 | 1.0.0 |
| CHAT-32 | P1 | NexusNavHost back stack | WP-01 | - | 1.0.0 |
| NOTIF-01 | P0 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-02 | P0 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-03 | P0 | NotificationsScreen + NotificationRouter (WP-01) | WP-11 | - | 1.0.0 |
| NOTIF-04 | P0 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-05 | P1 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-06 | P0 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-07 | P0 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-08 | P1 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-09 | P2 | Notifications | WP-11 | WP-04 | Deferred: ADR-0008 (no push) |
| NOTIF-10 | P2 | Notifications | WP-11 | WP-04 | Deferred: ADR-0008 |
| NOTIF-11 | P3 | Notifications | WP-11 | WP-04 | Deferred: ADR-0008 |
| NOTIF-12 | P1 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-13 | P2 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-14 | P0 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-15 | P1 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-16 | P1 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-17 | P0 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-18 | P1 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-19 | P2 | MainShell in-app banner | WP-01 | - | 1.0.0 |
| NOTIF-20 | P2 | Notifications | WP-11 | WP-04 | 1.0.0 |
| NOTIF-21 | P2 | Notifications | WP-11 | WP-04 | 1.0.0 |
| SRCH-01 | P0 | Search | WP-11 | WP-03 | 1.0.0 |
| SRCH-02 | P1 | Search | WP-11 | WP-03 | 1.0.0 |
| SRCH-03 | P0 | Search | WP-11 | WP-03 | 1.0.0 |
| SRCH-04 | P1 | Search | WP-11 | WP-03 | 1.0.0 |
| SRCH-05 | P0 | Search | WP-11 | WP-03 | 1.0.0 |
| SRCH-06 | P1 | Search | WP-11 | WP-03 | 1.0.0 |
| SRCH-07 | P0 | Search | WP-11 | WP-03 | 1.0.0 |
| SRCH-08 | P1 | SearchScreen recents | WP-11 | WP-02 | 1.0.0 |
| SRCH-09 | P2 | Search | WP-11 | WP-03 | 1.0.0 |
| SRCH-10 | P2 | Routes.SEARCH?q | WP-11 | - | 1.0.0 |
| SRCH-11 | P2 | Search | WP-11 | WP-03 | 1.0.0 |
| SRCH-12 | P3 | Search | WP-11 | WP-03 | Deferred: backlog |
| SRCH-13 | P1 | Search | WP-11 | WP-03 | 1.0.0 |
| SRCH-14 | P3 | Search | WP-11 | WP-03 | Deferred: backlog |
| SRCH-15 | P0 | SearchRepository masking | WP-11 | WP-03 | 1.0.0 |
| SRCH-16 | P1 | Search | WP-11 | WP-03 | 1.0.0 |
| SRCH-17 | P1 | Search | WP-11 | WP-03 | 1.0.0 |
| SRCH-18 | P2 | Search | WP-11 | WP-03 | 1.0.0 |
| SRCH-19 | P1 | Search | WP-11 | WP-03 | 1.0.0 |
| LEAD-01 | P0 | Leaderboard | WP-11 | WP-03 | 1.0.0 |
| LEAD-02 | P1 | Leaderboard | WP-11 | WP-03 | 1.0.0 |
| LEAD-03 | P1 | Leaderboard | WP-11 | WP-03 | 1.0.0 |
| LEAD-04 | P1 | Leaderboard | WP-11 | WP-03 | 1.0.0 |
| LEAD-05 | P0 | Leaderboard | WP-11 | WP-03 | 1.0.0 |
| LEAD-06 | P1 | Leaderboard | WP-11 | WP-03 | 1.0.0 |
| LEAD-07 | P1 | Leaderboard | WP-11 | WP-03 | 1.0.0 |
| LEAD-08 | P2 | Leaderboard | WP-11 | WP-03 | 1.0.0 |
| LEAD-09 | P2 | Leaderboard | WP-11 | WP-03 | 1.0.0 |
| LEAD-10 | P1 | domain/rules/Tier.kt | WP-01 | - | 1.0.0 |
| LEAD-11 | P2 | Leaderboard | WP-11 | WP-03 | 1.0.0 |
| LEAD-12 | P2 | Leaderboard | WP-11 | WP-03 | 1.0.0 |
| LEAD-13 | P3 | Leaderboard | WP-11 | WP-03 | Deferred: backlog |
| LEAD-14 | P1 | Leaderboard | WP-11 | WP-03 | 1.0.0 |
| PIN-01 | P1 | PinnedCard on Home/Feed | WP-06 | WP-02 | 1.0.0 |
| PIN-02 | P0 | PinnedCard on Home/Feed | WP-06 | WP-02 | 1.0.0 |
| PIN-03 | P1 | PinnedCard on Home/Feed | WP-06 | WP-02 | 1.0.0 |
| PIN-04 | P1 | PinnedCard on Home/Feed | WP-06 | WP-02 | 1.0.0 |
| PIN-05 | P1 | PinnedCard on Home/Feed | WP-06 | WP-02 | 1.0.0 |
| PIN-06 | P1 | PinnedCard on Home/Feed | WP-06 | WP-02 | 1.0.0 |
| PIN-07 | P1 | AdminPinnedScreen `admin/pinned` | WP-12 | WP-04 | 1.0.0 |
| PIN-08 | P2 | AdminPinnedScreen (stats) | WP-12 | WP-04 | 1.0.0 |
| PIN-09 | P3 | PinnedCard on Home/Feed | WP-06 | WP-02 | Deferred: backlog |
| PIN-10 | P3 | PinnedCard on Home/Feed | WP-06 | WP-02 | Deferred: backlog |
| PIN-11 | P1 | PinnedCard on Home/Feed | WP-06 | WP-02 | 1.0.0 |
| PIN-12 | P2 | PinnedCard on Home/Feed | WP-06 | WP-02 | 1.0.0 |
| PIN-13 | P2 | PinnedCard on Home/Feed | WP-06 | WP-02 | 1.0.0 |
| PIN-14 | P2 | PinnedCard on Home/Feed | WP-06 | WP-02 | 1.0.0 |
| PIN-15 | P2 | PinnedCard on Home/Feed | WP-06 | WP-02 | 1.0.0 |
| PIN-16 | P3 | PinnedCard on Home/Feed | WP-06 | WP-02 | Deferred: backlog |
| ADM-01 | P0 | AdminHomeScreen (WP-12) gated by Permissions.admin (WP-01) | WP-12 | - | 1.0.0 |
| ADM-02 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-03 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-04 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-05 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-06 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-07 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-08 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-09 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-10 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-11 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-12 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-13 | P2 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-14 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-15 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-16 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-17 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-18 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-19 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-20 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-21 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-22 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-23 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-24 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-25 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-26 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-27 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-28 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-29 | P0 | ApiErrorMapper / toUiText | WP-01 | - | 1.0.0 |
| ADM-30 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-31 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-32 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-33 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-34 | P2 | Admin screens | WP-12 | WP-04 | 1.0.0 guarded: ADR-0018 flag off |
| ADM-35 | P0 | AdminReportDetailScreen, PostCard (staff) | WP-12 | WP-03 | 1.0.0 |
| ADM-36 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-37 | P3 | Admin screens | WP-12 | WP-04 | Deferred: backlog |
| ADM-38 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-39 | P0 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-40 | P1 | strings_admin.xml | WP-01 | - | 1.0.0 |
| ADM-41 | P0 | Permissions.canActOnTarget (WP-01) enforced in AdminUsers (WP-12) | WP-01 | - | 1.0.0 |
| ADM-42 | P0 | domain/rules/Permissions.kt + tests | WP-01 | - | 1.0.0 |
| ADM-43 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-44 | P3 | Admin screens | WP-12 | WP-04 | Deferred: backlog |
| ADM-45 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| ADM-46 | P2 | Admin screens | WP-12 | WP-04 | Deferred: backlog (server) |
| ADM-47 | P0 | AdminSubAdminsScreen | WP-12 | WP-04 | 1.0.0 |
| ADM-48 | P1 | Admin screens | WP-12 | WP-04 | 1.0.0 |
| SET-01 | P0 | Settings | WP-10 | WP-02 | 1.0.0 |
| SET-02 | P2 | Settings | WP-10 | WP-02 | 1.0.0 |
| SET-03 | P1 | Settings | WP-10 | WP-02 | 1.0.0 |
| SET-04 | P1 | ProfileScreen ThemePicker | WP-10 | WP-02 | 1.0.0 |
| SET-05 | P1 | ChangePasswordScreen `settings/password` (built in WP-05) | WP-05 | WP-02 | 1.0.0 |
| SET-06 | P0 | Settings | WP-10 | WP-02 | 1.0.0 |
| SET-07 | P1 | AboutScreen `settings/about` | WP-10 | - | 1.0.0 |
| SET-08 | P1 | Settings | WP-10 | WP-02 | 1.0.0 |
| SET-09 | P2 | Settings | WP-10 | WP-02 | Deferred: ADR-0008 (in-app explanation only) |
| SET-10 | P1 | Settings | WP-10 | WP-02 | 1.0.0 guarded: ADR-0006 |
| SET-11 | P3 | Settings | WP-10 | WP-02 | Deferred: backlog |
| SET-12 | P3 | Settings | WP-10 | WP-02 | Deferred: backlog |
| SET-13 | P3 | Settings | WP-10 | WP-02 | Deferred: backlog |
| SET-14 | P3 | Settings | WP-10 | WP-02 | Deferred: backlog |
| SET-15 | P1 | SettingsStore migration | WP-10 | WP-02 | 1.0.0 |
| SET-16 | P0 | designsystem Color/Theme, MainActivity | WP-01 | - | 1.0.0 |
| SET-17 | P2 | Settings | WP-10 | WP-02 | 1.0.0 |
| SET-18 | P2 | designsystem Color/Theme, MainActivity | WP-01 | - | 1.0.0 |
| SET-19 | P3 | Settings | WP-10 | WP-02 | Deferred: backlog |
| SET-20 | P2 | designsystem Color/Theme, MainActivity | WP-01 | - | 1.0.0 |
| X-01 | P0 | Cross-cutting (all screens) | WP-01 | WP-01 | 1.0.0 |
| X-02 | P1 | Cross-cutting (all screens) | WP-01 | WP-01 | 1.0.0 |
| X-03 | P1 | SwrCache (core) used by data WPs | WP-01 | WP-02/03/04 | 1.0.0 |
| X-04 | P0 | Cross-cutting (all screens) | WP-01 | WP-01 | 1.0.0 |
| X-05 | P1 | every screen (DoD item) | all feature WPs | - | 1.0.0 |
| X-06 | P0 | Cross-cutting (all screens) | WP-01 | WP-01 | 1.0.0 |
| X-07 | P1 | every screen (DoD item) | all feature WPs | - | 1.0.0 |
| X-08 | P0 | Cross-cutting (all screens) | WP-01 | WP-01 | 1.0.0 |
| X-09 | P0 | Cross-cutting (all screens) | WP-01 | WP-01 | 1.0.0 |
| X-10 | P1 | upload progress UI (WP-07 files, WP-08 images) | WP-07 | WP-02 | 1.0.0 |
| X-11 | P2 | Cross-cutting (all screens) | WP-01 | WP-01 | 1.0.0 |
| X-12 | P0 | data layer (keyset pagination, counts) | - | WP-02/03/04 | 1.0.0 |
| X-13 | P1 | data layer (keyset pagination, counts) | - | WP-02/03/04 | 1.0.0 |
| X-14 | P0 | Cross-cutting (all screens) | WP-01 | WP-01 | 1.0.0 |
| X-15 | P1 | Cross-cutting (all screens) | WP-01 | WP-01 | 1.0.0 |
| X-16 | P2 | Cross-cutting (all screens) | WP-01 | WP-01 | 1.0.0 |
| X-17 | P0 | StorageRepository + UploadSource (progress UI in WP-07/08/10) | WP-01 | WP-02 | 1.0.0 |
| X-18 | P1 | StorageRepository + UploadSource (progress UI in WP-07/08/10) | WP-01 | WP-02 | 1.0.0 |
| X-19 | P0 | StorageRepository + UploadSource (progress UI in WP-07/08/10) | WP-01 | WP-02 | 1.0.0 |
| X-20 | P2 | StorageRepository + UploadSource (progress UI in WP-07/08/10) | WP-01 | WP-02 | 1.0.0 |
| X-21 | P1 | StorageRepository + UploadSource (progress UI in WP-07/08/10) | WP-01 | WP-02 | 1.0.0 |
| X-22 | P1 | StorageRepository + UploadSource (progress UI in WP-07/08/10) | WP-01 | WP-02 | 1.0.0 |
| X-23 | P1 | StorageRepository + UploadSource (progress UI in WP-07/08/10) | WP-01 | WP-02 | 1.0.0 |
| X-24 | P1 | StorageRepository + UploadSource (progress UI in WP-07/08/10) | WP-01 | WP-02 | 1.0.0 |
| X-25 | P0 | Cross-cutting (all screens) | WP-01 | WP-01 | 1.0.0 |
| X-26 | P1 | Cross-cutting (all screens) | WP-01 | WP-01 | 1.0.0 |
| X-27 | P0 | AccountBlockedScreen / AppGate | WP-05 | - | 1.0.0 |
| X-28 | P1 | Cross-cutting (all screens) | WP-01 | WP-01 | 1.0.0 |
| X-29 | P1 | every screen (DoD item) | all feature WPs | - | 1.0.0 |
| X-30 | P3 | Cross-cutting (all screens) | WP-01 | WP-01 | Deferred: post-1.0 backlog |
| X-31 | P0 | every screen (DoD item) | all feature WPs | - | 1.0.0 |
| X-32 | P1 | every screen (DoD item) | all feature WPs | - | 1.0.0 |
| X-33 | P0 | Cross-cutting (all screens) | WP-01 | WP-01 | 1.0.0 |
| I18N-01 | P0 | Cross-cutting (resources) | WP-01 | - | 1.0.0 |
| I18N-02 | P1 | Cross-cutting (resources) | WP-01 | - | 1.0.0 |
| I18N-03 | P0 | Cross-cutting (resources) | WP-01 | - | 1.0.0 |
| I18N-04 | P2 | Cross-cutting (resources) | WP-01 | - | 1.0.0 |
| I18N-05 | P1 | Cross-cutting (resources) | WP-01 | - | 1.0.0 |
| I18N-06 | P1 | Cross-cutting (resources) | WP-01 | - | 1.0.0 |
| I18N-07 | P2 | Cross-cutting (resources) | WP-01 | - | 1.0.0 |
| I18N-08 | P3 | Cross-cutting (resources) | WP-01 | - | Deferred: post-1.0 backlog |
| A11Y-01 | P0 | Cross-cutting (components + DoD) | WP-01 | - | 1.0.0 |
| A11Y-02 | P0 | Cross-cutting (components + DoD) | WP-01 | - | 1.0.0 |
| A11Y-03 | P0 | Cross-cutting (components + DoD) | WP-01 | - | 1.0.0 |
| A11Y-04 | P1 | Cross-cutting (components + DoD) | WP-01 | - | 1.0.0 |
| A11Y-05 | P1 | Cross-cutting (components + DoD) | WP-01 | - | 1.0.0 |
| A11Y-06 | P1 | Cross-cutting (components + DoD) | WP-01 | - | 1.0.0 |
| A11Y-07 | P1 | Cross-cutting (components + DoD) | WP-01 | - | 1.0.0 |
| A11Y-08 | P2 | Cross-cutting (components + DoD) | WP-01 | - | 1.0.0 |
| A11Y-09 | P3 | Cross-cutting (components + DoD) | WP-01 | - | Deferred: post-1.0 backlog |
| SEC-01 | P0 | Login/ChangePassword (no saveable password) | WP-05 | - | 1.0.0 |
| SEC-02 | P0 | Cross-cutting | WP-01 | - | 1.0.0 |
| SEC-03 | P0 | Cross-cutting | WP-01 | - | 1.0.0 |
| SEC-04 | P0 | masking in repositories | WP-08 | WP-03/WP-04 | 1.0.0 |
| SEC-05 | P0 | data layer: ids from session only | - | WP-02/03/04 | 1.0.0 |
| SEC-06 | P0 | Cross-cutting | WP-01 | - | 1.0.0 |
| SEC-07 | P1 | Cross-cutting | WP-01 | - | 1.0.0 |
| SEC-08 | P0 | Cross-cutting | WP-01 | - | 1.0.0 |
| SEC-09 | P1 | Cross-cutting | WP-01 | - | 1.0.0 |
| SEC-10 | P0 | Admin create account / broadcast | WP-12 | WP-04 | 1.0.0 |
| SEC-11 | P1 | PollCard | WP-06 | WP-02 | 1.0.0 |
| SEC-12 | P1 | Search/Leaderboard role filtering | WP-11 | WP-03 | 1.0.0 |
| SEC-13 | P1 | Cross-cutting | WP-01 | - | 1.0.0 |
| SEC-14 | P1 | StorageRepository validation | - | WP-02 | 1.0.0 |
| SEC-15 | P1 | Cross-cutting | WP-01 | - | 1.0.0 |
| SEC-16 | P0 | Cross-cutting | WP-01 | - | 1.0.0 |
| SEC-17 | P3 | Cross-cutting | WP-01 | - | Deferred: backlog |
| NAV-01 | P0 | NavHost / AppGate / MainShell | WP-01 | - | 1.0.0 |
| NAV-02 | P0 | NavHost / AppGate / MainShell | WP-01 | - | 1.0.0 |
| NAV-03 | P0 | NavHost / AppGate / MainShell | WP-01 | - | 1.0.0 |
| NAV-04 | P0 | manifest + MainActivity + AppGate (Recovery) | WP-01 | - | 1.0.0 |
| NAV-05 | P2 | NavHost / AppGate / MainShell | WP-01 | - | Deferred: ADR-0012 |
| NAV-06 | P0 | NavHost / AppGate / MainShell | WP-01 | - | 1.0.0 |
| NAV-07 | P3 | NavHost / AppGate / MainShell | WP-01 | - | Deferred: ADR-0012 |
| NAV-08 | P0 | NavHost / AppGate / MainShell | WP-01 | - | 1.0.0 |
| NAV-09 | P0 | NavHost / AppGate / MainShell | WP-01 | - | 1.0.0 |
| NAV-10 | P1 | NavHost / AppGate / MainShell | WP-01 | - | 1.0.0 |
| NAV-11 | P1 | NavHost / AppGate / MainShell | WP-01 | - | 1.0.0 |
| NAV-12 | P0 | Recover sheet to SetNewPasswordScreen | WP-05 | WP-02 | 1.0.0 |
| NAV-13 | P0 | ChangePasswordScreen forced | WP-05 | WP-02 | 1.0.0 |
| NAV-14 | P1 | NavHost / AppGate / MainShell | WP-01 | - | 1.0.0 |
| NAV-15 | P0 | NavHost / AppGate / MainShell | WP-01 | - | 1.0.0 |
| NAV-16 | P2 | NavHost / AppGate / MainShell | WP-01 | - | 1.0.0 |
| NAV-17 | P1 | NavHost / AppGate / MainShell | WP-01 | - | 1.0.0 |
| NAV-18 | P0 | NavHost / AppGate / MainShell | WP-01 | - | 1.0.0 |
| BLD-01 | P0 | Build / manifest / CI | WP-01 | - | 1.0.0 |
| BLD-02 | P1 | Build / manifest / CI | WP-01 | - | 1.0.0 |
| BLD-03 | P0 | Build / manifest / CI | WP-01 | - | 1.0.0 |
| BLD-04 | P0 | Build / manifest / CI | WP-01 | - | 1.0.0 |
| BLD-05 | P1 | Build / manifest / CI | WP-01 | - | 1.0.0 |
| BLD-06 | P2 | Build / manifest / CI | WP-01 | - | 1.0.0 |
| BE-01 | P2 | WarningsScreen | WP-10 | WP-02 | 1.0.0 |
| BE-02 | P1 | AdminActivityScreen | WP-12 | WP-04 | 1.0.0 |
| BE-03 | P2 | ProfileScreen | WP-10 | WP-03 | 1.0.0 |
| BE-04 | P3 | see place | WP-10 | - | Deferred: backlog |
| BE-05 | P3 | see place | WP-10 | - | Deferred: backlog |
| BE-06 | P3 | see place | WP-10 | - | Deferred: backlog |
| BE-07 | P1 | BlockedUsersScreen | WP-10 | WP-04 | 1.0.0 |
| BE-08 | P2 | server dependent (flag) | - | WP-04 | 1.0.0 guarded: ADR-0002 flag |
| BE-09 | P1 | CoursesScreen counts | WP-07 | WP-03 | 1.0.0 |
| BE-10 | P1 | RoleSheet in AdminUsers | WP-12 | WP-04 | 1.0.0 |
| BE-11 | P2 | see place | WP-10 | - | Deferred: backlog |
| BE-12 | P2 | see place | WP-10 | - | Deferred: ADR-0005/0008 |
| BE-13 | P2 | PostDetailScreen comment depth | WP-08 | WP-03 | 1.0.0 |

## Part 3. Counts

* Total IDs: 432 (P0 159, P1 158, P2 80, P3 35)
* Delivered in 1.0.0 (including guarded): 387
* Deferred with a recorded reason: 45
