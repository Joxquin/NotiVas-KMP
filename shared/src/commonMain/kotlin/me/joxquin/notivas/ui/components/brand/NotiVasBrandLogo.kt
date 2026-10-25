package me.joxquin.notivas.ui.components.brand

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NotiVasBrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 108.dp,
    elevation: Dp = 8.dp,
    transparentBackground: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (!transparentBackground) {
                    Modifier
                        .shadow(
                            elevation = elevation,
                            shape = CircleShape,
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // 0. Mitad inferior en tinte suave dual-tone si no es transparente
            if (!transparentBackground) {
                drawRect(
                    color = Color(0xFFF6F3EE),
                    topLeft = Offset(0f, h * 0.5f),
                    size = androidx.compose.ui.geometry.Size(w, h * 0.5f)
                )
            }

            // Factor de escala relativo del vector original
            fun sx(x: Float) = (x / 140f) * w
            fun sy(y: Float) = (y / 140f) * h

            val capBaseColor = if (transparentBackground && isDark) Color(0xFF3D3028) else Color(0xFF150B00)
            val diamondColor = if (transparentBackground && isDark) Color(0xFFF7ECE4) else Color(0xFF2A1700)
            val facetColor = if (transparentBackground && isDark) Color(0xFFE2D4C8) else Color(0xFF534434).copy(alpha = 0.32f)
            val cordColor = if (transparentBackground && isDark) Color(0xFF769352) else Color(0xFF4F6532)
            val tasselColor = if (transparentBackground && isDark) Color(0xFF5A743B) else Color(0xFF3A4D23)
            val ringColor = if (transparentBackground && isDark) Color(0xFFFFB74D) else Color(0xFFF59E0B)

            // 1. Casquete / Base Inferior del Birrete
            val capBasePath = Path().apply {
                moveTo(sx(28f), sy(70f))
                lineTo(sx(28f), sy(96f))
                cubicTo(sx(28f), sy(116f), sx(47f), sy(127f), sx(70f), sy(127f))
                cubicTo(sx(93f), sy(127f), sx(112f), sy(116f), sx(112f), sy(96f))
                lineTo(sx(112f), sy(70f))
                lineTo(sx(70f), sy(88f))
                close()
            }
            drawPath(path = capBasePath, color = capBaseColor)

            // 2. Rombo Superior del Birrete
            val diamondPath = Path().apply {
                moveTo(sx(70f), sy(24f))
                lineTo(sx(136f), sy(54f))
                lineTo(sx(70f), sy(84f))
                lineTo(sx(4f), sy(54f))
                close()
            }
            drawPath(path = diamondPath, color = diamondColor)

            // 3. Faceta Especular
            val facetPath = Path().apply {
                moveTo(sx(70f), sy(28f))
                lineTo(sx(128f), sy(54f))
                lineTo(sx(70f), sy(78f))
                lineTo(sx(12f), sy(54f))
                close()
            }
            drawPath(path = facetPath, color = facetColor)

            // 4. Cuerda Drapeada de la Borla
            val cordPath = Path().apply {
                moveTo(sx(70f), sy(54f))
                cubicTo(sx(54f), sy(54f), sx(38f), sy(65f), sx(32f), sy(78f))
                lineTo(sx(28f), sy(98f))
            }
            drawPath(
                path = cordPath,
                color = cordColor,
                style = Stroke(width = sx(4.5f), cap = StrokeCap.Round)
            )

            // 5. Boton Pivote Central de la Borla
            drawCircle(
                color = cordColor,
                radius = sx(6f),
                center = Offset(sx(70f), sy(54f))
            )

            // 6. Flecos de Graduacion
            val tasselPath = Path().apply {
                moveTo(sx(28f), sy(98f))
                lineTo(sx(23f), sy(120f))
                cubicTo(sx(24f), sy(123f), sx(31f), sy(123f), sx(33f), sy(120f))
                lineTo(sx(31f), sy(98f))
                close()
            }
            drawPath(path = tasselPath, color = tasselColor)

            // 7. Anillo Tonal de la Borla
            drawRect(
                color = ringColor,
                topLeft = Offset(sx(24f), sy(101f)),
                size = androidx.compose.ui.geometry.Size(sx(8f), sy(4f))
            )
        }
    }
}

/**
 * Logo vectorial birrete NotiVas en estilo Outline puro con colores Monet dinámicos (sin fondo blanco).
 */
@Composable
fun NotiVasOutlineLogo(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    accentTint: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            fun sx(x: Float) = (x / 140f) * w
            fun sy(y: Float) = (y / 140f) * h

            val strokeWidth = sx(4.2f)

            // 1. Casquete / Base Inferior del Birrete (Trazo Outline)
            val capBasePath = Path().apply {
                moveTo(sx(28f), sy(70f))
                lineTo(sx(28f), sy(96f))
                cubicTo(sx(28f), sy(116f), sx(47f), sy(127f), sx(70f), sy(127f))
                cubicTo(sx(93f), sy(127f), sx(112f), sy(116f), sx(112f), sy(96f))
                lineTo(sx(112f), sy(70f))
            }
            drawPath(
                path = capBasePath,
                color = tint,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Rombo Superior del Birrete (Trazo Outline)
            val diamondPath = Path().apply {
                moveTo(sx(70f), sy(24f))
                lineTo(sx(136f), sy(54f))
                lineTo(sx(70f), sy(84f))
                lineTo(sx(4f), sy(54f))
                close()
            }
            drawPath(
                path = diamondPath,
                color = tint,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 3. Faceta o lomo interior sutil
            val ridgePath = Path().apply {
                moveTo(sx(70f), sy(24f))
                lineTo(sx(70f), sy(84f))
            }
            drawPath(
                path = ridgePath,
                color = accentTint,
                style = Stroke(width = strokeWidth * 0.7f, cap = StrokeCap.Round)
            )

            // 4. Cuerda Drapeada de la Borla
            val cordPath = Path().apply {
                moveTo(sx(70f), sy(54f))
                cubicTo(sx(54f), sy(54f), sx(38f), sy(65f), sx(32f), sy(78f))
                lineTo(sx(28f), sy(98f))
            }
            drawPath(
                path = cordPath,
                color = tint,
                style = Stroke(width = strokeWidth * 0.9f, cap = StrokeCap.Round)
            )

            // 5. Boton Pivote Central de la Borla
            drawCircle(
                color = tint,
                radius = sx(4.5f),
                center = Offset(sx(70f), sy(54f))
            )

            // 6. Flecos de Graduacion
            val tasselPath = Path().apply {
                moveTo(sx(28f), sy(98f))
                lineTo(sx(23f), sy(118f))
                cubicTo(sx(24f), sy(121f), sx(31f), sy(121f), sx(33f), sy(118f))
                lineTo(sx(31f), sy(98f))
                close()
            }
            drawPath(
                path = tasselPath,
                color = tint,
                style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round)
            )
        }
    }
}
