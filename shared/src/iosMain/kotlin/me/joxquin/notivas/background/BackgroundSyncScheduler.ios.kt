package me.joxquin.notivas.background

actual object BackgroundSyncScheduler {
    actual fun schedulePeriodicSync(intervalMinutes: Long) {
        // En iOS el background fetch se gestiona vía BackgroundTasks framework nativo
    }

    actual fun cancelPeriodicSync() {
        // Cancelar tareas de BackgroundTasks
    }
}
