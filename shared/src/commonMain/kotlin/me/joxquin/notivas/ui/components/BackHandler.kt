package me.joxquin.notivas.ui.components

import androidx.compose.runtime.Composable

/**
 * Multiplatform BackHandler expect function.
 * On Android, intercepts system back button and gestures.
 * On JVM / iOS, provides a no-op fallback.
 */
@Composable
expect fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
)
