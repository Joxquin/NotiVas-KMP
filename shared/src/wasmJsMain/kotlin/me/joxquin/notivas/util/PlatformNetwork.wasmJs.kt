package me.joxquin.notivas.util

import kotlinx.browser.window
import io.ktor.http.encodeURLQueryComponent

private const val CLOUDFLARE_PROXY_URL = "https://notivas-cors-proxy.jotajoxquin.workers.dev"

actual fun resolveApiUrl(targetUrl: String): String {
    val clean = targetUrl.trim()
    if (clean.contains("notivas-cors-proxy.jotajoxquin.workers.dev") || clean.contains("/api-proxy")) return clean
    return try {
        val hostname = window.location.hostname
        val encoded = clean.encodeURLQueryComponent()
        val proxyBase = if (hostname == "localhost" || hostname == "127.0.0.1") {
            "${window.location.origin}/api-proxy"
        } else {
            CLOUDFLARE_PROXY_URL
        }
        "$proxyBase?url=$encoded"
    } catch (_: Throwable) {
        clean
    }
}

