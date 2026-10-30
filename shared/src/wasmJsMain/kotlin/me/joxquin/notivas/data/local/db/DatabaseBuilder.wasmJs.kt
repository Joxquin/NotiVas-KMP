package me.joxquin.notivas.data.local.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.w3c.dom.Worker

private fun createDbWorker(): Worker {
    return Worker("sqlite-worker.js")
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<NotivasDatabase> {
    return Room.databaseBuilder<NotivasDatabase>(
        name = "notivas_database.db"
    ).setDriver(WebWorkerSQLiteDriver(createDbWorker()))
}
