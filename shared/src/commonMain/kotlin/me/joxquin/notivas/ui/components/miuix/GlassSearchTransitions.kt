// Copyright 2026, compose-miuix-ui contributors / NotiVas contributors
// SPDX-License-Identifier: Apache-2.0

package me.joxquin.notivas.ui.components.miuix

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.glass.glassSearchBarTransition as miuixGlassSearchBarTransition
import top.yukonga.miuix.kmp.glass.rememberGlassSearchBarAlpha as miuixRememberGlassSearchBarAlpha
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Curva canónica de resorte de HyperOS para el modo de búsqueda mapeada a 400ms.
 */
val SearchModeEasing: Easing = run {
    val omega = 2.0 * PI / 0.75f
    val stiffness = (omega * omega).toFloat()
    val damping = (0.98f * 2.0 * omega).toFloat()
    val decay = -damping / 2f
    val frequency = sqrt(-(damping.toDouble() * damping - stiffness.toDouble() * 4)).toFloat() / 2f
    val sineCoefficient = decay / frequency
    Easing { fraction ->
        (
            1.0 + exp((decay * fraction).toDouble()) *
                (-cos((frequency * fraction).toDouble()) + sineCoefficient * sin((frequency * fraction).toDouble()))
            ).toFloat()
    }
}

/**
 * Controla la opacidad del TopAppBar durante la entrada y salida del modo de búsqueda.
 */
@Composable
fun rememberGlassSearchBarAlpha(expanded: Boolean): State<Float> =
    miuixRememberGlassSearchBarAlpha(expanded)

/**
 * Transición HyperOS canónica para la barra superior (TopAppBar) al entrar o salir del modo de búsqueda.
 */
@Composable
fun Modifier.glassSearchBarTransition(
    expanded: Boolean,
    blurEnabled: Boolean = true,
    barAlpha: () -> Float,
): Modifier = miuixGlassSearchBarTransition(
    expanded = expanded,
    blurEnabled = blurEnabled,
    barAlpha = barAlpha,
)

/**
 * Controla el desvanecimiento y reaparición con rebote del título de la barra al salir del modo búsqueda.
 */
@Composable
fun rememberGlassSearchTitleAlpha(expanded: Boolean, collapsed: Boolean): () -> Float {
    val transition = updateTransition(expanded, label = "glassSearchTitlePhase")
    val progress = transition.animateFloat(
        transitionSpec = { tween(400, easing = SearchModeEasing) },
        label = "glassSearchTitleExitProgress",
    ) { if (it) 0f else 1f }
    val reveal by remember(expanded, progress) { derivedStateOf { expanded || progress.value > 0.8f } }
    val response = if (collapsed) 0.15f else 0.6f
    val titleAlpha = animateFloatAsState(
        targetValue = if (reveal) 1f else 0f,
        animationSpec = if (reveal && !expanded) {
            spring(dampingRatio = 1f, stiffness = (2 * PI / response).let { (it * it).toFloat() }, visibilityThreshold = 0.001f)
        } else {
            snap()
        },
        label = "glassSearchTitleAlpha",
    )
    return remember(titleAlpha) { { titleAlpha.value } }
}
