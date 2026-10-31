package me.joxquin.notivas.util

expect object PlatformShareHandler {
    fun shareText(text: String, title: String? = null)
}
