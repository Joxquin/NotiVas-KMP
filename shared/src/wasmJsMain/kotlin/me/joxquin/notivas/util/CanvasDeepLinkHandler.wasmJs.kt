package me.joxquin.notivas.util

import kotlinx.browser.window

actual object CanvasDeepLinkHandler {
    actual fun openCanvasUrl(url: String, fallbackToBrowser: Boolean) {
        if (!fallbackToBrowser) return
        try {
            window.open(url, "_blank")
        } catch (_: Throwable) {}
    }
}
