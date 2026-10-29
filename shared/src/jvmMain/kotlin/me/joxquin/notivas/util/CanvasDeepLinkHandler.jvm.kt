package me.joxquin.notivas.util

import java.awt.Desktop
import java.net.URI

actual object CanvasDeepLinkHandler {
    actual fun openCanvasUrl(url: String, fallbackToBrowser: Boolean) {
        if (!fallbackToBrowser) return
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(url))
            }
        } catch (_: Exception) {}
    }
}

