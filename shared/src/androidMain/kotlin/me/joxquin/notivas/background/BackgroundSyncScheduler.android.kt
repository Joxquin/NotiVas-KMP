package me.joxquin.notivas.background

import android.app.Application
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import me.joxquin.notivas.background.worker.ReminderWorker
import java.util.concurrent.TimeUnit

actual object BackgroundSyncScheduler {
    private const val WORK_NAME = "assignment_reminder"

    actual fun schedulePeriodicSync(intervalMinutes: Long) {
        try {
            val context = getAndroidApplicationContext()
            val effectiveMinutes = maxOf(15L, intervalMinutes)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<ReminderWorker>(effectiveMinutes, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    actual fun cancelPeriodicSync() {
        try {
            val context = getAndroidApplicationContext()
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun getAndroidApplicationContext(): Context {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentAppMethod = activityThreadClass.getMethod("currentApplication")
        val app = currentAppMethod.invoke(null) as? Application
        return app?.applicationContext ?: throw IllegalStateException("Android Context is null")
    }
}
