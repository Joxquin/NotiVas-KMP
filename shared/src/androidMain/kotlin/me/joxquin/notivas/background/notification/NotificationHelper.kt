package me.joxquin.notivas.background.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class NotificationHelper(
    private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val notificationIdGenerator = AtomicInteger((System.currentTimeMillis() % 100000).toInt())

    companion object {
        const val CHANNEL_REMINDERS_ID = "assignment_reminders"
        const val CHANNEL_REMINDERS_NAME = "Recordatorios de Tareas"

        const val CHANNEL_GENERAL_ID = "general_notifications"
        const val CHANNEL_GENERAL_NAME = "Avisos Generales"

        const val CHANNEL_SYNC_ID = "sync_notifications"
        const val CHANNEL_SYNC_NAME = "Sincronización en Segundo Plano"

        const val SESSION_EXPIRED_NOTIFICATION_ID = 77777
        const val SYNC_NOTIFICATION_ID = 88888
        const val SUMMARY_NOTIFICATION_ID = 99999
        const val NOTIFICATION_GROUP_KEY = "ASSIGNMENT_REMINDERS"
    }

    fun showNotification(
        title: String,
        message: String,
        channelId: String = CHANNEL_REMINDERS_ID,
        notificationId: Int? = null,
        targetId: Long? = null,
        secondaryId: Long? = null,
        courseCode: String? = null,
        courseName: String? = null
    ) {
        createChannels()

        val smallIconRes = context.applicationInfo.icon.takeIf { it != 0 }
            ?: android.R.drawable.ic_dialog_info

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setGroup(NOTIFICATION_GROUP_KEY)
            .build()

        val id = notificationId ?: notificationIdGenerator.incrementAndGet()
        notificationManager.notify(id, notification)

        // Registrar evento en la base de datos Room para el feed de Actividad Reciente
        try {
            val db = me.joxquin.notivas.data.local.db.createRoomDatabase()
            CoroutineScope(Dispatchers.IO).launch {
                val derivedCode = me.joxquin.notivas.util.CourseUtil.extractCourseTag(courseCode, courseName)
                db.activityLogDao().insert(
                    me.joxquin.notivas.data.local.db.entities.ActivityLogEntity(
                        type = "CANVAS_NOTIFICATION",
                        title = title,
                        description = message,
                        timestamp = System.currentTimeMillis(),
                        courseCode = derivedCode,
                        courseName = courseName,
                        targetId = targetId,
                        secondaryId = secondaryId,
                        extraInfo = "Notificación Canvas"
                    )
                )
            }
        } catch (_: Throwable) {}

        // Generar o actualizar notificación resumen para agrupar
        val summaryNotification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIconRes)
            .setStyle(NotificationCompat.InboxStyle().setSummaryText("Recordatorios Activos"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(NOTIFICATION_GROUP_KEY)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(SUMMARY_NOTIFICATION_ID, summaryNotification)
    }

    fun showSyncNotification(
        title: String = "Sincronizando Canvas",
        message: String = "Actualizando cursos, tareas y notas..."
    ) {
        createChannels()

        val smallIconRes = context.applicationInfo.icon.takeIf { it != 0 }
            ?: android.R.drawable.ic_dialog_info

        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC_ID)
            .setSmallIcon(smallIconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOngoing(true)
            .setTimeoutAfter(45_000L)
            .setProgress(0, 0, true)
            .build()

        notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
    }

    fun showSessionExpiredNotification(
        title: String = "Sesión de Canvas expirada",
        message: String = "Tu sesión web ha caducado. Toca para reconectar y actualizar tus tareas."
    ) {
        createChannels()

        val smallIconRes = context.applicationInfo.icon.takeIf { it != 0 }
            ?: android.R.drawable.ic_dialog_alert

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action", "relogin")
        }
        val pendingIntent = intent?.let {
            android.app.PendingIntent.getActivity(
                context,
                SESSION_EXPIRED_NOTIFICATION_ID,
                it,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL_ID)
            .setSmallIcon(smallIconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .apply {
                if (pendingIntent != null) {
                    setContentIntent(pendingIntent)
                }
            }
            .build()

        notificationManager.notify(SESSION_EXPIRED_NOTIFICATION_ID, notification)
    }

    fun dismissSyncNotification() {
        notificationManager.cancel(SYNC_NOTIFICATION_ID)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    private fun createChannels() {
        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDERS_ID,
            CHANNEL_REMINDERS_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Canal prioritario para recordatorios de entrega de tareas y evaluaciones"
            enableVibration(true)
        }

        val generalChannel = NotificationChannel(
            CHANNEL_GENERAL_ID,
            CHANNEL_GENERAL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos y alertas generales de la aplicación"
        }

        val syncChannel = NotificationChannel(
            CHANNEL_SYNC_ID,
            CHANNEL_SYNC_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notificaciones silenciosas de progreso durante la sincronización"
            setSound(null, null)
            enableVibration(false)
        }

        notificationManager.createNotificationChannel(reminderChannel)
        notificationManager.createNotificationChannel(generalChannel)
        notificationManager.createNotificationChannel(syncChannel)
    }
}
