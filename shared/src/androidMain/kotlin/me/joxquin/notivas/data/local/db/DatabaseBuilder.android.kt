package me.joxquin.notivas.data.local.db

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<NotivasDatabase> {
    val ctx = getAndroidApplicationContext()
    val dbFile = File(ctx.filesDir, "notivas_database.db")
    return Room.databaseBuilder<NotivasDatabase>(
        context = ctx,
        name = dbFile.absolutePath
    )
}

private fun getAndroidApplicationContext(): Context {
    return try {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentAppMethod = activityThreadClass.getMethod("currentApplication")
        val app = currentAppMethod.invoke(null) as? Application
        app?.applicationContext ?: throw IllegalStateException("Android Context is null")
    } catch (e: Throwable) {
        throw IllegalStateException("Failed to get Android Application Context", e)
    }
}
