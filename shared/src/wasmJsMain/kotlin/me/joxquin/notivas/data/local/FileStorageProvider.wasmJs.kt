package me.joxquin.notivas.data.local

actual object FileStorageProvider {
    actual fun getAppDataDirectory(): String = "/notivas_data"
}
