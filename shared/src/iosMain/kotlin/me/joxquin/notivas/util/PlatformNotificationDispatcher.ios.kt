package me.joxquin.notivas.util

actual object PlatformNotificationDispatcher {
    actual fun showNotification(
        title: String,
        message: String,
        targetId: Long?,
        secondaryId: Long?,
        courseCode: String?,
        courseName: String?,
        type: String?
    ) {
        // No-op en iOS
    }
}

