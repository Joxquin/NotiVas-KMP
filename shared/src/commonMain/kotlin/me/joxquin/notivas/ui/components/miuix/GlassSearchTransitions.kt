// Copyright 2026, compose-miuix-ui contributors / NotiVas contributors
// SPDX-License-Identifier: Apache-2.0

package me.joxquin.notivas.ui.components.miuix

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.material.GlassDefaults
import top.yukonga.miuix.kmp.material.GlassShadows
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

private const val SEARCH_TOP_BAR_BLUR_RADIUS = 50f

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
fun rememberGlassSearchBarAlpha(expanded: Boolean): State<Float> {
    val transition = updateTransition(expanded, label = "glassSearchTopBarOpacity")
    return transition.animateFloat(
        transitionSpec = {
            tween(150, easing = if (targetState) CubicBezierEasing(0f, 0f, 0.67f, 1f) else CubicBezierEasing(0.26f, 0f, 0.6f, 0.2f))
        },
        label = "glassSearchTopBarAlpha",
    ) { if (it) 0f else 1f }
}

/**
 * Transición HyperOS canónica para la barra superior (TopAppBar) al entrar o salir del modo de búsqueda.
 * El TopAppBar se desvanece (alpha), reduce su escala a 0.9 y aplica un desenfoque progresivo de 50f con SearchModeEasing.
 */
@Composable
fun Modifier.glassSearchBarTransition(expanded: Boolean, barAlpha: () -> Float): Modifier {
    val density = LocalDensity.current
    val layerOutset = remember(density) {
        val shadow = GlassShadows.Regular
        val shadowOutset = ((shadow.radius + maxOf(abs(shadow.offsetX), abs(shadow.offsetY))) / GlassDefaults.SourceDensity).dp
        // Mantiene un halo fijo para las sombras y el desenfoque máximo durante el fade offscreen
        with(density) { shadowOutset.toPx() } + SEARCH_TOP_BAR_BLUR_RADIUS * 3f
    }
    val transition = updateTransition(expanded, label = "glassSearchTopBar")
    val scale = transition.animateFloat(
        transitionSpec = { tween(400, easing = SearchModeEasing) },
        label = "glassSearchTopBarScale",
    ) { if (it) 0.9f else 1f }
    val blur = transition.animateFloat(
        transitionSpec = { tween(if (targetState) 150 else 120, easing = LinearEasing) },
        label = "glassSearchTopBarBlur",
    ) { if (it) SEARCH_TOP_BAR_BLUR_RADIUS else 0f }
    return drawWithCache {
        val layer = obtainGraphicsLayer()
        val halo = ceil(layerOutset).toInt()
        val layerSize = IntSize(size.width.toInt() + halo * 2, size.height.toInt() + halo * 2)
        onDrawWithContent {
            val opacity = barAlpha().coerceIn(0f, 1f)
            val currentScale = scale.value
            val currentBlur = blur.value
            if (opacity > 0f) {
                if (opacity == 1f && currentScale == 1f && currentBlur == 0f) {
                    drawContent()
                } else {
                    layer.record(density = this@drawWithCache, size = layerSize) {
                        translate(halo.toFloat(), halo.toFloat()) { this@record.drawContent() }
                    }
                    layer.alpha = opacity
                    layer.scaleX = currentScale
                    layer.scaleY = currentScale
                    layer.renderEffect = if (currentBlur > 0.5f) BlurEffect(currentBlur, currentBlur, TileMode.Decal) else null
                    translate(-halo.toFloat(), -halo.toFloat()) { drawLayer(layer) }
                }
            }
        }
    }
}

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
