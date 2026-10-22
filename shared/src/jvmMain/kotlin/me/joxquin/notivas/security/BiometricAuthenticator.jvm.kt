package me.joxquin.notivas.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class BiometricAuthenticator {
    actual fun canAuthenticate(): Boolean = false

    actual fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // En desktop/JVM no hay biometría nativa estándar; llamamos onSuccess o informamos
        onSuccess()
    }
}

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    return remember { BiometricAuthenticator() }
}
