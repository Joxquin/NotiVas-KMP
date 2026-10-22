package me.joxquin.notivas.ui.components.miuix

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * MiuixAutoSettleScrollConnection:
 * Conexión de scroll anidado que replica el comportamiento de settling de MiuixScrollBehavior.
 * Al soltar el gesto de scroll (onPostFling), si la lista se encuentra dentro de la zona de
 * transición del encabezado (index 0, offset menor al umbral), auto-desliza la lista con
 * animación fluida para que el título quede 100% en la vista o 100% en el TopAppBar,
 * eliminando cualquier estado intermedio atascado.
 */
class MiuixAutoSettleScrollConnection(
    private val listState: LazyListState,
    private val thresholdPx: Float,
    private val coroutineScope: CoroutineScope
) : NestedScrollConnection {

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (listState.firstVisibleItemIndex == 0) {
            val offset = listState.firstVisibleItemScrollOffset.toFloat()
            if (offset > 0f && offset < thresholdPx) {
                coroutineScope.launch {
                    if (offset < thresholdPx * 0.45f) {
                        // Deslizar automáticamente hacia 100% en la vista (expandido)
                        listState.animateScrollToItem(0, 0)
                    } else {
                        // Deslizar automáticamente hacia 100% en el TopAppBar (colapsado)
                        listState.animateScrollToItem(0, thresholdPx.toInt())
                    }
                }
            }
        }
        return super.onPostFling(consumed, available)
    }
}

@Composable
fun rememberMiuixAutoSettleScrollConnection(
    listState: LazyListState,
    thresholdDp: Dp = 72.dp
): NestedScrollConnection {
    val density = LocalDensity.current
    val thresholdPx = with(density) { thresholdDp.toPx() }
    val coroutineScope = rememberCoroutineScope()
    return remember(listState, thresholdPx) {
        MiuixAutoSettleScrollConnection(listState, thresholdPx, coroutineScope)
    }
}
