package me.joxquin.notivas.ui.components.miuix

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Alarm
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur

object MiuixTopAppBarDefaults {
    val HeaderHeight = 64.dp
}

/**
 * Botón de acción circular estándar para la barra superior de HyperOS Miuix.
 * Mantiene la consistencia visual de fondo circular, borde y tinte idéntico
 * al icono de notificaciones/reloj del Dashboard.
 */
@Composable
fun MiuixTopAppBarAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(MiuixTheme.colorScheme.surfaceVariant)
            .border(0.5.dp, MiuixTheme.colorScheme.dividerLine, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            content()
        }
    }
}

/**
 * TopAppBar Canónica HyperOS Miuix con Progressive Blur:
 * - Edge-to-Edge: La barra cubre toda el área hasta el tope de la pantalla (incluyendo status bar),
 *   y solo los controles y título aplican el padding de status bar.
 * - Reposo (Y = 0): 100% transparente.
 * - Desplazamiento (Y > 0): Activa suavemente el shader progressiveBlur(ProgressiveBlur.Top)
 *   modulando su opacidad mediante clamp(deltaY / threshold, 0.0, 1.0).
 * - Transición y Alineación de Título: Animación fluida de fade-in + slide-up centrado.
 */
@Composable
fun MiuixTopAppBar(
    title: String,
    isScrolled: Boolean,
    scrollOffset: Float = 0f,
    backdrop: Backdrop? = null,
    isBlurActive: Boolean = true,
    titleAlwaysVisible: Boolean = false,
    onNavigationClick: (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Opacidad de blur progresivo clamp(deltaY / threshold, 0.0, 1.0)
    val blurAlpha = (scrollOffset / 64f).coerceIn(0f, 1f)
    val borderColor = MiuixTheme.colorScheme.dividerLine.copy(alpha = blurAlpha)
    val fallbackBg = MiuixTheme.colorScheme.surface.copy(alpha = 0.85f * blurAlpha)

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Capa 1: Progressive Blur Shader Reactivo al Desplazamiento (Cubre de borde a borde, incluyendo status bar)
        if (isBlurActive && backdrop != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = blurAlpha }
                    .progressiveTextureBlur(
                        backdrop = backdrop,
                        shape = RectangleShape,
                        gradient = ProgressiveBlur.Top.copy(curve = 2.2f),
                        blurRadius = 16f,
                        colors = BlurDefaults.blurColors(
                            blendColors = listOf(
                                BlendColorEntry(color = MiuixTheme.colorScheme.surface.copy(alpha = 0.45f))
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(fallbackBg)
            )
        }

        // Capa 2: Contenido Estructurado de la Barra Superior (Padding de status bar aplicado aquí para los iconos)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MiuixTopAppBarDefaults.HeaderHeight)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Controles de Acción Izquierda
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (navigationIcon != null) {
                        navigationIcon()
                    } else if (onNavigationClick != null) {
                        MiuixTopAppBarAction(onClick = onNavigationClick) {
                            Icon(
                                imageVector = MiuixIcons.Alarm,
                                contentDescription = "Notificaciones",
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Título Compacto Centrado (Simetría HyperOS)
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = titleAlwaysVisible || isScrolled,
                        enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) +
                                slideInVertically(animationSpec = androidx.compose.animation.core.tween(280)) { it / 2 },
                        exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) +
                                slideOutVertically(animationSpec = androidx.compose.animation.core.tween(200)) { it / 2 }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = title,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.3).sp,
                                    color = MiuixTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }

                // Controles de Acción Derecha
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actions()
                }
            }
        }
    }
}