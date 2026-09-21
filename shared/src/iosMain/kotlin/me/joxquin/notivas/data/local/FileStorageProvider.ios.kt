package me.joxquin.notivas.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSFileManager

@OptIn(ExperimentalForeignApi::class)
actual object FileStorageProvider {
    actual fun getAppDataDirectory(): String {
        val paths = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true)
        val basePath = paths.firstOrNull() as? String ?: "./Documents"
        val fullPath = "$basePath/NotiVas"
        NSFileManager.defaultManager.createDirectoryAtPath(fullPath, withIntermediateDirectories = true, attributes = null, error = null)
        return fullPath
    }
}
