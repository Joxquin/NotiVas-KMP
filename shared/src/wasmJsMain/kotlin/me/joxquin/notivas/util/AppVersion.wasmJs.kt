package me.joxquin.notivas.util

actual object AppVersion {
    actual fun get(): AppVersionInfo {
        return AppVersionInfo(
            versionName = "1.3.0",
            versionCode = 1L
        )
    }
}
