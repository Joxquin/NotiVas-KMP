package me.joxquin.notivas.security

import androidx.compose.runtime.Composable

expect class BiometricAuthenticator {
    fun canAuthenticate(): Boolean
    fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}

@Composable
expect fun rememberBiometricAuthenticator(): BiometricAuthenticator
