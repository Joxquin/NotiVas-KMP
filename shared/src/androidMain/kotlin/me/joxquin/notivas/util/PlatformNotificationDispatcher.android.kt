package me.joxquin.notivas.util

import android.app.Application
import android.content.Context
import me.joxquin.notivas.background.notification.NotificationHelper

actual object PlatformNotificationDispatcher {
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

    actual fun showNotification(
        title: String,
        message: String,
        targetId: Long?,
        secondaryId: Long?,
        courseCode: String?,
        courseName: String?
    ) {
        val context = getContext() ?: return
        val helper = NotificationHelper(context)
        helper.showNotification(
            title = title,
            message = message,
            targetId = targetId,
            secondaryId = secondaryId,
            courseCode = courseCode,
            courseName = courseName
        )
    }
}

