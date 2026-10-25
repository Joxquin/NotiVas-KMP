package me.joxquin.notivas.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics

actual class BiometricAuthenticator {
    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    actual fun canAuthenticate(): Boolean {
        val context = LAContext()
        val error = null
        return context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, error)
    }

    actual fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val context = LAContext()
        val reason = if (subtitle.isNotBlank()) "$title - $subtitle" else title
        context.evaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            localizedReason = reason
        ) { success, error ->
            platform.darwin.dispatch_async(platform.darwin.dispatch_get_main_queue()) {
                if (success) {
                    onSuccess()
                } else {
                    onError(error?.localizedDescription ?: "Error de autenticación biométrica")
                }
            }
        }
    }
}

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    return remember { BiometricAuthenticator() }
}
