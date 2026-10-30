# Project specific ProGuard rules for NotiVas KMP

# Preserve Kotlin Serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class **$$serializer {
    *** INSTANCE;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName *;
}
-dontwarn kotlinx.serialization.**

# Ktor Client
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Miuix UI & Theme
-keep class top.yukonga.miuix.** { *; }
-dontwarn top.yukonga.miuix.**

# Compose Multiplatform
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# AndroidX WorkManager & Room
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class androidx.work.impl.WorkDatabase_Impl {
    <init>(...);
}
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class androidx.startup.** { *; }
-dontwarn androidx.work.**
-dontwarn androidx.room.**
-dontwarn androidx.startup.**
