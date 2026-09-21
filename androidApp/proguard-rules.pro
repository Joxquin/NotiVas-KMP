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