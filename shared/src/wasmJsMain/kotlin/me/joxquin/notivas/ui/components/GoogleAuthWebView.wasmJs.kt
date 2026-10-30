package me.joxquin.notivas.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun GoogleAuthWebView(
    authUrl: String,
    redirectUri: String,
    onAuthCodeReceived: (authCode: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Autenticación por WebView disponible en Android.")
    }
}
