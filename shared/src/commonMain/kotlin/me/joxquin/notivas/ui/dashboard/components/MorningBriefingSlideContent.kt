package me.joxquin.notivas.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.model.MorningBriefingData
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.morning_briefing_closing_today
import me.joxquin.notivas.shared.resources.morning_briefing_critical_tasks
import me.joxquin.notivas.shared.resources.morning_briefing_pill
import me.joxquin.notivas.shared.resources.morning_briefing_upcoming_tasks
import me.joxquin.notivas.shared.resources.morning_briefing_view_plan
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MorningBriefingSlideContent(
    briefing: MorningBriefingData,
    onOpenRoadmap: () -> Unit,
    isMiuix: Boolean,
    modifier: Modifier = Modifier
) {
    val pillBg =
        if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHighest
    val pillText = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val titleColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val summaryColor =
        if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val criticalColor = if (isMiuix) MiuixTheme.colorScheme.error else MaterialTheme.colorScheme.error
    val upcomingColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    val cardBg =
        if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenRoadmap),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Encabezado y Hora: Píldora temporal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = pillBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(pillText)
                    )
                    Text(
                        text = stringResource(Res.string.morning_briefing_pill),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = pillText
                        )
                    )
                }
            }

            // Affordance sutil
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(Res.string.morning_briefing_view_plan),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = pillText,
                        fontWeight = FontWeight.Medium
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = pillText,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        // 2. Saludo Contextual Personalizado
        Text(
            text = briefing.greeting,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = titleColor
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // 3. Métrica Clave Cuantificada (Semáforo de urgencia)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (briefing.criticalTasksCount > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = criticalColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(criticalColor)
                        )
                        Text(
                            text = stringResource(
                                Res.string.morning_briefing_critical_tasks,
                                briefing.criticalTasksCount
                            ),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = criticalColor
                            )
                        )
                    }
                }
            }

            if (briefing.upcomingTasksCount > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = upcomingColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(upcomingColor)
                        )
                        Text(
                            text = stringResource(
                                Res.string.morning_briefing_upcoming_tasks,
                                briefing.upcomingTasksCount
                            ),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = upcomingColor
                            )
                        )
                    }
                }
            }

            if (briefing.closingForumsCount > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = pillBg
                ) {
                    Text(
                        text = stringResource(Res.string.morning_briefing_closing_today, briefing.closingForumsCount),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = summaryColor,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 4. Tarea Más Crítica Destacada (Hero Task)
        briefing.heroTask?.let { heroUi ->
            val assignment = heroUi.assignment
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = cardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Indicador de color del curso Canvas
                    val parsedColor = briefing.heroTaskCourseColor?.let { raw ->
                        try {
                            val clean = if (raw.startsWith("#")) raw.substring(1) else raw
                            Color(clean.toLong(16) or 0x00000000FF000000)
                        } catch (_: Exception) {
                            null
                        }
                    } ?: pillText

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(parsedColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = parsedColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = heroUi.courseName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = parsedColor,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = assignment.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = titleColor
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            briefing.heroTaskCountdown?.let { countdown ->
                                Text(
                                    text = countdown,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = criticalColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            briefing.heroTaskPointsLabel?.let { pts ->
                                Text(
                                    text = "• $pts",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = summaryColor
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
