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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.ui.components.brand.NotiVasBrandLogo
import me.joxquin.notivas.ui.components.brand.NotiVasOutlineLogo
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class SplashPhase {
    HOLD,
    SCALE_DOWN,
    BREATHING,
    EXIT
}

/**
 * Pantalla de inicio animada (Splash post-setup) de NotiVas.
 * Proporciona continuidad visual con el splash nativo del sistema,
 * redimensiona hacia el outline suspirante durante la precarga de datos,
 * y se desvanece suavemente para revelar el Dashboard.
 */
@Composable
fun AppStartupSplash(
    themeStyle: AppThemeStyle,
    isDataReady: Boolean,
    onSplashFinished: () -> Unit,
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

    // 189.dp reproduce exactamente los 356px del splash nativo de Android
    val startLogoSize = 189.dp
    val targetLogoSize = 72.dp
    val scaleTarget = targetLogoSize / startLogoSize

    // Anclaje visual del birrete en 75.5/140 idéntico al Onboarding
    val anchorYFraction = 75.5f / 140f
    val logoVerticalOffset = -(startLogoSize * (anchorYFraction - 0.5f))

    var phase by remember { mutableStateOf(SplashPhase.HOLD) }

    val morphProgress = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }
    val exitScale = remember { Animatable(1f) }

    // Pulso orgánico ("outline suspirante")
    val infiniteTransition = rememberInfiniteTransition(label = "breathing_pulse")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )
    val breathingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_alpha"
    )

    // Orquestación de fases temporales
    LaunchedEffect(Unit) {
        // Fase 1: Pausa de continuidad idéntica al splash nativo
        delay(200)
        phase = SplashPhase.SCALE_DOWN

        // Fase 2: Redimensión suave y morphing a outline
        morphProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
        )
        phase = SplashPhase.BREATHING
    }

    // Disparador de salida al completar precarga (con cortesía mínima de apreciación)
    LaunchedEffect(isDataReady, phase) {
        if (phase == SplashPhase.BREATHING && isDataReady) {
            // Mínima pausa para que se aprecie la respiración
            delay(280)
            phase = SplashPhase.EXIT

            // Fase 4: Desvanecimiento con ligera expansión liberadora
            exitScale.animateTo(
                targetValue = 1.08f,
                animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)
            )
        }
    }

    LaunchedEffect(phase) {
        if (phase == SplashPhase.EXIT) {
            exitAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 340, easing = LinearEasing)
            )
            onSplashFinished()
        }
    }

    // Timeout de seguridad de 2400ms para nunca bloquear al usuario en redes lentas
    LaunchedEffect(Unit) {
        delay(2400)
        if (phase != SplashPhase.EXIT) {
            phase = SplashPhase.EXIT
        }
    }

    val currentMorph = morphProgress.value
    val baseScale = 1f + (scaleTarget - 1f) * currentMorph
    val effectiveScale = if (phase == SplashPhase.BREATHING) {
        baseScale * breathingScale
    } else {
        baseScale * exitScale.value
    }

    val filledAlpha = (1f - currentMorph).coerceIn(0f, 1f)
    val outlineAlpha = if (phase == SplashPhase.BREATHING) {
        breathingAlpha * currentMorph
    } else {
        currentMorph
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = exitAlpha.value }
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset(y = logoVerticalOffset)
                .size(startLogoSize)
                .graphicsLayer {
                    scaleX = effectiveScale
                    scaleY = effectiveScale
                    transformOrigin = TransformOrigin(0.5f, anchorYFraction)
                },
            contentAlignment = Alignment.Center
        ) {
            // 1. Logo relleno (idéntico al splash nativo)
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

            // 2. Logo outline Monet suspirante
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

