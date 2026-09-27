package me.joxquin.notivas.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CanvasLoginWebView(
    url: String,
    onLoginSuccess: (cookies: String, csrfToken: String?) -> Unit,
    modifier: Modifier = Modifier,
    onPageStarted: (() -> Unit)? = null,
    onPageFinished: (() -> Unit)? = null
)
