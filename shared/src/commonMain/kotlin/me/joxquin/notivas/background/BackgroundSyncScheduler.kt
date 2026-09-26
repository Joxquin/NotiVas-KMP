package me.joxquin.notivas.background

expect object BackgroundSyncScheduler {
    fun schedulePeriodicSync(intervalMinutes: Long)
    fun cancelPeriodicSync()
}
