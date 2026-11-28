package me.joxquin.notivas.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Pausa entre un elemento y el siguiente. */
private const val STAGGER_STEP_MS = 45L

/** Duración del fundido y el deslizamiento de cada elemento. */
private const val ENTRANCE_DURATION_MS = 320

/** Solo se escalonan los primeros elementos (los que se ven al abrir); el resto aparece sin demora. */
private const val MAX_STAGGERED = 10

/**
 * Estado de la entrada escalonada de una lista: los elementos aparecen uno tras otro con un fundido y un
 * leve deslizamiento hacia arriba, solo la primera vez que se muestra la lista.
 */
@Stable
class StaggeredEntrance internal constructor(alreadyPlayed: Boolean) {
    /** true cuando la entrada ya terminó (o se hizo antes): lo que se componga después no se anima. */
    internal var played by mutableStateOf(alreadyPlayed)

    /** true cuando la app ya se ve (después de la animación de arranque) y los elementos pueden entrar. */
    internal var started by mutableStateOf(alreadyPlayed)
}

/**
 * Crea el estado de una lista. Se guarda con rememberSaveable: al volver a la pantalla, cambiar de pestaña o
 * desplazar la lista no se repite. Espera a [LocalEntranceAnimationsReady] para no correr debajo de la
 * animación de arranque (como pasaba con el promedio de Inicio).
 */
@Composable
fun rememberStaggeredEntrance(): StaggeredEntrance {
    var playedSaved by rememberSaveable { mutableStateOf(false) }
    val entrance = remember { StaggeredEntrance(playedSaved) }
    val ready = LocalEntranceAnimationsReady.current
    LaunchedEffect(entrance, ready) {
        if (entrance.played || !ready) return@LaunchedEffect
        entrance.started = true
        delay(STAGGER_STEP_MS * MAX_STAGGERED + ENTRANCE_DURATION_MS)
        entrance.played = true
        playedSaved = true
    }
    return entrance
}

/**
 * Hace entrar el elemento [index] de la lista con su retraso. Se decide una sola vez al componer el elemento
 * si se anima, así la animación nunca se corta a la mitad; el progreso se lee al dibujar (graphicsLayer) y
 * no recompone la lista.
 */
@Composable
fun Modifier.staggeredEntrance(index: Int, entrance: StaggeredEntrance): Modifier {
    val animate = remember { !entrance.played && index < MAX_STAGGERED }
    val progress = remember { Animatable(if (animate) 0f else 1f) }
    if (animate) {
        val started = entrance.started
        LaunchedEffect(started) {
            if (!started) return@LaunchedEffect
            delay(STAGGER_STEP_MS * index)
            progress.animateTo(1f, tween(ENTRANCE_DURATION_MS, easing = FastOutSlowInEasing))
        }
    }
    val offsetPx = with(LocalDensity.current) { 16.dp.toPx() }
    return this.graphicsLayer {
        val p = progress.value
        alpha = p
        translationY = (1f - p) * offsetPx
    }
}
