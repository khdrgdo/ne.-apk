# NEXUS release shrinking rules.
# Compose, Coil, OkHttp and AndroidX ship their own consumer rules; only gaps are covered here.

-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# OkHttp optional platform providers (not on the classpath of Android builds).
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn javax.annotation.**

# Okio
-dontwarn okio.**

# org.json is part of the Android framework (android.jar) and is never renamed or removed by R8.
# The domain models are plain Kotlin classes built by hand (no reflection, no serialization plugin).

# Coil (image loading) uses ServiceLoader and reflection on the optional decoders.
-dontwarn coil.**

# Keep Kotlin metadata needed by coroutines debug machinery.
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.debug.**

# Strip verbose logging in release builds.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
