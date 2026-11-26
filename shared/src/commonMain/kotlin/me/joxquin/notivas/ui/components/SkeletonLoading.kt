package me.joxquin.notivas.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.content_loading
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/**
 * Brillo compartido por todos los bloques de un esqueleto de carga, para que avance sincronizado.
 * El progreso se lee al dibujar, así que la animación solo redibuja los bloques y no recompone la pantalla.
 */
@Stable
class SkeletonShimmer internal constructor(
    internal val base: Color,
    internal val highlight: Color,
    internal val progress: State<Float>
)

/** Colores del tema (sin hex): la base de las píldoras y el brillo del contenedor destacado. */
@Composable
fun rememberSkeletonShimmer(themeStyle: AppThemeStyle): SkeletonShimmer {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val base = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHigh
    val highlight = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerHighest
    val progress = rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1100, easing = LinearEasing), RepeatMode.Restart),
        label = "skeletonProgress"
    )
    return remember(base, highlight, progress) { SkeletonShimmer(base, highlight, progress) }
}

/** Pinta el elemento como un bloque de carga con la forma del contenido que reemplaza. */
fun Modifier.skeleton(shimmer: SkeletonShimmer, shape: Shape = RoundedCornerShape(8.dp)): Modifier =
    clip(shape).drawBehind {
        drawRect(shimmer.base)
        val band = size.width * 0.6f
        val startX = -band + (size.width + band) * shimmer.progress.value
        drawRect(
            brush = Brush.horizontalGradient(
                0f to shimmer.base,
                0.5f to shimmer.highlight,
                1f to shimmer.base,
                startX = startX,
                endX = startX + band
            )
        )
    }

/** Tarjeta del esqueleto con el mismo fondo, forma y borde que las tarjetas reales de cada diseño. */
@Composable
private fun SkeletonCard(
    themeStyle: AppThemeStyle,
    cornerRadius: Dp,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    bordered: Boolean = true,
    content: @Composable () -> Unit
) {
    if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixCard(
            modifier = modifier.fillMaxWidth(),
            colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
            insideMargin = contentPadding,
            pressFeedbackType = PressFeedbackType.None
        ) {
            content()
        }
    } else {
        val shape = RoundedCornerShape(cornerRadius)
        val borderModifier = if (bordered) {
            Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape)
        } else {
            Modifier
        }
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .then(borderModifier)
                .padding(contentPadding)
        ) {
            content()
        }
    }
}

/**
 * Esqueleto del detalle de curso: la portada con sus 3 métricas y la tarjeta de ruta de aprendizaje,
 * con las mismas medidas que CourseHeroBannerCard y CourseLearningRoadmapCard para que no salte al cargar.
 */
@Composable
fun CourseDetailSkeleton(
    themeStyle: AppThemeStyle,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val shimmer = rememberSkeletonShimmer(themeStyle)
    val loadingDescription = stringResource(Res.string.content_loading)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .semantics { contentDescription = loadingDescription },
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SkeletonCard(themeStyle = themeStyle, cornerRadius = 24.dp, contentPadding = PaddingValues(0.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(190.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(112.dp).skeleton(shimmer, RectangleShape))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(3) {
                        Box(modifier = Modifier.weight(1f).height(58.dp).skeleton(shimmer, RoundedCornerShape(16.dp)))
                    }
                }
            }
        }

        SkeletonCard(themeStyle = themeStyle, cornerRadius = 20.dp, contentPadding = PaddingValues(16.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(150.dp).height(18.dp).skeleton(shimmer))
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.width(72.dp).height(18.dp).skeleton(shimmer))
                }
                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth().height(132.dp).skeleton(shimmer, RoundedCornerShape(18.dp)))
                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.width(170.dp).height(14.dp).skeleton(shimmer))
            }
        }
    }
}

/**
 * Esqueleto de una lista de tarjetas de una fila (como los módulos de un curso): indicador, título y flecha.
 * Los anchos de los títulos varían para que no parezca una tabla.
 */
@Composable
fun CardListSkeleton(
    themeStyle: AppThemeStyle,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    rows: Int = 7,
    spacing: Dp = 12.dp
) {
    val shimmer = rememberSkeletonShimmer(themeStyle)
    val loadingDescription = stringResource(Res.string.content_loading)
    val titleWidths = listOf(0.72f, 0.55f, 0.64f, 0.48f, 0.68f, 0.58f, 0.62f)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .semantics { contentDescription = loadingDescription },
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        repeat(rows) { index ->
            SkeletonCard(
                themeStyle = themeStyle,
                cornerRadius = 16.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                bordered = false
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(18.dp).skeleton(shimmer, CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(titleWidths[index % titleWidths.size])
                                .height(14.dp)
                                .skeleton(shimmer)
                        )
                        // La mayoría de los títulos reales ocupan dos líneas ("SEMANA 3: Control de Versiones
                        // Avanzados."): sin la segunda, la lista se encogía al aparecer el contenido.
                        if (index % 3 != 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.4f)
                                    .height(12.dp)
                                    .skeleton(shimmer)
                            )
                        }
                    }
                    Box(modifier = Modifier.size(20.dp).skeleton(shimmer, CircleShape))
                }
            }
        }
    }
}
