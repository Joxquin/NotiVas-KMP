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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.dashboard_no_tasks_today
import me.joxquin.notivas.shared.resources.dashboard_see_all
import me.joxquin.notivas.shared.resources.dashboard_todays_tasks_section
import me.joxquin.notivas.shared.resources.schedule_completed_status
import me.joxquin.notivas.shared.resources.schedule_upcoming_status
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.util.DateTimeUtil
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun DashboardTodayTasksSection(
    tasks: List<AssignmentUiModel>,
    onSeeAll: () -> Unit,
    onTaskClick: (AssignmentUiModel) -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.dashboard_todays_tasks_section),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = onSurfaceColor
            )

            Text(
                text = stringResource(Res.string.dashboard_see_all),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = primaryColor,
                modifier = Modifier.clickable(onClick = onSeeAll)
            )
        }

        if (tasks.isEmpty()) {
            val emptyContainerColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow
            if (isMiuix) {
                MiuixCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = MiuixCardDefaults.defaultColors(color = emptyContainerColor),
                    insideMargin = PaddingValues(20.dp),
                    pressFeedbackType = PressFeedbackType.None
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(Res.string.dashboard_no_tasks_today),
                            style = MaterialTheme.typography.bodySmall,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = emptyContainerColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(Res.string.dashboard_no_tasks_today),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                tasks.take(4).forEach { item ->
                    TodayTaskCard(
                        item = item,
                        isMiuix = isMiuix,
                        onClick = { onTaskClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayTaskCard(
    item: AssignmentUiModel,
    isMiuix: Boolean,
    onClick: () -> Unit
) {
    val dueRaw = item.assignment.dueAt ?: item.assignment.lockAt ?: ""
    val timeFormatted = remember(dueRaw) {
        val comp = DateTimeUtil.parseIsoToComponents(dueRaw)
        if (comp != null) "Hoy: ${comp.hour.toString().padStart(2, '0')}:${comp.minute.toString().padStart(2, '0')}"
        else "Hoy: 23:59"
    }

    val isCompleted = item.assignment.isCompleted || item.assignment.status == "completed"
    val statusLabel = if (isCompleted) {
        stringResource(Res.string.schedule_completed_status)
    } else {
        stringResource(Res.string.schedule_upcoming_status)
    }

    val containerColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = if (isCompleted) {
        if (isMiuix) MiuixTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    } else {
        if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    }

    if (isMiuix) {
        MiuixCard(
            modifier = Modifier.fillMaxWidth(),
            colors = MiuixCardDefaults.defaultColors(color = containerColor),
            insideMargin = PaddingValues(14.dp),
            pressFeedbackType = PressFeedbackType.Sink,
            onClick = onClick
        ) {
            TaskCardContent(
                item = item,
                timeFormatted = timeFormatted,
                statusLabel = statusLabel,
                isCompleted = isCompleted,
                accentColor = accentColor,
                onSurfaceColor = onSurfaceColor,
                onVariantColor = onVariantColor
            )
        }
    } else {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
        ) {
            Box(modifier = Modifier.padding(14.dp)) {
                TaskCardContent(
                    item = item,
                    timeFormatted = timeFormatted,
                    statusLabel = statusLabel,
                    isCompleted = isCompleted,
                    accentColor = accentColor,
                    onSurfaceColor = onSurfaceColor,
                    onVariantColor = onVariantColor
                )
            }
        }
    }
}

@Composable
private fun TaskCardContent(
    item: AssignmentUiModel,
    timeFormatted: String,
    statusLabel: String,
    isCompleted: Boolean,
    accentColor: Color,
    onSurfaceColor: Color,
    onVariantColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Barra indicadora de estado
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.assignment.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = onSurfaceColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.courseName,
                    style = MaterialTheme.typography.bodySmall,
                    color = onVariantColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(onVariantColor.copy(alpha = 0.5f))
                )
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = accentColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Text(
            text = timeFormatted,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = onVariantColor,
            maxLines = 1,
            softWrap = false
        )
    }
}
