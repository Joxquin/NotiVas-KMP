package me.joxquin.notivas.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import me.joxquin.notivas.domain.grades.GradeInsights

/**
 * Valor que sube animado hasta [target] cuando aparece (promedios de Inicio, Rendimiento, el curso y Notas).
 *
 * - La primera vez cuenta desde 0; si el valor cambia después (por ejemplo al sincronizar), anima desde el anterior.
 * - El último valor mostrado se guarda con rememberSaveable: al volver a la pantalla, cambiar de pestaña o
 *   desplazar una lista no se repite la animación.
 * - Al terminar devuelve exactamente [target], así el texto final es idéntico al formato de siempre.
 * - Con las animaciones del sistema desactivadas, Compose salta directo al valor final.
 */
@Composable
fun animatedCountUp(target: Double, durationMillis: Int = 900): Double {
    var lastShown by rememberSaveable { mutableStateOf<Double?>(null) }
    val animatable = remember { Animatable((lastShown ?: 0.0).toFloat()) }
    var settled by remember { mutableStateOf(lastShown == target) }
    val ready = LocalEntranceAnimationsReady.current

    LaunchedEffect(target, ready) {
        if (lastShown == target) {
            settled = true
            return@LaunchedEffect
        }
        settled = false
        // Detrás de la animación de arranque el conteo se veía terminado: espera en 0 hasta que la app se vea.
        if (!ready) return@LaunchedEffect
        animatable.animateTo(target.toFloat(), tween(durationMillis, easing = FastOutSlowInEasing))
        lastShown = target
        settled = true
    }
    return if (settled) target else animatable.value.toDouble()
}

/**
 * false mientras la animación de arranque (el birrete que vuela a la barra) tapa la app. En el Redmi el
 * promedio de Inicio terminaba de subir debajo de esa animación y aparecía ya fijo. Lo usan los números
 * que suben y las listas escalonadas para empezar cuando la app ya se ve. App.kt lo provee con
 * hasStartupSplashRun; fuera de ese caso vale true.
 */
val LocalEntranceAnimationsReady = compositionLocalOf { true }

/** Un decimal truncado, el mismo formato de los promedios en toda la app (16.78 -> "16.7"); ver GradeInsights.shown1. */
fun formatAverageOneDecimal(value: Double): String = GradeInsights.shown1(value).toString()
