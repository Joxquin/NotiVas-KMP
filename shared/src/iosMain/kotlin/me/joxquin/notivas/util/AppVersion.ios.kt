package me.joxquin.notivas.util

import platform.Foundation.NSBundle

actual object AppVersion {
    actual fun get(): AppVersionInfo {
        val bundle = NSBundle.mainBundle
        val versionName = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0.0"
        val versionCodeStr = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "1"
        val versionCode = versionCodeStr.toLongOrNull() ?: 1L
        return AppVersionInfo(
            versionName = versionName,
            versionCode = versionCode
        )
    }
}
