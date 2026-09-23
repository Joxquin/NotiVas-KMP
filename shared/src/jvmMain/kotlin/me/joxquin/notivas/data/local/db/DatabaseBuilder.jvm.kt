package me.joxquin.notivas.data.local.db

import androidx.room.Room
import androidx.room.RoomDatabase
import me.joxquin.notivas.data.local.FileStorageProvider
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<NotivasDatabase> {
    val dbFile = File(FileStorageProvider.getAppDataDirectory(), "notivas_database.db")
    return Room.databaseBuilder<NotivasDatabase>(
        name = dbFile.absolutePath
    )
}
