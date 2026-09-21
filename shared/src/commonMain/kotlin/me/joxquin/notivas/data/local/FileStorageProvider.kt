package me.joxquin.notivas.data.local

expect object FileStorageProvider {
    fun getAppDataDirectory(): String
}
