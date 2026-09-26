package me.joxquin.notivas.util

data class AppVersionInfo(
    val versionName: String,
    val versionCode: Long
)

expect object AppVersion {
    fun get(): AppVersionInfo
}
