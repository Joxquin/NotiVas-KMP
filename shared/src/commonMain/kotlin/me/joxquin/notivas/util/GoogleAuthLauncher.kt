package me.joxquin.notivas.util

import androidx.compose.runtime.Composable

expect class GoogleAuthLauncher {
    fun launchSignIn()
}

@Composable
expect fun rememberGoogleAuthLauncher(
    onAuthSuccess: (accessToken: String, refreshToken: String?, userEmail: String?) -> Unit,
    onAuthError: (String) -> Unit
): GoogleAuthLauncher
