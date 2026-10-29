package me.joxquin.notivas.util

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual object CanvasDeepLinkHandler {
    actual fun openCanvasUrl(url: String, fallbackToBrowser: Boolean) {
        if (!fallbackToBrowser) return
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl)
    }
}

