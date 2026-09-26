package me.joxquin.notivas.background.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import me.joxquin.notivas.background.alarm.AndroidAlarmScheduler
import me.joxquin.notivas.background.alarm.AssignmentAlarmReceiver
import me.joxquin.notivas.background.notification.NotificationHelper
import me.joxquin.notivas.di.AppModule
import java.time.Duration
import java.time.ZonedDateTime

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val preferencesManager = AppModule.preferencesManager
            val canvasRepository = AppModule.canvasRepository
            val localStore = AppModule.localStore
            val notificationHelper = NotificationHelper(applicationContext)
            val alarmScheduler = AndroidAlarmScheduler(applicationContext, preferencesManager, localStore)

            val token = preferencesManager.accessToken.first()
            if (!token.isNullOrBlank()) {
                notificationHelper.showSyncNotification()
                try {
                    canvasRepository.fetchAndSaveData()
                } catch (_: Exception) {
                    // Fallback a cache local
                } finally {
                    notificationHelper.dismissSyncNotification()
                }
            }

            val assignments = localStore.getAssignmentList()
            val courses = localStore.getAllCourses().first()
            val courseMap = courses.associateBy { it.id }
            val now = ZonedDateTime.now()

            // 1. Asegurar que las alarmas exactas queden programadas
            assignments.forEach { assignment ->
                val courseName = courseMap[assignment.courseId]?.name ?: "Curso"
                alarmScheduler.scheduleAlarmsForAssignment(assignment, courseName)
            }

            val notif24hEnabled = preferencesManager.notif24h.first()
            val notif3hEnabled = preferencesManager.notif3h.first()
            val notif30mEnabled = preferencesManager.notif30m.first()

            // 2. Evaluar ventanas de alerta preventiva / urgencia / crítica
            assignments.forEach { assignment ->
                if (assignment.isCompleted || assignment.status == "completed") {
                    alarmScheduler.cancel(assignment.id)
                    return@forEach
                }

                val dueStr = assignment.dueAt ?: assignment.lockAt ?: return@forEach
                try {
                    val dueDate = ZonedDateTime.parse(dueStr)
                    if (now.isAfter(dueDate)) {
                        alarmScheduler.cancel(assignment.id)
                        return@forEach
                    }

                    val minutesRemaining = Duration.between(now, dueDate).toMinutes()
                    val courseName = courseMap[assignment.courseId]?.name ?: "Curso"
                    val shortName = if (assignment.name.length > 38) {
                        assignment.name.take(35) + "..."
                    } else {
                        assignment.name
                    }

                    // Alerta Crítica (30 minutos o menos)
                    if (notif30mEnabled && !assignment.notified30m && minutesRemaining in 1..30) {
                        val notifId = AndroidAlarmScheduler.generateRequestCode(
                            assignment.id,
                            AssignmentAlarmReceiver.ALERT_TYPE_30M
                        )
                        notificationHelper.showNotification(
                            title = "⚠️ Alerta Crítica · $courseName",
                            message = "$shortName: ¡Últimos $minutesRemaining minutos para la entrega!",
                            notificationId = notifId
                        )
                        localStore.markNotified30m(assignment.id)
                    }
                    // Alerta de Urgencia (3 horas a 31 min)
                    else if (notif3hEnabled && !assignment.notified3h && minutesRemaining in 31..180) {
                        val hours = maxOf(1L, minutesRemaining / 60)
                        val notifId = AndroidAlarmScheduler.generateRequestCode(
                            assignment.id,
                            AssignmentAlarmReceiver.ALERT_TYPE_3H
                        )
                        notificationHelper.showNotification(
                            title = "⏰ Alerta de Urgencia · $courseName",
                            message = "$shortName: Quedan ~$hours hora(s) para la entrega",
                            notificationId = notifId
                        )
                        localStore.markNotified3h(assignment.id)
                    }
                    // Recordatorio Preventivo (24 horas a 3h)
                    else if (notif24hEnabled && !assignment.notified24h && minutesRemaining in 181..1440) {
                        val notifId = AndroidAlarmScheduler.generateRequestCode(
                            assignment.id,
                            AssignmentAlarmReceiver.ALERT_TYPE_24H
                        )
                        notificationHelper.showNotification(
                            title = "📅 Recordatorio Preventivo · $courseName",
                            message = "$shortName: Entrega programada para las próximas 24h",
                            notificationId = notifId
                        )
                        localStore.markNotified24h(assignment.id)
                    }
                } catch (_: Exception) {
                    // Ignorar errores de parseo de fechas específicas
                }
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
