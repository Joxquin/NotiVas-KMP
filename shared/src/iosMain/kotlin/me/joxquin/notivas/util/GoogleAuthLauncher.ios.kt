package me.joxquin.notivas.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class GoogleAuthLauncher {
    actual fun launchSignIn() {}
}

@Composable
actual fun rememberGoogleAuthLauncher(
    onAuthSuccess: (accessToken: String, refreshToken: String?, userEmail: String?) -> Unit,
    onAuthError: (String) -> Unit
): GoogleAuthLauncher {
    return remember { GoogleAuthLauncher() }
}
