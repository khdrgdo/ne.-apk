# Recommended build.gradle.kts update for 1.0.0

Apply in `app/build.gradle.kts` (WP-01). Replace the two literal version lines by constants so there is one source of truth (BLD-02):

```kotlin
// top of app/build.gradle.kts, below the plugins block
val appVersionName = "1.0.0"
val appVersionCode = 10000
```

```kotlin
defaultConfig {
    applicationId = "com.nexus.app"
    minSdk = 26
    targetSdk = 35
    versionCode = appVersionCode      // was 20000
    versionName = appVersionName      // was "2.0.0-preview.1"
    ...
}
```

Optional signing, active only when properties are provided (RELEASE.md):

```kotlin
val nexusKeystore = providers.gradleProperty("nexus.keystore").orNull
android {
    signingConfigs {
        if (nexusKeystore != null) {
            create("release") {
                storeFile = file(nexusKeystore)
                storePassword = providers.gradleProperty("nexus.keystorePassword").get()
                keyAlias = providers.gradleProperty("nexus.keyAlias").get()
                keyPassword = providers.gradleProperty("nexus.keyPassword").get()
            }
        }
    }
    buildTypes {
        release {
            if (nexusKeystore != null) signingConfig = signingConfigs.getByName("release")
            // existing minify/shrink/proguard lines stay
        }
    }
}
```

No dependency changes. Also: workflow artifact names `NEXUS-1.0.0-*`, README title "NEXUS Android 1.0.0".
