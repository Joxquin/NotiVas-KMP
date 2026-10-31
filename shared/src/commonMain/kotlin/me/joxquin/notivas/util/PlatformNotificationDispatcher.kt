package me.joxquin.notivas.util

expect object PlatformNotificationDispatcher {
    fun showNotification(
        title: String,
        message: String,
        targetId: Long? = null,
        secondaryId: Long? = null,
        courseCode: String? = null,
        courseName: String? = null
    )
}

