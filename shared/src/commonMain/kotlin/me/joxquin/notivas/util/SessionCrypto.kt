package me.joxquin.notivas.util

expect object SessionCrypto {
    fun encrypt(plainText: String, pin: String): Pair<String, String> // Pair(encryptedBase64, ivBase64)
}

