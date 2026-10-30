package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class GoogleAuthLauncher(
    private val onLaunch: () -> Unit
) {
    actual fun launchSignIn() {
        onLaunch()
    }
}

@Composable
actual fun rememberGoogleAuthLauncher(
    onAuthSuccess: (accessToken: String, refreshToken: String?, userEmail: String?) -> Unit,
    onAuthError: (String) -> Unit
): GoogleAuthLauncher {
    return remember {
        GoogleAuthLauncher {
            onAuthError("Google Sign-In Web está disponible próximamente.")
        }
    }
}
