package me.joxquin.notivas.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.ui.components.brand.NotiVasBrandLogo
import me.joxquin.notivas.ui.components.brand.NotiVasOutlineLogo
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

val LocalSplashLogoTarget = compositionLocalOf<((Rect) -> Unit)?> { null }

private enum class SplashPhase {
    HOLD,
    SCALE_DOWN,
    BREATHING,
    FLY,
    FINISHED
}

/**
 * Pantalla de inicio animada (Splash post-setup) de NotiVas.
 * Proporciona continuidad visual absoluta con el splash nativo del sistema mediante
 * anclaje visual en transformOrigin, escala sin saltos hacia el outline Monet suspirante,
 * y vuela con precisión matemática hacia la posición real del TopAppBar.
 */
@Composable
fun AppStartupSplash(
    themeStyle: AppThemeStyle,
    isDataReady: Boolean,
    onSplashFinished: () -> Unit,
    targetLogoBounds: Rect? = null,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val backgroundColor = if (isMiuix) {
        MiuixTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surface
    }
    val primaryColor = if (isMiuix) {
        MiuixTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary
    }

    var phase by remember { mutableStateOf(SplashPhase.HOLD) }

    val morphProgress = remember { Animatable(0f) }
    val flyProgress = remember { Animatable(0f) }
    val backgroundAlpha = remember { Animatable(1f) }

    // Pulso orgánico ("outline suspirante") activo durante la fase de espera
    val infiniteTransition = rememberInfiniteTransition(label = "breathing_pulse")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )
    val breathingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.80f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_alpha"
    )

    // Valores congelados de respiración al iniciar el vuelo para evitar oscilaciones
    var frozenBreathScale by remember { mutableStateOf(1f) }
    var frozenBreathAlpha by remember { mutableStateOf(1f) }

    // Destino congelado fijado al iniciar el vuelo
    var lockedTargetX by remember { mutableStateOf<Float?>(null) }
    var lockedTargetY by remember { mutableStateOf<Float?>(null) }
    var lockedTargetScale by remember { mutableStateOf<Float?>(null) }

    // Orquestación secuencial de fases
    LaunchedEffect(Unit) {
        // Pausa breve de continuidad perfecta con el splash nativo
        delay(100)
        phase = SplashPhase.SCALE_DOWN

        // Redimensión suave y morphing continuo hacia outline Monet
        morphProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 460, easing = FastOutSlowInEasing)
        )
        phase = SplashPhase.BREATHING
    }

    // Disparador de vuelo inmediato al completar precarga sin retraso artificial
    LaunchedEffect(isDataReady, phase) {
        if (phase == SplashPhase.BREATHING && isDataReady) {
            phase = SplashPhase.FLY
        }
    }

    // Ejecución de la animación de vuelo y desvanecimiento del fondo
    LaunchedEffect(phase) {
        if (phase == SplashPhase.FLY) {
            frozenBreathScale = breathingScale
            frozenBreathAlpha = breathingAlpha

            launch {
                delay(40)
                backgroundAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 440, easing = LinearEasing)
                )
            }
            flyProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
            phase = SplashPhase.FINISHED
            onSplashFinished()
        }
    }

    // Timeout de seguridad para evitar bloqueos en redes lentas
    LaunchedEffect(Unit) {
        delay(2200)
        if (phase == SplashPhase.BREATHING || phase == SplashPhase.SCALE_DOWN || phase == SplashPhase.HOLD) {
            phase = SplashPhase.FLY
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor.copy(alpha = backgroundAlpha.value)),
        contentAlignment = Alignment.TopStart
    ) {
        val currentMorph = morphProgress.value
        val density = LocalDensity.current
        val totalWidthPx = constraints.maxWidth.toFloat()
        val totalHeightPx = constraints.maxHeight.toFloat()
        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val statusBarTopPx = with(density) { statusBarTop.toPx() }

        // 189.dp reproduce exactamente los 356px del splash nativo de Android
        val startLogoSize = 189.dp
        val startLogoSizePx = with(density) { startLogoSize.toPx() }
        val anchorYFraction = 75.5f / 140f

        val breathingLogoSize = 68.dp
        val breathingScaleTarget = with(density) { breathingLogoSize.toPx() } / startLogoSizePx

        val targetLogoSize = 28.dp
        val targetLogoSizePx = with(density) { targetLogoSize.toPx() }
        val finalScaleTarget = targetLogoSizePx / startLogoSizePx

        // Fallbacks deterministas en caso de que la medición aún no haya emitido
        val fallbackTargetX = with(density) { (if (isMiuix) 28.dp else 36.dp).toPx() }
        val fallbackTargetY = statusBarTopPx + with(density) { (if (isMiuix) 44.dp else 32.dp).toPx() }

        // Destino medido exactamente en ventana a partir del TopAppBar
        val measuredTargetX = targetLogoBounds?.center?.x ?: fallbackTargetX
        val measuredTargetY = targetLogoBounds?.let { bounds ->
            bounds.center.y + targetLogoSizePx * (anchorYFraction - 0.5f)
        } ?: fallbackTargetY

        if (phase == SplashPhase.FLY && lockedTargetX == null) {
            lockedTargetX = measuredTargetX
            lockedTargetY = measuredTargetY
            lockedTargetScale = finalScaleTarget
        }

        val targetX = lockedTargetX ?: measuredTargetX
        val targetY = lockedTargetY ?: measuredTargetY
        val targetScale = lockedTargetScale ?: finalScaleTarget

        // Coordenadas de inicio: el anclaje del birrete en el centro exacto de la pantalla
        val startX = totalWidthPx * 0.5f
        val startY = totalHeightPx * 0.5f

        val currentFly = flyProgress.value

        // Interpolación continua de las coordenadas de anclaje
        val currentAnchorX = startX + (targetX - startX) * currentFly
        val currentAnchorY = startY + (targetY - startY) * currentFly

        // Escala base:
        // Antes del vuelo: reduce de 1.0f a breathingScaleTarget (0.36f)
        // Durante el vuelo: escala de breathingScaleTarget a targetScale (0.148f)
        val currentBaseScale = if (currentFly <= 0f) {
            1f + (breathingScaleTarget - 1f) * currentMorph
        } else {
            breathingScaleTarget + (targetScale - breathingScaleTarget) * currentFly
        }

        // Amortiguación suave del pulso respirante para congelar vibraciones en vuelo
        val breathScale = if (phase == SplashPhase.BREATHING) {
            breathingScale
        } else if (phase == SplashPhase.FLY) {
            val w = (1f - currentFly * 3f).coerceIn(0f, 1f)
            1f + (frozenBreathScale - 1f) * w
        } else {
            1f
        }
        val effectiveScale = currentBaseScale * breathScale

        val breathAlpha = if (phase == SplashPhase.BREATHING) {
            breathingAlpha
        } else if (phase == SplashPhase.FLY) {
            val w = (1f - currentFly * 3f).coerceIn(0f, 1f)
            1f + (frozenBreathAlpha - 1f) * w
        } else {
            1f
        }

        // Crossfade de capas
        val filledAlpha = (1f - currentMorph).coerceIn(0f, 1f)
        val outlineAlpha = if (phase == SplashPhase.HOLD) {
            0f
        } else if (phase == SplashPhase.SCALE_DOWN) {
            currentMorph
        } else {
            breathAlpha.coerceIn(0f, 1f)
        }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (currentAnchorX - startLogoSizePx * 0.5f).roundToInt(),
                        y = (currentAnchorY - startLogoSizePx * anchorYFraction).roundToInt()
                    )
                }
                .size(startLogoSize)
                .graphicsLayer {
                    scaleX = effectiveScale
                    scaleY = effectiveScale
                    transformOrigin = TransformOrigin(0.5f, anchorYFraction)
                },
            contentAlignment = Alignment.Center
        ) {
            // 1. Logo relleno (continuidad limpia con splash nativo del sistema)
            if (filledAlpha > 0.01f) {
                NotiVasBrandLogo(
                    size = startLogoSize,
                    transparentBackground = true,
                    elevation = 0.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = filledAlpha }
                )
            }

            // 2. Logo outline Monet
            if (outlineAlpha > 0.01f) {
                NotiVasOutlineLogo(
                    size = startLogoSize,
                    tint = primaryColor,
                    accentTint = primaryColor.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = outlineAlpha }
                )
            }
        }
    }
}
