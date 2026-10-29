package me.joxquin.notivas.util

actual object PlatformAlarmScheduler {
    actual fun scheduleCustomReminder(
        assignmentId: Long,
        courseName: String,
        assignmentName: String,
        triggerEpochMillis: Long
    ) {
        // No-op en JVM/Desktop
    }
}

