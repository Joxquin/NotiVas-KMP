package me.joxquin.notivas.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun GoogleAuthWebView(
    authUrl: String,
    redirectUri: String,
    onAuthCodeReceived: (authCode: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
)
