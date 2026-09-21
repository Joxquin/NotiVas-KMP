package me.joxquin.notivas.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Provee el ColorScheme dinámico si la plataforma lo soporta (Android 12+ Monet),
 * o devuelve null si no está disponible o está deshabilitado.
 */
@Composable
expect fun dynamicColorSchemeOrDefault(
    darkTheme: Boolean,
    dynamicColor: Boolean
): ColorScheme?

expect fun isDynamicColorAvailable(): Boolean
