# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# ============================================================================
# Kotlin
# ============================================================================
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Kotlin data classes (preserves all fields and methods)
-keep class kotlin.Metadata { *; }
-keepclassmembers class * {
    public <init>(...);
}

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ============================================================================
# Room Database
# ============================================================================
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class androidx.room.** { *; }

# Keep all Room entity classes (all data classes in core.model package)
-keep class com.vettr.android.core.model.** { *; }
-keepclassmembers class com.vettr.android.core.model.** { *; }

# ============================================================================
# Retrofit & Networking
# ============================================================================
-keepattributes Signature
-keepattributes Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Keep API interfaces and models
-keep interface com.vettr.android.core.data.remote.** { *; }
-keep class com.vettr.android.core.data.remote.model.** { *; }
-keepclassmembers class com.vettr.android.core.data.remote.model.** { *; }

# ============================================================================
# OkHttp
# ============================================================================
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ============================================================================
# Gson
# ============================================================================
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep all @SerializedName annotations
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep data models for Gson serialization
-keep class com.vettr.android.core.data.remote.model.** { *; }
-keep class com.vettr.android.core.model.** { *; }

# ============================================================================
# Hilt Dependency Injection
# ============================================================================
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep Hilt modules and components
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.components.SingletonComponent class * { *; }

# Keep Hilt generated code
-keep class **_HiltModules { *; }
-keep class **_Factory { *; }
-keep class **_Impl { *; }
-keep class **_MembersInjector { *; }
-keep class dagger.hilt.** { *; }

# ============================================================================
# Google Sign-In / Credentials
# ============================================================================
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.tasks.** { *; }
-keep interface com.google.android.gms.** { *; }

# Keep Google ID credential classes
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }

# Play Services Auth
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.** { *; }

# ============================================================================
# AndroidX Security Crypto
# ============================================================================
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }

# Google Crypto Tink - Missing optional dependencies
-dontwarn com.google.api.client.http.**
-dontwarn org.joda.time.**

# ============================================================================
# AndroidX Biometric
# ============================================================================
-keep class androidx.biometric.** { *; }

# ============================================================================
# WorkManager
# ============================================================================
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ============================================================================
# Jetpack Compose
# ============================================================================
-keep class androidx.compose.** { *; }
-keep @androidx.compose.runtime.Composable class * { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# ============================================================================
# DataStore
# ============================================================================
-keep class androidx.datastore.** { *; }
-keep class com.google.protobuf.** { *; }

# ============================================================================
# Coil Image Loading
# ============================================================================
-keep class coil3.** { *; }
-keep interface coil3.** { *; }
-dontwarn coil3.PlatformContext

# ============================================================================
# Application-specific Rules
# ============================================================================
# Keep all ViewModels
-keep class com.vettr.android.feature.**.ViewModel { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep all Repositories
-keep class com.vettr.android.core.data.repository.** { *; }
-keep interface com.vettr.android.core.data.repository.** { *; }

# Keep all DAOs
-keep interface com.vettr.android.core.data.local.dao.** { *; }

# Keep main Application class
-keep class com.vettr.android.VettrApp { *; }
-keep class com.vettr.android.MainActivity { *; }
