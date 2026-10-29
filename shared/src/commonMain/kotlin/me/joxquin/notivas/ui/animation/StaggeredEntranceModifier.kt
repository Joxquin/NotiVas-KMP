package me.joxquin.notivas.ui.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modificador para animar la entrada escalonada en cascada de elementos de la interfaz.
 * Se ejecuta 100% sobre capas de GPU para evitar relayouts o pérdidas de fluidez.
 *
 * @param index Posición ordinal del elemento en la cascada visual (0 = primero arriba).
 * @param trigger Condición que activa el inicio de la animación de entrada.
 * @param baseDelayMs Retardo base previo al inicio del primer elemento.
 * @param stepDelayMs Retardo incremental entre cada elemento adyacente.
 * @param durationMs Duración de la animación de cada elemento individual.
 * @param initialOffsetY Desplazamiento inicial superior desde donde desliza hacia abajo.
 */
@Composable
fun Modifier.staggeredEntrance(
    index: Int,
    trigger: Boolean,
    baseDelayMs: Int = 40,
    stepDelayMs: Int = 60,
    durationMs: Int = 380,
    initialOffsetY: Dp = (-26).dp
): Modifier {
    val density = LocalDensity.current
    val progress by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = tween(
            durationMillis = durationMs,
            delayMillis = baseDelayMs + (index * stepDelayMs),
            easing = FastOutSlowInEasing
        ),
        label = "staggered_entrance_$index"
    )

    if (progress >= 1f) {
        return this
    }

    val offsetPx = with(density) { initialOffsetY.toPx() }
    return this.graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * offsetPx
    }
}

