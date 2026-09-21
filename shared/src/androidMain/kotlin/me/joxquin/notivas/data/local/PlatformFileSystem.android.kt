package me.joxquin.notivas.data.local

import java.io.File

actual object PlatformFileSystem {
    actual fun readString(filePath: String): String? {
        return try {
            val file = File(filePath)
            if (file.exists() && file.isFile) file.readText() else null
        } catch (_: Exception) {
            null
        }
    }

    actual fun writeString(filePath: String, content: String): Boolean {
        return try {
            val file = File(filePath)
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (_: Exception) {
            false
        }
    }

    actual fun deleteFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) file.delete() else true
        } catch (_: Exception) {
            false
        }
    }
}
