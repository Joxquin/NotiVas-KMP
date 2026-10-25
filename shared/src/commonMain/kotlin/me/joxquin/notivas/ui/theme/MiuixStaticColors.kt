package me.joxquin.notivas.ui.theme

import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

/**
 * Esquemas de color estáticos para MIUIX que respetan fielmente la paleta corporativa
 * Warm Academic / Stitch de NotiVas cuando Monet (dynamicColor) se encuentra desactivado.
 */
val WarmMiuixLightColors: Colors = lightColorScheme(
    primary = AmberDark,
    onPrimary = Color.White,
    primaryVariant = AmberPrimary,
    onPrimaryVariant = Color.White,
    primaryContainer = AmberPrimary,
    onPrimaryContainer = Color.White,
    secondary = SageSecondary,
    onSecondary = Color.White,
    secondaryVariant = SageContainer,
    onSecondaryVariant = OnSageContainer,
    secondaryContainer = SageContainer,
    onSecondaryContainer = OnSageContainer,
    tertiaryContainer = TerracottaContainer,
    onTertiaryContainer = OnTerracottaContainer,
    background = CanvasSurface,
    onBackground = CanvasOnSurface,
    surface = CanvasSurface,
    onSurface = CanvasOnSurface,
    surfaceVariant = CanvasSurfaceVariant,
    onSurfaceVariantSummary = CanvasOnSurfaceVariant,
    onSurfaceVariantActions = SageSecondary,
    surfaceContainer = CanvasSurfaceContainer,
    surfaceContainerHigh = CanvasSurfaceContainerHigh,
    surfaceContainerHighest = CanvasSurfaceContainerHighest,
    outline = CanvasOutline,
    dividerLine = CanvasOutlineVariant
)

val WarmMiuixDarkColors: Colors = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Color(0xFF452B00),
    primaryVariant = AmberDark,
    onPrimaryVariant = Color.White,
    primaryContainer = AmberDark,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFB5CF91),
    onSecondary = Color(0xFF223608),
    secondaryVariant = SageSecondary,
    onSecondaryVariant = Color.White,
    secondaryContainer = SageSecondary,
    onSecondaryContainer = Color.White,
    tertiaryContainer = TerracottaTertiary,
    onTertiaryContainer = Color.White,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF534434),
    onSurfaceVariantSummary = DarkOnSurfaceVariant,
    onSurfaceVariantActions = Color(0xFFB5CF91),
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = Color(0xFF35332E),
    outline = Color(0xFF9F8D7C),
    dividerLine = Color(0xFF534434)
)
