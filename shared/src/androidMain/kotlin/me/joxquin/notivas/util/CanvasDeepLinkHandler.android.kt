package me.joxquin.notivas.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri

actual object CanvasDeepLinkHandler {
    private const val CANVAS_STUDENT_PACKAGE = "com.instructure.candroid"

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

    /**
     * Convierte enlace web de Canvas https://... en canvas-courses://dominio/ruta
     */
    private fun toCanvasAppUri(url: String): String? {
        val trimmed = url.trim()
        val match = Regex("""^https://([^/?#]+)(.*)$""").find(trimmed) ?: return null
        val (host, rest) = match.destructured
        return "canvas-courses://$host$rest"
    }

    actual fun openCanvasUrl(url: String, fallbackToBrowser: Boolean) {
        val context = getContext() ?: return
        try {
            val appUriString = toCanvasAppUri(url)
            val canvasAppIntent = appUriString?.let {
                Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    .setPackage(CANVAS_STUDENT_PACKAGE)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val pm = context.packageManager
            if (canvasAppIntent != null && canvasAppIntent.resolveActivity(pm) != null) {
                context.startActivity(canvasAppIntent)
                return
            }

            if (fallbackToBrowser) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            if (fallbackToBrowser) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(browserIntent)
                } catch (_: Exception) {}
            }
        }
    }
}

