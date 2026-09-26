package me.joxquin.notivas.background

actual object BackgroundSyncScheduler {
    actual fun schedulePeriodicSync(intervalMinutes: Long) {
        // No-op en JVM/Desktop
    }

    actual fun cancelPeriodicSync() {
        // No-op en JVM/Desktop
    }
}
