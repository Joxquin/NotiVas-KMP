package me.joxquin.notivas.background.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import me.joxquin.notivas.background.notification.NotificationHelper
import me.joxquin.notivas.di.AppModule

class AssignmentAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val applicationContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val completed = withTimeoutOrNull(RECEIVER_TIMEOUT_MILLIS) {
                    handleIntent(applicationContext, intent)
                }
                if (completed == null) {
                    Log.w(TAG, "Alarm processing exceeded the broadcast time limit")
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to process assignment alarm", exception)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleIntent(context: Context, intent: Intent) {
        val localStore = AppModule.localStore
        val preferencesManager = AppModule.preferencesManager
        val notificationHelper = NotificationHelper(context)
        val alarmScheduler = AndroidAlarmScheduler(context, preferencesManager, localStore)

        if (intent.action in RESCHEDULE_ACTIONS) {
            alarmScheduler.rescheduleAllAlarms()
            return
        }

        val assignmentId = intent.getLongExtra(EXTRA_ASSIGNMENT_ID, -1L)
        val alertType = intent.getStringExtra(EXTRA_ALERT_TYPE) ?: return
        val courseName = intent.getStringExtra(EXTRA_COURSE_NAME) ?: "Curso"
        val assignmentName = intent.getStringExtra(EXTRA_ASSIGNMENT_NAME) ?: "Tarea"

        if (assignmentId == -1L) return

        val (title, message) = when (alertType) {
            ALERT_TYPE_30M -> {
                "Alerta Crítica · $courseName" to "$assignmentName: ¡Últimos 30 minutos para la entrega!"
            }
            ALERT_TYPE_3H -> {
                "Alerta de Urgencia · $courseName" to "$assignmentName: Quedan ~3 horas para la entrega"
            }
            ALERT_TYPE_24H -> {
                "Recordatorio Preventivo · $courseName" to "$assignmentName: Entrega programada para las próximas 24h"
            }
            ALERT_TYPE_CUSTOM -> {
                "Recordatorio Personalizado · $courseName" to "$assignmentName: Recordatorio programado para esta entrega"
            }
            else -> return
        }

        // Verificar el estado más fresco en Room para evitar notificaciones obsoletas o duplicadas
        val assignment = localStore.getAssignmentList().firstOrNull { it.id == assignmentId }
            ?: return

        if (assignment.isCompleted || assignment.status == "completed") {
            alarmScheduler.cancel(assignmentId)
            return
        }

        val shouldNotify = when (alertType) {
            ALERT_TYPE_24H -> preferencesManager.notif24h.first() && !assignment.notified24h
            ALERT_TYPE_3H -> preferencesManager.notif3h.first() && !assignment.notified3h
            ALERT_TYPE_30M -> preferencesManager.notif30m.first() && !assignment.notified30m
            ALERT_TYPE_CUSTOM -> true
            else -> false
        }

        if (shouldNotify) {
            val notifId = AndroidAlarmScheduler.generateRequestCode(assignmentId, alertType)
            val course = localStore.getCourseList().firstOrNull { it.id == assignment.courseId }
            notificationHelper.showNotification(
                title = title,
                message = message,
                notificationId = notifId,
                targetId = assignmentId,
                secondaryId = assignment.courseId,
                courseCode = course?.courseCode,
                courseName = courseName,
                type = "TASK_REMINDER"
            )

            when (alertType) {
                ALERT_TYPE_24H -> localStore.markNotified24h(assignmentId)
                ALERT_TYPE_3H -> localStore.markNotified3h(assignmentId)
                ALERT_TYPE_30M -> localStore.markNotified30m(assignmentId)
            }
        }
    }

    companion object {
        private const val TAG = "AssignmentAlarmReceiver"
        private const val RECEIVER_TIMEOUT_MILLIS = 9_000L
        private val RESCHEDULE_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON"
        )

        const val EXTRA_ASSIGNMENT_ID = "extra_assignment_id"
        const val EXTRA_ALERT_TYPE = "extra_alert_type"
        const val EXTRA_COURSE_NAME = "extra_course_name"
        const val EXTRA_ASSIGNMENT_NAME = "extra_assignment_name"

        const val ALERT_TYPE_24H = "24h"
        const val ALERT_TYPE_3H = "3h"
        const val ALERT_TYPE_30M = "30m"
        const val ALERT_TYPE_CUSTOM = "custom"
    }
}
