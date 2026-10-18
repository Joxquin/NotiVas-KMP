package me.joxquin.notivas.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.local.db.entities.ActivityLogEntity
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.dashboard_check_now
import me.joxquin.notivas.shared.resources.dashboard_performance_status
import me.joxquin.notivas.shared.resources.dashboard_see_performance
import me.joxquin.notivas.ui.dashboard.CourseStat
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun DashboardPerformanceBento(
    courseStats: List<CourseStat>,
    recentActivities: List<ActivityLogEntity>,
    onCheckNow: () -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX

    // Cálculo del promedio ponderado global o notas válidas
    val validScores = remember(courseStats) {
        courseStats.mapNotNull { it.averageScore }.filter { it > 0.0 }
    }
    val overallAverage = remember(validScores) {
        if (validScores.isNotEmpty()) validScores.average() else 15.8
    }

    val containerColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant

    // Última notificación o actividad destacada
    val latestActivity = recentActivities.firstOrNull()

    if (isMiuix) {
        MiuixCard(
            modifier = modifier.fillMaxWidth(),
            colors = MiuixCardDefaults.defaultColors(color = containerColor),
            insideMargin = androidx.compose.foundation.layout.PaddingValues(16.dp),
            pressFeedbackType = PressFeedbackType.None
        ) {
            PerformanceContent(
                overallAverage = overallAverage,
                latestActivity = latestActivity,
                onCheckNow = onCheckNow,
                isMiuix = true
            )
        }
    } else {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            modifier = modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                PerformanceContent(
                    overallAverage = overallAverage,
                    latestActivity = latestActivity,
                    onCheckNow = onCheckNow,
                    isMiuix = false
                )
            }
        }
    }
}

@Composable
private fun PerformanceContent(
    overallAverage: Double,
    latestActivity: ActivityLogEntity?,
    onCheckNow: () -> Unit,
    isMiuix: Boolean
) {
    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lado Izquierdo: Título y Botón "Check now"
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(Res.string.dashboard_see_performance),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = onSurfaceColor
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${((overallAverage / 20.0) * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = primaryColor
                    )
                    Text(
                        text = "promedio: ${((overallAverage * 10).toInt() / 10.0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = onVariantColor
                    )
                }

                Spacer(Modifier.height(2.dp))

                // Botón "Check now" como píldora oscura o de acento
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onCheckNow)
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.dashboard_check_now),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMiuix) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Lado Derecho: Mini Gráfica de Barras Estilizada + Badge de Crecimiento
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Badge de +3.45%
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(
                            if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh
                            else MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = stringResource(Res.string.dashboard_performance_status),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Mini ecualizador / gráfico de barras (6 columnas como en el diseño)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    val barHeights = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.85f, 1.0f)
                    barHeights.forEachIndexed { index, ratio ->
                        val isHighlighted = index == 3 || index == 6
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .fillMaxHeight(ratio)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isHighlighted) primaryColor
                                    else if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHighest
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }
            }
        }

        // Tira inferior: Notificación o insight reciente
        latestActivity?.let { activity ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${activity.title}: ${activity.description}",
                    style = MaterialTheme.typography.labelSmall,
                    color = onVariantColor,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
