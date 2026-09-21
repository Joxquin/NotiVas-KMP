package me.joxquin.notivas.data.local

expect object PlatformFileSystem {
    fun readString(filePath: String): String?
    fun writeString(filePath: String, content: String): Boolean
    fun deleteFile(filePath: String): Boolean
}
