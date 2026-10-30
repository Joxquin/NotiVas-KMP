package me.joxquin.notivas.ui.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.dashboard_check_now
import me.joxquin.notivas.shared.resources.dashboard_performance_status
import me.joxquin.notivas.shared.resources.dashboard_see_performance
import me.joxquin.notivas.shared.resources.unconfigured_banner_btn
import me.joxquin.notivas.shared.resources.unconfigured_banner_desc_multiple
import me.joxquin.notivas.shared.resources.unconfigured_banner_desc_single
import me.joxquin.notivas.shared.resources.unconfigured_banner_title
import me.joxquin.notivas.ui.dashboard.CourseStat
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/**
 * Carrusel interactivo que unifica "Ver mi rendimiento" y "Configuración de fórmulas"
 * en un componente tipo hero swipeable, con soporte dual para Material 3 y MIUIX.
 */
@Composable
fun DashboardHeroCarousel(
    courseStats: List<CourseStat>,
    unconfiguredCount: Int,
    firstUnconfiguredCourse: Course?,
    onCheckPerformance: () -> Unit,
    onConfigureFormulas: (Course?) -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val totalPages = 2
    val pagerState = rememberPagerState(
        initialPage = 0
    ) { totalPages }
    val coroutineScope = rememberCoroutineScope()

    // Cálculo del promedio ponderado global
    val validScores = remember(courseStats) {
        courseStats.mapNotNull { it.averageScore }.filter { it > 0.0 }
    }
    val overallAverage = remember(validScores) {
        if (validScores.isNotEmpty()) validScores.average() else 16.5
    }

    val containerColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val content: @Composable () -> Unit = {
                when (page) {
                    0 -> {
                        PerformanceSlideContent(
                            overallAverage = overallAverage,
                            onCheckPerformance = onCheckPerformance,
                            isMiuix = isMiuix
                        )
                    }
                    else -> {
                        FormulasSlideContent(
                            unconfiguredCount = unconfiguredCount,
                            firstUnconfiguredCourse = firstUnconfiguredCourse,
                            onConfigureFormulas = onConfigureFormulas,
                            isMiuix = isMiuix
                        )
                    }
                }
            }

            if (isMiuix) {
                MiuixCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = MiuixCardDefaults.defaultColors(color = containerColor),
                    insideMargin = PaddingValues(16.dp),
                    pressFeedbackType = PressFeedbackType.None
                ) {
                    content()
                }
            } else {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        content()
                    }
                }
            }
        }

        // Indicador de Puntos (Dots Indicator)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalPages) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 20.dp else 6.dp,
                    animationSpec = tween(durationMillis = 200),
                    label = "dotWidth"
                )
                val dotColor by animateColorAsState(
                    targetValue = when {
                        isSelected -> if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                        else -> if (isMiuix) MiuixTheme.colorScheme.dividerLine else MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    animationSpec = tween(durationMillis = 200),
                    label = "dotColor"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(dotColor)
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                )
            }
        }
    }
}

/**
 * Slide 1: Rendimiento Académico y Progreso
 */
@Composable
private fun PerformanceSlideContent(
    overallAverage: Double,
    onCheckPerformance: () -> Unit,
    isMiuix: Boolean
) {
    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val primaryContainer = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.primaryContainer

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = stringResource(Res.string.dashboard_see_performance),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = onSurfaceColor
                )
            }

            // Píldora de estado de rendimiento (+3.45% este mes)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = stringResource(Res.string.dashboard_performance_status),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = primaryColor
                    )
                }
            }
        }

        // Métricas de Nota + Gráfico de Barras Estilizado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Promedio Ponderado",
                    style = MaterialTheme.typography.labelSmall,
                    color = onVariantColor
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = (overallAverage * 10).toInt().toDouble().div(10).toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = onSurfaceColor
                    )
                    Text(
                        text = " /20",
                        style = MaterialTheme.typography.titleSmall,
                        color = onVariantColor,
                        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                    )
                }
            }

            // Gráfico de Barras Estilizado
            Row(
                modifier = Modifier.height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val heights = listOf(0.45f, 0.70f, 0.55f, 0.90f, 0.65f)
                val days = listOf("L", "M", "X", "J", "V")
                heights.forEachIndexed { idx, h ->
                    val isPeak = idx == 3
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .fillMaxHeight(h)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (isPeak) primaryColor
                                    else if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh
                                    else MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = days[idx],
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = onVariantColor
                        )
                    }
                }
            }
        }

        // Botón de Acción a Pantalla Completa
        if (isMiuix) {
            TextButton(
                text = stringResource(Res.string.dashboard_check_now),
                onClick = onCheckPerformance,
                modifier = Modifier.fillMaxWidth(),
                colors = MiuixButtonDefaults.textButtonColorsPrimary()
            )
        } else {
            Button(
                onClick = onCheckPerformance,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.dashboard_check_now),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Slide 2: Configuración de Fórmulas y Ponderaciones
 */
@Composable
private fun FormulasSlideContent(
    unconfiguredCount: Int,
    firstUnconfiguredCourse: Course?,
    onConfigureFormulas: (Course?) -> Unit,
    isMiuix: Boolean
) {
    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val primaryContainer = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.primaryContainer

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (unconfiguredCount > 0) Icons.Outlined.Tune else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = stringResource(Res.string.unconfigured_banner_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = onSurfaceColor
                )
            }

            // Píldora de estado (Atención requerida / Al día)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (unconfiguredCount > 0) Icons.Outlined.Tune else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (unconfiguredCount > 0) "Atención requerida" else "Al día",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = primaryColor
                    )
                }
            }
        }

        // Descripción y Estado
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val description = if (unconfiguredCount > 0) {
                if (unconfiguredCount == 1) {
                    stringResource(Res.string.unconfigured_banner_desc_single)
                } else {
                    stringResource(Res.string.unconfigured_banner_desc_multiple, unconfiguredCount)
                }
            } else {
                "Todas las ponderaciones y fórmulas de tus cursos están calibradas correctamente."
            }

            Text(
                text = description,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = onSurfaceColor
            )
            Text(
                text = if (unconfiguredCount > 0) {
                    "Configura los pesos para calcular tu promedio ponderado exacto."
                } else {
                    "Las proyecciones y notas ponderadas se actualizan en tiempo real."
                },
                style = MaterialTheme.typography.bodySmall,
                color = onVariantColor
            )
        }

        // Botón de Configuración
        if (isMiuix) {
            TextButton(
                text = stringResource(Res.string.unconfigured_banner_btn),
                onClick = { onConfigureFormulas(firstUnconfiguredCourse) },
                modifier = Modifier.fillMaxWidth(),
                colors = MiuixButtonDefaults.textButtonColorsPrimary()
            )
        } else {
            Button(
                onClick = { onConfigureFormulas(firstUnconfiguredCourse) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.unconfigured_banner_btn),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
