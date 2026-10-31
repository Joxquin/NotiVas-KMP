package me.joxquin.notivas.util

import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

actual object SessionCrypto {
    private val random = SecureRandom()
    private val fixedSalt = "NotiVasSessionSalt2026".toByteArray(StandardCharsets.UTF_8)

    actual fun encrypt(plainText: String, pin: String): Pair<String, String> {
        val spec = PBEKeySpec(pin.toCharArray(), fixedSalt, 100000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val iv = ByteArray(12)
        random.nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

        val encryptedB64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)

        return Pair(encryptedB64, ivB64)
    }
}

