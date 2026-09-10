# ==============================================================================
# BSP Utility - ProGuard & R8 Optimization Rules
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. Stack Trace De-obfuscation & Line Numbers (Critical for Crash Reporting)
# ------------------------------------------------------------------------------
# Preserves file names and line numbers in stack traces so obfuscated production
# crash reports (Logcat, Play Console, Crashlytics) can be de-obfuscated using mapping.txt.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ------------------------------------------------------------------------------
# 2. General Annotations & Type Signatures (Required for Reflection & Generics)
# ------------------------------------------------------------------------------
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations

# ------------------------------------------------------------------------------
# 3. Android Components & App Entry Points
# ------------------------------------------------------------------------------
-keep public class com.gratus.bsputility.MainActivity { *; }
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# ------------------------------------------------------------------------------
# 4. BSP Utility Data Models (Room Entities, Attendance, & Custom Fields)
# ------------------------------------------------------------------------------
# Keep all data models completely intact to prevent field renaming or stripping,
# ensuring Room SQLite column mappings and JSON serialization/deserialization work seamlessly.
-keep class com.gratus.bsputility.data.models.** { *; }
-keepclassmembers class com.gratus.bsputility.data.models.** { *; }
-keepclassmembers enum com.gratus.bsputility.data.models.** { *; }

# ------------------------------------------------------------------------------
# 5. AndroidX Room Database & DAOs
# ------------------------------------------------------------------------------
# Room uses reflection to instantiate databases, migrations, and generated DAO implementations.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomOpenHelper
-keep class * implements androidx.room.RoomDatabase$Callback
-keep class com.gratus.bsputility.data.db.** { *; }
-keep class **_Impl { *; }
-dontwarn androidx.room.paging.**

# ------------------------------------------------------------------------------
# 6. AndroidX Lifecycle & ViewModels
# ------------------------------------------------------------------------------
# Allow ViewModelProvider / viewModel() factory to instantiate ViewModels via reflection.
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}
-keep class com.gratus.bsputility.ui.viewmodel.** { *; }

# ------------------------------------------------------------------------------
# 7. JSON Serialization (Moshi & org.json)
# ------------------------------------------------------------------------------
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <fields>;
}
-keep class com.squareup.moshi.** { *; }
-keep class * extends com.squareup.moshi.JsonAdapter { *; }
-dontwarn com.squareup.moshi.**

# ------------------------------------------------------------------------------
# 8. Networking (Retrofit & OkHttp)
# ------------------------------------------------------------------------------
# Preserve Retrofit HTTP annotations and OkHttp platform reflection checks.
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ------------------------------------------------------------------------------
# 9. KotlinX Coroutines
# ------------------------------------------------------------------------------
# Preserve volatile fields used by atomic operations and coroutine reflection hooks.
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ------------------------------------------------------------------------------
# 10. Jetpack Compose
# ------------------------------------------------------------------------------
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# ------------------------------------------------------------------------------
# 11. Firebase & Google Play Services
# ------------------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ------------------------------------------------------------------------------
# 12. Suppress Harmless Platform Warnings
# ------------------------------------------------------------------------------
-dontwarn sun.misc.**
-dontwarn java.lang.invoke.**
-dontwarn java.nio.file.**
