package me.joxquin.notivas.ui.components.miuix

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.glass.GlassTopAppBar
import top.yukonga.miuix.kmp.glass.GlassIconButton
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported
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
fun rememberMiuixBlurBackdrop(withSurfaceBackground: Boolean = true): LayerBackdrop? {
    if (!isRuntimeShaderSupported()) return null
    val surfaceColor = MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        if (withSurfaceBackground) {
            drawRect(surfaceColor)
        }
        drawContent()
    }
}

fun Modifier.miuixBackdropModifier(backdrop: LayerBackdrop?): Modifier =
    if (backdrop != null) this.layerBackdrop(backdrop) else this

val LocalMiuixPageBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

/**
 * Contenedor de barra superior para vistas HyperOS Miuix. Provee el backdrop de difuminado
 * al árbol de composición para alimentar el desenfoque progresivo de GlassTopAppBar.
 */
@Composable
fun MiuixBlurredBar(
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean = true,
    scrollBehavior: ScrollBehavior? = null,
    alpha: Float? = null,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalMiuixPageBackdrop provides (backdrop.takeIf { blurEnabled })) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

/**
 * TopAppBar canónico para pantallas Miuix, basado en GlassTopAppBar oficial de HyperOS
 * con soporte para modo contraído fijo (expandable = false) o expandido dinámico,
 * desenfoque progresivo reactivo al MiuixScrollBehavior y botones glass.
 */
@Composable
fun MiuixPageTopAppBar(
    title: String,
    largeTitle: String = title,
    scrollBehavior: ScrollBehavior? = null,
    backdrop: LayerBackdrop? = LocalMiuixPageBackdrop.current,
    progressiveBlur: Boolean = true,
    isContentScrolled: Boolean? = null,
    contentModifier: Modifier = Modifier,
    titleAlpha: () -> Float = { 1f },
    backgroundAlpha: () -> Float = { 1f },
    subtitle: String = "",
    color: Color = Color.Transparent,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable () -> Unit = {},
    collapsed: Boolean = false,
) {
    val contentScrolled by remember(scrollBehavior, isContentScrolled, collapsed) {
        derivedStateOf {
            isContentScrolled ?: if (collapsed) true else (scrollBehavior?.state?.let { it.contentOffset < 0f } ?: false)
        }
    }

    GlassTopAppBar(
        title = title,
        largeTitle = largeTitle,
        subtitle = subtitle,
        contentModifier = contentModifier,
        titleAlpha = titleAlpha,
        backgroundAlpha = backgroundAlpha,
        scrollBehavior = if (collapsed) null else scrollBehavior,
        backdrop = backdrop,
        progressiveBlur = progressiveBlur && backdrop != null,
        isContentScrolled = contentScrolled,
        expandable = !collapsed,
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

/**
 * Botón de acción canónico para la barra superior de HyperOS Miuix.
 * Utiliza el componente oficial GlassIconButton de compose-miuix-ui
 * con retroalimentación háptica nativa y adaptación de material glass idéntica a la implementación oficial.
 */
@Composable
fun MiuixTopAppBarAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: LayerBackdrop? = LocalMiuixPageBackdrop.current,
    content: @Composable () -> Unit
) {
    GlassIconButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier
    ) {
        content()
    }
}

