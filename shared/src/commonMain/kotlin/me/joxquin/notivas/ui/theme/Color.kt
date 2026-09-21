package me.joxquin.notivas.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ─── Warm Academic Copilot Palette (Stitch) ──────────────────────────────────
val AmberPrimary = Color(0xFFF59E0B)
val AmberDark = Color(0xFF855300)
val AmberContainer = Color(0xFFFFDDB8)
val OnAmberContainer = Color(0xFF2A1700)

val SageSecondary = Color(0xFF4F6532)
val SageContainer = Color(0xFFD1ECAB)
val OnSageContainer = Color(0xFF102000)

val TerracottaTertiary = Color(0xFFA23E18)
val TerracottaContainer = Color(0xFFFFDBCF)
val OnTerracottaContainer = Color(0xFF390C00)

val CanvasSurface = Color(0xFFFCF9F4)
val CanvasSurfaceDim = Color(0xFFDCDAD5)
val CanvasSurfaceBright = Color(0xFFFCF9F4)
val CanvasSurfaceContainerLowest = Color(0xFFFFFFFF)
val CanvasSurfaceContainerLow = Color(0xFFF6F3EE)
val CanvasSurfaceContainer = Color(0xFFF0EDE9)
val CanvasSurfaceContainerHigh = Color(0xFFEBE8E3)
val CanvasSurfaceContainerHighest = Color(0xFFE5E2DD)

val CanvasOnSurface = Color(0xFF1C1C19)
val CanvasOnSurfaceVariant = Color(0xFF534434)
val CanvasSurfaceVariant = Color(0xFFE5E2DD)
val CanvasOutline = Color(0xFF867461)
val CanvasOutlineVariant = Color(0xFFD8C3AD)

// Dark Palette
val DarkSurface = Color(0xFF141311)
val DarkSurfaceContainer = Color(0xFF211F1C)
val DarkSurfaceContainerHigh = Color(0xFF2B2A26)
val DarkOnSurface = Color(0xFFE5E2DD)
val DarkOnSurfaceVariant = Color(0xFFD8C3AD)

val WarmLightColorScheme = lightColorScheme(
    primary = AmberDark,
    onPrimary = Color.White,
    primaryContainer = AmberPrimary,
    onPrimaryContainer = Color.White,
    secondary = SageSecondary,
    onSecondary = Color.White,
    secondaryContainer = SageContainer,
    onSecondaryContainer = OnSageContainer,
    tertiary = TerracottaTertiary,
    onTertiary = Color.White,
    tertiaryContainer = TerracottaContainer,
    onTertiaryContainer = OnTerracottaContainer,
    background = CanvasSurface,
    onBackground = CanvasOnSurface,
    surface = CanvasSurface,
    onSurface = CanvasOnSurface,
    surfaceVariant = CanvasSurfaceVariant,
    onSurfaceVariant = CanvasOnSurfaceVariant,
    surfaceContainer = CanvasSurfaceContainer,
    surfaceContainerLow = CanvasSurfaceContainerLow,
    surfaceContainerHigh = CanvasSurfaceContainerHigh,
    surfaceContainerHighest = CanvasSurfaceContainerHighest,
    surfaceContainerLowest = CanvasSurfaceContainerLowest,
    outline = CanvasOutline,
    outlineVariant = CanvasOutlineVariant
)

val WarmDarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Color(0xFF452B00),
    primaryContainer = AmberDark,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFB5CF91),
    onSecondary = Color(0xFF223608),
    secondaryContainer = SageSecondary,
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFFFB59C),
    onTertiary = Color(0xFF5C1B02),
    tertiaryContainer = TerracottaTertiary,
    onTertiaryContainer = Color.White,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF534434),
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerLowest = Color(0xFF0F0E0D),
    outline = Color(0xFF9F8D7C),
    outlineVariant = Color(0xFF534434)
)
