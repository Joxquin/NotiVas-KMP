package me.joxquin.notivas.util

actual object AppVersion {
    actual fun get(): AppVersionInfo {
        val implementationVersion = AppVersion::class.java.`package`?.implementationVersion
        val versionName = implementationVersion ?: "1.0.0"
        return AppVersionInfo(
            versionName = versionName,
            versionCode = 1L
        )
    }
}
