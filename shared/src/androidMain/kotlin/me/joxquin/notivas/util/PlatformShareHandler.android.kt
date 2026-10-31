package me.joxquin.notivas.util

import android.app.Application
import android.content.Context
import android.content.Intent

actual object PlatformShareHandler {
    private fun getContext(): Context? {
        return try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentAppMethod = activityThreadClass.getMethod("currentApplication")
            val app = currentAppMethod.invoke(null) as? Application
            app?.applicationContext
        } catch (_: Throwable) {
            null
        }
    }

    actual fun shareText(text: String, title: String?) {
        val context = getContext() ?: return
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooserIntent = Intent.createChooser(sendIntent, title ?: "Compartir").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        } catch (_: Exception) {}
    }
}
