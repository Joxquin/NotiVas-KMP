package me.joxquin.notivas.util

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual object PlatformShareHandler {
    actual fun shareText(text: String, title: String?) {
        try {
            val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
            val activityController = UIActivityViewController(listOf(text), null)
            rootViewController?.presentViewController(activityController, animated = true, completion = null)
        } catch (_: Throwable) {}
    }
}
