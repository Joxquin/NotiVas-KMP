package me.joxquin.notivas.util

expect object PlatformAlarmScheduler {
    fun scheduleCustomReminder(
        assignmentId: Long,
        courseName: String,
        assignmentName: String,
        triggerEpochMillis: Long
    )
}

