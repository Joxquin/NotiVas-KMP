// Copyright 2026, compose-miuix-ui contributors / NotiVas contributors
// SPDX-License-Identifier: Apache-2.0

package me.joxquin.notivas.ui.components.miuix

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Capa de ventana completa dibujada por encima del chrome de la app (barra flotante, rail, NavDisplay).
 * Permite elevar overlays como GlassSearchOverlay o popups inline sin que queden atrapados
 * en el HorizontalPager o debajo de barras persistentes.
 */
@Stable
class TopLayerState {
    internal val entries = mutableStateListOf<TopLayerEntry>()
}

@Stable
internal class TopLayerEntry {
    var content: @Composable BoxScope.() -> Unit by mutableStateOf({})
}

val LocalTopLayer = staticCompositionLocalOf<TopLayerState?> { null }

/** Dibuja [content] en el [TopLayerHost] más cercano, o en su lugar si no hay ninguno. */
@Composable
fun TopLayer(content: @Composable BoxScope.() -> Unit) {
    val layer = LocalTopLayer.current
    if (layer == null) {
        Box(modifier = Modifier.fillMaxSize(), content = content)
        return
    }
    val entry = remember { TopLayerEntry() }
    SideEffect { entry.content = content }
    DisposableEffect(layer, entry) {
        layer.entries += entry
        onDispose { layer.entries -= entry }
    }
}

/** Aloja el contenido elevado por [TopLayer]. Colocar después del chrome sobre el que debe quedar. */
@Composable
fun TopLayerHost(
    state: TopLayerState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        state.entries.forEach { entry ->
            key(entry) { entry.content(this) }
        }
    }
}
