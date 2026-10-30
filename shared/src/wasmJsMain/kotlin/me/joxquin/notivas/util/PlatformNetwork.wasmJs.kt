package me.joxquin.notivas.util

import kotlinx.browser.window
import io.ktor.http.encodeURLQueryComponent

actual fun resolveApiUrl(targetUrl: String): String {
    val clean = targetUrl.trim()
    if (clean.contains("/api-proxy")) return clean
    return try {
        val origin = window.location.origin
        val encoded = clean.encodeURLQueryComponent()
        "$origin/api-proxy?url=$encoded"
    } catch (_: Throwable) {
        clean
    }
}

