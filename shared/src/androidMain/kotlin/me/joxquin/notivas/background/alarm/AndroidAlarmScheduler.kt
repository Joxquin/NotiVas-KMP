package me.joxquin.notivas.background.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.first
import me.joxquin.notivas.data.local.InMemoryLocalStore
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.model.Assignment
import java.time.Duration
import java.time.ZonedDateTime

data class AssignmentAlarmItem(
    val id: Long,
    val name: String,
    val dueAt: String?,
    val lockAt: String?,
    val courseName: String,
    val isCompleted: Boolean,
    val notified24h: Boolean,
    val notified3h: Boolean,
    val notified30m: Boolean
) {
    companion object {
        fun fromAssignment(assignment: Assignment, courseName: String): AssignmentAlarmItem {
            return AssignmentAlarmItem(
                id = assignment.id,
                name = assignment.name,
                dueAt = assignment.dueAt,
                lockAt = assignment.lockAt,
                courseName = courseName,
                isCompleted = assignment.isCompleted || assignment.status == "completed",
                notified24h = assignment.notified24h,
                notified3h = assignment.notified3h,
                notified30m = assignment.notified30m
            )
        }
    }
}

class AndroidAlarmScheduler(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val localStore: InMemoryLocalStore
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun schedule(item: AssignmentAlarmItem) {
        if (item.isCompleted) {
            cancel(item.id)
            return
        }

        val dueStr = item.dueAt ?: item.lockAt ?: return
        val dueDate = try {
            ZonedDateTime.parse(dueStr)
        } catch (_: Exception) {
            return
        }

        val now = ZonedDateTime.now()
        if (now.isAfter(dueDate)) {
            cancel(item.id)
            return
        }

        val notif24hEnabled = preferencesManager.notif24h.first()
        val notif3hEnabled = preferencesManager.notif3h.first()
        val notif30mEnabled = preferencesManager.notif30m.first()

        val shortName = if (item.name.length > 38) {
            item.name.take(35) + "..."
        } else {
            item.name
        }

        // 1. Alerta Crítica (30 minutos o menos)
        if (notif30mEnabled && !item.notified30m) {
            val triggerTime30m = dueDate.minusMinutes(30)
            if (triggerTime30m.isAfter(now)) {
                scheduleExactAlarm(
                    assignmentId = item.id,
                    alertType = AssignmentAlarmReceiver.ALERT_TYPE_30M,
                    triggerEpochMillis = triggerTime30m.toInstant().toEpochMilli(),
                    courseName = item.courseName,
                    assignmentName = shortName
                )
            }
        }

        // 2. Alerta de Urgencia (3 horas antes)
        if (notif3hEnabled && !item.notified3h) {
            val triggerTime3h = dueDate.minusHours(3)
            if (triggerTime3h.isAfter(now)) {
                scheduleExactAlarm(
                    assignmentId = item.id,
                    alertType = AssignmentAlarmReceiver.ALERT_TYPE_3H,
                    triggerEpochMillis = triggerTime3h.toInstant().toEpochMilli(),
                    courseName = item.courseName,
                    assignmentName = shortName
                )
            }
        }

        // 3. Alerta Preventiva (24 horas antes)
        if (notif24hEnabled && !item.notified24h) {
            val triggerTime24h = dueDate.minusHours(24)
            if (triggerTime24h.isAfter(now)) {
                scheduleExactAlarm(
                    assignmentId = item.id,
                    alertType = AssignmentAlarmReceiver.ALERT_TYPE_24H,
                    triggerEpochMillis = triggerTime24h.toInstant().toEpochMilli(),
                    courseName = item.courseName,
                    assignmentName = shortName
                )
            }
        }
    }

    suspend fun scheduleAlarmsForAssignment(assignment: Assignment, courseName: String) {
        schedule(AssignmentAlarmItem.fromAssignment(assignment, courseName))
    }

    fun cancel(assignmentId: Long) {
        listOf(
            AssignmentAlarmReceiver.ALERT_TYPE_24H,
            AssignmentAlarmReceiver.ALERT_TYPE_3H,
            AssignmentAlarmReceiver.ALERT_TYPE_30M
        ).forEach { alertType ->
            val intent = Intent(context, AssignmentAlarmReceiver::class.java)
            val requestCode = generateRequestCode(assignmentId, alertType)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    suspend fun rescheduleAllAlarms() {
        try {
            val assignments = localStore.getAllAssignments().first()
            val courses = localStore.getAllCourses().first()
            val courseMap = courses.associateBy { it.id }

            assignments.forEach { assignment ->
                val courseName = courseMap[assignment.courseId]?.name ?: "Curso"
                scheduleAlarmsForAssignment(assignment, courseName)
            }
        } catch (e: Exception) {
            Log.e("AndroidAlarmScheduler", "Error rescheduling all alarms", e)
        }
    }

    private fun scheduleExactAlarm(
        assignmentId: Long,
        alertType: String,
        triggerEpochMillis: Long,
        courseName: String,
        assignmentName: String
    ) {
        val intent = Intent(context, AssignmentAlarmReceiver::class.java).apply {
            putExtra(AssignmentAlarmReceiver.EXTRA_ASSIGNMENT_ID, assignmentId)
            putExtra(AssignmentAlarmReceiver.EXTRA_ALERT_TYPE, alertType)
            putExtra(AssignmentAlarmReceiver.EXTRA_COURSE_NAME, courseName)
            putExtra(AssignmentAlarmReceiver.EXTRA_ASSIGNMENT_NAME, assignmentName)
        }

        val requestCode = generateRequestCode(assignmentId, alertType)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e("AndroidAlarmScheduler", "Error scheduling alarm for assignment $assignmentId", e)
        }
    }

    companion object {
        fun generateRequestCode(assignmentId: Long, alertType: String): Int {
            val typeOffset = when (alertType) {
                AssignmentAlarmReceiver.ALERT_TYPE_24H -> 1
                AssignmentAlarmReceiver.ALERT_TYPE_3H -> 2
                AssignmentAlarmReceiver.ALERT_TYPE_30M -> 3
                else -> 0
            }
            return ((assignmentId % 1000000) * 10 + typeOffset).toInt()
        }
    }
}
