package me.joxquin.notivas.ui.components.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Representa el estado finito del buscador MIUIX, siguiendo la arquitectura
 * de referencia de KernelSU y compose-miuix-ui.
 */
@Immutable
data class SearchStatus(
    val label: String = "",
    val searchText: String = "",
    val current: Status = Status.COLLAPSED,
    val offsetY: Dp = 0.dp,
    val resultStatus: ResultStatus = ResultStatus.DEFAULT,
) {
    fun isExpand() = current == Status.EXPANDED
    fun isCollapsed() = current == Status.COLLAPSED
    fun shouldExpand() = current == Status.EXPANDED || current == Status.EXPANDING
    fun shouldCollapsed() = current == Status.COLLAPSED || current == Status.COLLAPSING
    fun isAnimatingExpand() = current == Status.EXPANDING

    fun onAnimationComplete(): SearchStatus = when (current) {
        Status.EXPANDING -> copy(current = Status.EXPANDED)
        Status.COLLAPSING -> copy(searchText = "", current = Status.COLLAPSED)
        else -> this
    }

    @Composable
    fun TopAppBarAnim(
        modifier: Modifier = Modifier,
        visible: Boolean = shouldCollapsed(),
        backgroundColor: Color = MiuixTheme.colorScheme.surface,
        content: @Composable () -> Unit
    ) {
        Box(modifier = modifier) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(backgroundColor)
            )
            Box(
                modifier = Modifier.graphicsLayer { alpha = if (visible) 1f else 0f }
            ) { content() }
        }
    }

    enum class Status {
        EXPANDED,
        EXPANDING,
        COLLAPSED,
        COLLAPSING,
    }

    enum class ResultStatus {
        DEFAULT,
        EMPTY,
        LOAD,
        SHOW,
    }
}

/**
 * SearchBox: oculta el feed/contenido subyacente cuando la búsqueda se expande,
 * evitando colisiones de eventos táctiles y pintado fantasma (patrón KernelSU).
 */
@Composable
fun SearchStatus.SearchBox(
    content: @Composable () -> Unit
) {
    if (shouldCollapsed()) content()
}
