package me.joxquin.notivas.util

actual object SessionCrypto {
    actual fun encrypt(plainText: String, pin: String): Pair<String, String> {
        return Pair("", "")
    }
}

