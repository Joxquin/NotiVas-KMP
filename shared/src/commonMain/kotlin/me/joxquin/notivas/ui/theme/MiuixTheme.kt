package me.joxquin.notivas.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class MiuixColorScheme(
    val bg: Color = Color(0xFF0C0D0E),
    val surface: Color = Color(0xFF16171A),
    val card: Color = Color(0xFF1B1D22),
    val elevation: Color = Color(0xFF222327),
    val border: Color = Color(0x14FFFFFF),
    val borderBright: Color = Color(0x28FFFFFF),
    val blue: Color = Color(0xFF3482FF),
    val red: Color = Color(0xFFFF5449),
    val redBg: Color = Color(0x26FF5449),
    val amber: Color = Color(0xFFFFB74D),
    val amberBg: Color = Color(0x26FFB74D),
    val green: Color = Color(0xFF66BB6A),
    val greenBg: Color = Color(0x2666BB6A),
    val textPrimary: Color = Color(0xFFEDEDED),
    val textSecondary: Color = Color(0x80FFFFFF),
    val textTertiary: Color = Color(0x50FFFFFF),
    val liquidGlassSurface: Color = Color(0xCC141519)
)

val LocalMiuixColorScheme = staticCompositionLocalOf { MiuixColorScheme() }

object MiuixTheme {
    val colorScheme: MiuixColorScheme
        @Composable
        get() = LocalMiuixColorScheme.current
}

@Composable
fun MiuixTheme(
    content: @Composable () -> Unit
) {
    val miuixColorScheme = MiuixColorScheme()
    CompositionLocalProvider(
        LocalMiuixColorScheme provides miuixColorScheme,
        content = content
    )
}
