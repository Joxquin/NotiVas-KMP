package me.joxquin.notivas.ui.components.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.ui.components.miuix.liquid.InnerShadow
import me.joxquin.notivas.ui.components.miuix.liquid.lens
import me.joxquin.notivas.ui.components.miuix.liquid.vibrancy
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.highlight.BloomStroke
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported

/**
 * Contenedor Liquid Glass (Glassy Capsule) canónico para botones de acción o navegación
 * en la barra superior (TopAppBar) de Xiaomi HyperOS / Compose Miuix UI.
 *
 * Utiliza los mismos shaders, distorsión de lente (lens), vibración (vibrancy), sombra interior (innerShadow)
 * y resaltado (highlight) que el FloatingNavigationBar.
 */
@Composable
fun MiuixGlassyActionsContainer(
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    shape: Shape = CircleShape,
    content: @Composable RowScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isBlurActive = isRuntimeShaderSupported() && backdrop != null

    Box(
        modifier = modifier
            .wrapContentWidth()
            .height(38.dp)
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 8.dp,
                    color = Color.Black,
                    alpha = if (isDark) 0.18f else 0.08f,
                ),
            )
            .then(
                if (isBlurActive && backdrop != null) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { shape },
                        effects = {
                            padding = maxOf(padding, 20.dp.toPx())
                            vibrancy()
                            blur(
                                3.dp.toPx(),
                                3.dp.toPx(),
                            )
                            lens(
                                10.dp.toPx(),
                                14.dp.toPx(),
                            )
                        },
                        highlight = {
                            Highlight(
                                width = 1.dp.toPx(),
                                color = Color.White,
                                alpha = if (isDark) 0.15f else 0.25f,
                            )
                        },
                        innerShadow = {
                            InnerShadow(
                                radius = 6.dp.toPx(),
                                color = Color.White,
                                alpha = if (isDark) 0.10f else 0.20f,
                            )
                        },
                        bloomStroke = {
                            BloomStroke(
                                strokeWidth = 0.8.dp.toPx(),
                                color = Color.White,
                                alpha = if (isDark) 0.20f else 0.35f,
                            )
                        },
                    )
                } else {
                    Modifier
                        .background(
                            color = if (isDark) Color(0x33282828) else Color(0x99F5F5F5),
                            shape = shape,
                        )
                        .border(
                            width = 0.5.dp,
                            color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f),
                            shape = shape,
                        )
                }
            )
            .clip(shape)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            content()
        }
    }
}
