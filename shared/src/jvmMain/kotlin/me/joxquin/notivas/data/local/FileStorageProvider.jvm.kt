package me.joxquin.notivas.data.local

import java.io.File

actual object FileStorageProvider {
    actual fun getAppDataDirectory(): String {
        val userHome = System.getProperty("user.home") ?: "."
        val os = System.getProperty("os.name")?.lowercase() ?: ""
        val dir = when {
            os.contains("win") -> {
                val appData = System.getenv("APPDATA")
                if (!appData.isNullOrBlank()) File(appData, "NotiVas") else File(userHome, "AppData/Roaming/NotiVas")
            }
            os.contains("mac") -> File(userHome, "Library/Application Support/NotiVas")
            else -> {
                val xdgData = System.getenv("XDG_DATA_HOME")
                if (!xdgData.isNullOrBlank()) File(xdgData, "notivas") else File(userHome, ".local/share/notivas")
            }
        }
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.absolutePath
    }
}
