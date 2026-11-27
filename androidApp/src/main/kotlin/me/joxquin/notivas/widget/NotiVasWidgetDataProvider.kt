package me.joxquin.notivas.widget

import android.content.Context
import kotlinx.coroutines.flow.first
import me.joxquin.notivas.data.local.FileStorageProvider
import me.joxquin.notivas.di.AppModule
import me.joxquin.notivas.domain.grades.GradeInsights
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class WidgetDueDay {
    TODAY,
    TOMORROW
}

data class WidgetPendingTask(
    val assignmentId: Long,
    val courseId: Long,
    val title: String,
    val courseName: String,
    val dueDay: WidgetDueDay,
    val dueTimeText: String,
    val dueEpochMillis: Long
)

data class NotiVasWidgetState(
    val overallAverage: Double?,
    val gradedCourseCount: Int,
    val pendingTasks: List<WidgetPendingTask>
)

object NotiVasWidgetDataProvider {

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    suspend fun load(context: Context): NotiVasWidgetState {
        FileStorageProvider.init(context.applicationContext)

        val localStore = AppModule.localStore
        val calculator = AppModule.academicCalculatorUseCase

        val courses = localStore.getAllCourses().first()
        val assignments = localStore.getAssignmentList()
        val groups = localStore.getAllGroupsWithItemsList()

        val assignmentsByCourse = assignments.groupBy { it.courseId }
        val groupsByCourse = groups.groupBy { it.group.courseId }

        val snapshots = courses.map { course ->
            GradeInsights.snapshot(
                course = course,
                courseAssignments = assignmentsByCourse[course.id].orEmpty(),
                courseGroups = groupsByCourse[course.id].orEmpty(),
                calculator = calculator
            )
        }

        val overallAverage = GradeInsights.overallAverage(snapshots)
        val gradedCourseCount = snapshots.count { (it.currentAverage ?: 0.0) > 0.0 }

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val tomorrow = today.plusDays(1)
        val courseNameById = courses.associate { it.id to it.name }

        val pendingTasks = assignments
            .asSequence()
            .filter { !it.isCompleted && it.status != "completed" && !it.isLocked }
            .mapNotNull { assignment ->
                val dueIso = assignment.dueAt ?: return@mapNotNull null
                val zoned = parseIsoToZonedDateTime(dueIso, zone) ?: return@mapNotNull null
                val localDate = zoned.toLocalDate()
                val dueDay = when (localDate) {
                    today -> WidgetDueDay.TODAY
                    tomorrow -> WidgetDueDay.TOMORROW
                    else -> return@mapNotNull null
                }
                WidgetPendingTask(
                    assignmentId = assignment.id,
                    courseId = assignment.courseId,
                    title = assignment.name,
                    courseName = courseNameById[assignment.courseId] ?: "",
                    dueDay = dueDay,
                    dueTimeText = zoned.format(timeFormatter),
                    dueEpochMillis = zoned.toInstant().toEpochMilli()
                )
            }
            .sortedBy { it.dueEpochMillis }
            .toList()

        return NotiVasWidgetState(
            overallAverage = overallAverage,
            gradedCourseCount = gradedCourseCount,
            pendingTasks = pendingTasks
        )
    }

    private fun parseIsoToZonedDateTime(iso: String, zone: ZoneId): ZonedDateTime? {
        return try {
            ZonedDateTime.parse(iso).withZoneSameInstant(zone)
        } catch (_: Exception) {
            try {
                Instant.parse(iso).atZone(zone)
            } catch (_: Exception) {
                null
            }
        }
    }
}
