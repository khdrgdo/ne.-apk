plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

fun quotedBuildValue(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
val supabaseUrl = providers.gradleProperty("SUPABASE_URL").orElse(providers.environmentVariable("SUPABASE_URL")).getOrElse("")
val supabaseKey = providers.gradleProperty("SUPABASE_PUBLISHABLE_KEY").orElse(providers.environmentVariable("SUPABASE_PUBLISHABLE_KEY")).getOrElse("")
val webAuthUrl = providers.gradleProperty("WEB_AUTH_URL").orElse(providers.environmentVariable("WEB_AUTH_URL")).getOrElse("")

android {
    namespace = "com.nexus.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.nexus.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 20000
        versionName = "2.0.0-preview.1"
        buildConfigField("String", "SUPABASE_URL", quotedBuildValue(supabaseUrl))
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", quotedBuildValue(supabaseKey))
        buildConfigField("String", "WEB_AUTH_URL", quotedBuildValue(webAuthUrl))
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    testOptions { unitTests.isReturnDefaultValues = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    // android.jar stubs of org.json do nothing in JVM unit tests; this real implementation is needed by the tests only.
    testImplementation("org.json:json:20240303")
}
