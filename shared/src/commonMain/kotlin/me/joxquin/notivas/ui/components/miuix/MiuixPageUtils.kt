package me.joxquin.notivas.ui.components.miuix

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * Modificadores canónicos de scroll para vistas HyperOS Miuix (patrón NavigateTestPage).
 * Incorpora retroalimentación háptica en topes (scrollEndHaptic), rebote elástico (overScrollVertical)
 * y conexión anidada con el MiuixScrollBehavior del TopAppBar.
 */
fun Modifier.miuixPageScrollModifiers(
    scrollBehavior: ScrollBehavior? = null,
    enableScrollEndHaptic: Boolean = true,
): Modifier = this
    .then(if (enableScrollEndHaptic) Modifier.scrollEndHaptic() else Modifier)
    .overScrollVertical()
    .then(if (scrollBehavior != null) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else Modifier)
    .fillMaxHeight()

/**
 * Calcula el padding de contenido seguro para páginas Miuix a partir del innerPadding
 * del Scaffold y el padding externo (ej. barra de navegación flotante).
 */
@Composable
fun miuixPageContentPadding(
    innerPadding: PaddingValues,
    outerPadding: PaddingValues = PaddingValues(0.dp),
    extraTop: Dp = 0.dp,
    extraStart: Dp = 16.dp,
    extraEnd: Dp = 16.dp,
    extraBottom: Dp = 16.dp,
): PaddingValues {
    val topPadding = innerPadding.calculateTopPadding() + extraTop
    val bottomPadding = outerPadding.calculateBottomPadding() + extraBottom

    return remember(topPadding, bottomPadding, extraStart, extraEnd) {
        PaddingValues(
            top = topPadding,
            start = extraStart,
            end = extraEnd,
            bottom = bottomPadding,
        )
    }
}

/**
 * Backdrop de difuminado reactivo en tiempo real para HyperOS Miuix.
 */
@Composable
fun rememberMiuixBlurBackdrop(): LayerBackdrop? {
    if (!isRuntimeShaderSupported()) return null
    val surfaceColor = MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}

fun Modifier.miuixBackdropModifier(backdrop: LayerBackdrop?): Modifier =
    if (backdrop != null) this.layerBackdrop(backdrop) else this

/**
 * Contenedor de barra superior con desenfoque progresivo reactivo al MiuixScrollBehavior.
 */
@Composable
fun MiuixBlurredBar(
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean = true,
    scrollBehavior: ScrollBehavior? = null,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { 48.dp.toPx() }
    val blurActive = blurEnabled && backdrop != null

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (blurActive) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        alpha = scrollBehavior?.state?.let {
                            (-it.contentOffset / thresholdPx).coerceIn(0f, 1f)
                        } ?: 1f
                    }
                    .progressiveTextureBlur(
                        backdrop = backdrop,
                        shape = RectangleShape,
                        gradient = ProgressiveBlur.Top.copy(curve = 2.2f),
                        blurRadius = 16f,
                        colors = miuixBarBlurColors(progressive = true),
                    ),
            )
        }
        content()
    }
}

@Composable
private fun miuixBarBlurColors(progressive: Boolean = false): BlurColors = BlurDefaults.blurColors(
    blendColors = listOf(
        BlendColorEntry(color = MiuixTheme.colorScheme.surface.copy(if (progressive) 0.35f else 0.85f)),
    ),
)

/**
 * TopAppBar canónico para pantallas Miuix, conectado con MiuixScrollBehavior.
 */
@Composable
fun MiuixPageTopAppBar(
    title: String,
    scrollBehavior: ScrollBehavior? = null,
    subtitle: String = "",
    color: Color = Color.Transparent,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = title,
        subtitle = subtitle,
        color = color,
        scrollBehavior = scrollBehavior,
        navigationIcon = navigationIcon,
        actions = actions,
        bottomContent = bottomContent,
    )
}

/**
 * Recorta y limpia el nombre del curso para que se visualice de forma compacta en TopAppBars
 * sin desbordarse en pantallas pequeñas ni colisionar con botones de navegación o acción.
 */
fun shortenCourseSubtitle(courseName: String?, maxLength: Int = 36): String {
    if (courseName.isNullOrBlank()) return ""
    // Los nombres de Canvas suelen incluir códigos de sección: "Nombre del Curso - C24 4to C-A - ..."
    val cleanName = if (courseName.contains(" - ")) {
        val parts = courseName.split(" - ")
        parts.firstOrNull()?.trim().takeIf { !it.isNullOrBlank() } ?: courseName.trim()
    } else {
        courseName.trim()
    }

    return if (cleanName.length > maxLength) {
        cleanName.take(maxLength - 1).trimEnd() + "…"
    } else {
        cleanName
    }
}

