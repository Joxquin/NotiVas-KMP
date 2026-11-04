package me.joxquin.notivas.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Dimension
import coil3.size.Precision
import coil3.size.Scale
import coil3.size.Size

/**
 * CourseCoverImage: Componente de alto rendimiento para renderizar portadas de cursos
 * (tanto imágenes estáticas JPG/PNG como GIFs animados).
 *
 * Aplica downsampling estricto en la decodificación de Coil para evitar cargar
 * imágenes de resolución original (1080p+) en memoria, reduciendo radicalmente
 * el consumo de RAM y GPU por cada cuadro del GIF o imagen estática.
 */
@Composable
fun CourseCoverImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    targetWidth: Dp = 240.dp,
    targetHeight: Dp = 110.dp,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: @Composable () -> Unit
) {
    if (imageUrl.isNullOrBlank()) {
        placeholder()
        return
    }

    val context = LocalPlatformContext.current
    val density = LocalDensity.current

    // Calculamos las dimensiones exactas en píxeles físicos para el downsampling de Coil
    val imageRequest = remember(imageUrl, targetWidth, targetHeight, density) {
        val widthPx = with(density) { targetWidth.roundToPx() }
        val heightPx = with(density) { targetHeight.roundToPx() }
        ImageRequest.Builder(context)
            .data(imageUrl)
            .size(Size(Dimension(widthPx), Dimension(heightPx)))
            .precision(Precision.INEXACT)
            .scale(Scale.FILL)
            .crossfade(true)
            .build()
    }

    Box(modifier = modifier) {
        // Placeholder visible inmediatamente como fondo hasta que termine de cargar
        placeholder()

        AsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize()
        )
    }
}
