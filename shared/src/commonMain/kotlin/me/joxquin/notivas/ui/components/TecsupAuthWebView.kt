package me.joxquin.notivas.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun TecsupAuthWebView(
    authUrl: String,
    onTokenReceived: (token: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
)
