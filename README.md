# NEXUS Android 1.2.0

Native Android client for the NEXUS academic community. This project packages the recovered Kotlin/Jetpack Compose implementation and builds an installable debug APK through GitHub Actions.

## Build

Requires JDK 17, Android SDK Platform 35, and Gradle 8.9.

```bash
gradle --no-daemon --console=plain --max-workers=2 -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :app:assembleDebug
```

The app reads `SUPABASE_URL`, `SUPABASE_PUBLISHABLE_KEY`, and optional `WEB_AUTH_URL` from Gradle properties or environment variables. The workflow retrieves only the public Supabase URL and `sb_publishable_` client key from the NEXUS web repository's committed frontend configuration. It never reads or embeds a service-role key. The web-auth handoff button remains hidden until `WEB_AUTH_URL` is configured.

## Important safety boundary

No production database migration, RLS change, or data write is performed by this Android build. See `NEXUS_ANDROID_PARITY_AUDIT.md` in the web repository for backend limitations and security findings that must be resolved before enabling affected actions.
