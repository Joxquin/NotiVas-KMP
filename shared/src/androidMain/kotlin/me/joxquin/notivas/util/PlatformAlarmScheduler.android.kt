package me.joxquin.notivas.util

import android.app.Application
import android.content.Context
import me.joxquin.notivas.background.alarm.AndroidAlarmScheduler
import me.joxquin.notivas.di.AppModule

actual object PlatformAlarmScheduler {
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

    actual fun scheduleCustomReminder(
        assignmentId: Long,
        courseName: String,
        assignmentName: String,
        triggerEpochMillis: Long
    ) {
        val context = getContext() ?: return
        val scheduler = AndroidAlarmScheduler(
            context = context,
            preferencesManager = AppModule.preferencesManager,
            localStore = AppModule.localStore
        )
        scheduler.scheduleCustomReminder(
            assignmentId = assignmentId,
            courseName = courseName,
            assignmentName = assignmentName,
            triggerEpochMillis = triggerEpochMillis
        )
    }
}

