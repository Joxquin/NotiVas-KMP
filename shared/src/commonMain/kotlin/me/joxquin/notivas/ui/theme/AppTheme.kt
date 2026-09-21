package me.joxquin.notivas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

import me.joxquin.notivas.data.local.AppThemeStyle

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Monet activado por defecto
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    content: @Composable () -> Unit
) {
    val dynamicColors = dynamicColorSchemeOrDefault(darkTheme, dynamicColor)
    val colorScheme = dynamicColors ?: if (darkTheme) WarmDarkColorScheme else WarmLightColorScheme

    val miuixColorSchemeMode = when {
        dynamicColor -> if (darkTheme) top.yukonga.miuix.kmp.theme.ColorSchemeMode.MonetDark else top.yukonga.miuix.kmp.theme.ColorSchemeMode.MonetLight
        darkTheme -> top.yukonga.miuix.kmp.theme.ColorSchemeMode.Dark
        else -> top.yukonga.miuix.kmp.theme.ColorSchemeMode.Light
    }
    val miuixThemeController = androidx.compose.runtime.remember(miuixColorSchemeMode, colorScheme.primary) {
        top.yukonga.miuix.kmp.theme.ThemeController(
            colorSchemeMode = miuixColorSchemeMode,
            keyColor = colorScheme.primary,
            isDark = darkTheme
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AppShapes
    ) {
        top.yukonga.miuix.kmp.theme.MiuixTheme(
            controller = miuixThemeController
        ) {
            content()
        }
    }
}
