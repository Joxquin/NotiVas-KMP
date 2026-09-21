package me.joxquin.notivas.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun dynamicColorSchemeOrDefault(
    darkTheme: Boolean,
    dynamicColor: Boolean
): ColorScheme? = null

actual fun isDynamicColorAvailable(): Boolean = false
