package me.joxquin.notivas.ui.components.miuix

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.computedStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.glass.GlassTopAppBar
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
import top.yukonga.miuix.kmp.glass.GlassIconButton
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

val LocalTopAppBarTitleAlpha = staticCompositionLocalOf<() -> Float> { { 1f } }
val LocalTopAppBarCenterObstructionStart = staticCompositionLocalOf<() -> Dp> { { Dp.Unspecified } }

/**
 * Contrato de áreas seguras e insets de página siguiendo el patrón oficial de miuix/example.
 */
@Stable
class AppPageInsets(
    val windowInsets: WindowInsets,
    val safeInsets: WindowInsets,
    private val overlayStart: () -> Dp,
    val searchTop: () -> Dp,
) {
    val contentInsets: WindowInsets = object : WindowInsets {
        override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int =
            safeInsets.getLeft(density, layoutDirection) +
                if (layoutDirection == LayoutDirection.Ltr) with(density) { overlayStart().roundToPx() } else 0

        override fun getRight(density: Density, layoutDirection: LayoutDirection): Int =
            safeInsets.getRight(density, layoutDirection) +
                if (layoutDirection == LayoutDirection.Rtl) with(density) { overlayStart().roundToPx() } else 0

        override fun getTop(density: Density): Int = safeInsets.getTop(density)

        override fun getBottom(density: Density): Int = safeInsets.getBottom(density)
    }

    fun whenHidden(hidden: () -> Boolean): AppPageInsets = AppPageInsets(
        windowInsets = windowInsets,
        safeInsets = safeInsets,
        overlayStart = overlayStart.frozenWhile(hidden),
        searchTop = searchTop.frozenWhile(hidden),
    )
}

private fun (() -> Dp).frozenWhile(frozen: () -> Boolean): () -> Dp {
    val live = this
    var last = Dp.Unspecified
    return {
        val held = last
        if (frozen() && held != Dp.Unspecified) held else live().also { last = it }
    }
}

val LocalAppPageInsets = staticCompositionLocalOf {
    AppPageInsets(WindowInsets(0), WindowInsets(0), { 0.dp }, { 0.dp })
}

@Composable
fun rememberAppPageInsets(
    hostPadding: PaddingValues = PaddingValues(0.dp),
    startAlreadyReserved: Boolean = false,
    overlayStart: () -> Dp = { 0.dp },
    searchTop: () -> Dp = { 0.dp },
): AppPageInsets {
    val systemInsets = WindowInsets.systemBars.union(WindowInsets.captionBar).union(WindowInsets.displayCutout)
    return remember(systemInsets, hostPadding, startAlreadyReserved, overlayStart, searchTop) {
        val hostInsets = object : WindowInsets {
            override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int = with(density) {
                hostPadding.calculateLeftPadding(layoutDirection).roundToPx()
            }

            override fun getRight(density: Density, layoutDirection: LayoutDirection): Int = with(density) {
                hostPadding.calculateRightPadding(layoutDirection).roundToPx()
            }

            override fun getTop(density: Density): Int = with(density) { hostPadding.calculateTopPadding().roundToPx() }

            override fun getBottom(density: Density): Int = with(density) { hostPadding.calculateBottomPadding().roundToPx() }
        }
        val windowInsets = systemInsets.union(hostInsets)
        val safeInsets = if (startAlreadyReserved) {
            windowInsets.only(WindowInsetsSides.End + WindowInsetsSides.Vertical)
        } else {
            windowInsets
        }
        AppPageInsets(windowInsets, safeInsets, overlayStart, searchTop)
    }
}

@Composable
fun Modifier.appPageHorizontalInsets(): Modifier = windowInsetsPadding(
    LocalAppPageInsets.current.contentInsets.only(WindowInsetsSides.Horizontal),
)

@Composable
fun Modifier.appPageTopBarInsets(): Modifier = appPageHorizontalInsets().windowInsetsPadding(
    LocalAppPageInsets.current.safeInsets.only(WindowInsetsSides.Top),
)

@Composable
fun Modifier.appPageSearchInsets(): Modifier {
    val searchTop = LocalAppPageInsets.current.searchTop
    return appPageHorizontalInsets().layout { measurable, constraints ->
        val top = searchTop().roundToPx().coerceIn(0, constraints.maxHeight)
        val placeable = measurable.measure(constraints.offset(vertical = -top))
        layout(placeable.width, placeable.height + top) {
            placeable.placeRelative(0, top)
        }
    }
}

@Composable
fun rememberAppSafePadding(): PaddingValues = LocalAppPageInsets.current.safeInsets.asPaddingValues()

/**
 * Modificadores canónicos de scroll para vistas HyperOS Miuix (patrón NavigateTestPage).
 * Incorpora retroalimentación háptica en topes (scrollEndHaptic), rebote elástico (overScrollVertical)
 * y conexión anidada con el MiuixScrollBehavior del TopAppBar.
 */
fun Modifier.miuixPageScrollModifiers(
    scrollBehavior: ScrollBehavior? = null,
    enableScrollEndHaptic: Boolean = true,
): Modifier {
    val onOffsetChanged: ((Float) -> Unit)? = scrollBehavior?.let { sb ->
        { sb.state.overscrollOffset = it }
    }
    return this
        .then(if (enableScrollEndHaptic) Modifier.scrollEndHaptic() else Modifier)
        .overScrollVertical(onOffsetChanged = onOffsetChanged)
        .then(if (scrollBehavior != null) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else Modifier)
        .fillMaxHeight()
}

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
    defaultWindowInsetsPadding: Boolean = true,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable () -> Unit = {},
    collapsed: Boolean = false,
) {
    val surfaceColor = MiuixTheme.colorScheme.surface
    val bandBrush = remember(surfaceColor) { GlassTopAppBarDefaults.bandBrush(surfaceColor) }
    val chromeTitleAlpha = LocalTopAppBarTitleAlpha.current
    val centerObstructionStart = LocalTopAppBarCenterObstructionStart.current()
    val contentScrolled by remember(scrollBehavior, isContentScrolled, collapsed) {
        computedStateOf {
            isContentScrolled ?: if (collapsed) true else (
                scrollBehavior?.state?.let { it.contentOffset < -1f || it.overscrollOffset < -1f } ?: false
                )
        }
    }

    GlassTopAppBar(
        title = title,
        largeTitle = largeTitle,
        subtitle = subtitle,
        contentModifier = contentModifier,
        titleAlpha = { titleAlpha() * chromeTitleAlpha() },
        backgroundAlpha = backgroundAlpha,
        scrollBehavior = if (collapsed) null else scrollBehavior,
        backdrop = backdrop,
        progressiveBlur = progressiveBlur && backdrop != null,
        isContentScrolled = contentScrolled,
        expandable = !collapsed,
        centerObstructionStart = centerObstructionStart,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
        bandBrush = bandBrush,
        bandOverhang = GlassTopAppBarDefaults.BandOverhang,
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

