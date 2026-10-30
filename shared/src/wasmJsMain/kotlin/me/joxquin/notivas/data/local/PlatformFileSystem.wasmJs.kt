package me.joxquin.notivas.data.local

import kotlinx.browser.localStorage

actual object PlatformFileSystem {
    actual fun readString(filePath: String): String? {
        return try {
            localStorage.getItem(filePath)
        } catch (_: Throwable) {
            null
        }
    }

    actual fun writeString(filePath: String, content: String): Boolean {
        return try {
            localStorage.setItem(filePath, content)
            true
        } catch (_: Throwable) {
            false
        }
    }

    actual fun deleteFile(filePath: String): Boolean {
        return try {
            localStorage.removeItem(filePath)
            true
        } catch (_: Throwable) {
            false
        }
    }
}
