package me.joxquin.notivas.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

actual class BiometricAuthenticator(
    private val activity: FragmentActivity?
) {
    actual fun canAuthenticate(): Boolean {
        if (activity == null) return false
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    actual fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentActivity = activity
        if (currentActivity == null) {
            onError("Actividad no disponible para autenticación biométrica.")
            return
        }

        val executor = ContextCompat.getMainExecutor(currentActivity)
        val biometricPrompt = BiometricPrompt(
            currentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // No llamamos a onError aquí porque BiometricPrompt permite reintentos,
                    // pero se puede registrar o dejar que el usuario reintente.
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onError("Error al iniciar autenticación biométrica: ${e.message}")
        }
    }
}

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    val context = LocalContext.current
    val activity = remember(context) {
        var curr = context
        while (curr is android.content.ContextWrapper) {
            if (curr is FragmentActivity) return@remember curr
            curr = curr.baseContext
        }
        null
    }
    return remember(activity) {
        BiometricAuthenticator(activity)
    }
}
