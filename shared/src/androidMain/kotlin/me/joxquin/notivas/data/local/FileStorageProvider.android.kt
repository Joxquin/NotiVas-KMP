package me.joxquin.notivas.data.local

import android.app.Application
import android.content.Context
import java.io.File

actual object FileStorageProvider {
    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun getContextAuto(): Context? {
        if (appContext != null) return appContext
        return try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentAppMethod = activityThreadClass.getMethod("currentApplication")
            val app = currentAppMethod.invoke(null) as? Application
            app?.applicationContext.also { if (it != null) appContext = it }
        } catch (_: Throwable) {
            null
        }
    }

    actual fun getAppDataDirectory(): String {
        val ctx = getContextAuto()
        val dir = if (ctx != null) {
            File(ctx.filesDir, "notivas_data")
        } else {
            File("/data/data/me.joxquin.notivas/files/notivas_data")
        }
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.absolutePath
    }
}
