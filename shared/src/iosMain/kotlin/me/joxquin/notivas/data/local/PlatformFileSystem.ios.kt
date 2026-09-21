package me.joxquin.notivas.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.dataUsingEncoding
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
actual object PlatformFileSystem {
    actual fun readString(filePath: String): String? {
        return try {
            val fileManager = NSFileManager.defaultManager
            if (!fileManager.fileExistsAtPath(filePath)) return null
            val data = NSData.dataWithContentsOfFile(filePath) ?: return null
            NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        } catch (_: Exception) {
            null
        }
    }

    actual fun writeString(filePath: String, content: String): Boolean {
        return try {
            val nsStr = content as NSString
            val data = nsStr.dataUsingEncoding(NSUTF8StringEncoding) ?: return false
            data.writeToFile(filePath, true)
        } catch (_: Exception) {
            false
        }
    }

    actual fun deleteFile(filePath: String): Boolean {
        return try {
            val fileManager = NSFileManager.defaultManager
            if (fileManager.fileExistsAtPath(filePath)) {
                fileManager.removeItemAtPath(filePath, null)
            } else {
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}
