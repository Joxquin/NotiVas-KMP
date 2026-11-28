package me.joxquin.notivas.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.study_plan_available_time
import me.joxquin.notivas.shared.resources.study_plan_deadline
import me.joxquin.notivas.shared.resources.study_plan_decrease_estimate
import me.joxquin.notivas.shared.resources.study_plan_grade_improvement
import me.joxquin.notivas.shared.resources.study_plan_increase_estimate
import me.joxquin.notivas.shared.resources.study_plan_minutes
import me.joxquin.notivas.shared.resources.study_plan_no_fit
import me.joxquin.notivas.shared.resources.study_plan_scheduled
import me.joxquin.notivas.shared.resources.study_plan_subtitle
import me.joxquin.notivas.shared.resources.study_plan_summary
import me.joxquin.notivas.shared.resources.study_plan_title
import me.joxquin.notivas.shared.resources.study_plan_unscheduled
import me.joxquin.notivas.ui.dashboard.StudyPlanTaskUiModel
import me.joxquin.notivas.ui.dashboard.StudyPlanUiState
import me.joxquin.notivas.util.DateTimeUtil
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

private val AvailableMinutes = listOf(30, 60, 90)
private const val EstimateStep = 15
private const val MaxEstimate = 90

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardStudyPlanCard(
    state: StudyPlanUiState,
    themeStyle: AppThemeStyle,
    onAvailableMinutesChange: (Int) -> Unit,
    onEstimatedMinutesChange: (Long, Int) -> Unit,
    onTaskClick: (StudyPlanTaskUiModel) -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val container = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val onSurface = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onVariant = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val primary = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val onPrimary = if (isMiuix) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
    val divider = if (isMiuix) MiuixTheme.colorScheme.dividerLine else MaterialTheme.colorScheme.outlineVariant
    val type = MaterialTheme.typography

    val content: @Composable () -> Unit = {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .clickable(role = Role.Button) { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(Res.string.study_plan_title),
                        color = onSurface,
                        style = type.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!expanded) {
                        Text(stringResource(Res.string.study_plan_subtitle), color = onVariant, style = type.bodySmall)
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = onVariant
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(stringResource(Res.string.study_plan_available_time), color = onSurface, style = type.bodyMedium)
                    FlowRow(
                        modifier = Modifier.selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (minutes in AvailableMinutes) {
                            val selected = minutes == state.availableMinutes
                            Text(
                                text = stringResource(Res.string.study_plan_minutes, minutes),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) primary else Color.Transparent)
                                    .selectable(selected = selected, role = Role.RadioButton) {
                                        onAvailableMinutesChange(minutes)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                style = type.labelLarge,
                                color = if (selected) onPrimary else onSurface
                            )
                        }
                    }
                    val summary = if (state.scheduledMinutes == 0) {
                        stringResource(Res.string.study_plan_no_fit)
                    } else {
                        stringResource(Res.string.study_plan_summary, state.scheduledMinutes, state.availableMinutes)
                    }
                    Text(summary, color = primary, style = type.bodyMedium, fontWeight = FontWeight.SemiBold)
                    for (task in state.tasks) {
                        HorizontalDivider(color = divider)
                        StudyPlanTaskRow(
                            task = task,
                            isMiuix = isMiuix,
                            onSurface = onSurface,
                            onVariant = onVariant,
                            statusColor = if (task.isScheduled) primary else onVariant,
                            onEstimatedMinutesChange = { onEstimatedMinutesChange(task.assignment.id, it) },
                            onClick = { onTaskClick(task) }
                        )
                    }
                }
            }
        }
    }

    if (isMiuix) {
        MiuixCard(
            modifier = modifier.fillMaxWidth(),
            insideMargin = PaddingValues(0.dp),
            colors = MiuixCardDefaults.defaultColors(color = container),
            pressFeedbackType = PressFeedbackType.None
        ) {
            content()
        }
    } else {
        Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = container)) {
            content()
        }
    }
}

@Composable
private fun StudyPlanTaskRow(
    task: StudyPlanTaskUiModel,
    isMiuix: Boolean,
    onSurface: Color,
    onVariant: Color,
    statusColor: Color,
    onEstimatedMinutesChange: (Int) -> Unit,
    onClick: () -> Unit
) {
    val deadline = remember(task.deadline) { DateTimeUtil.parseIsoToComponents(task.deadline.toString()) }
    val type = MaterialTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onClick)
                .padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(task.assignment.name, color = onSurface, style = type.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(task.courseName, color = onVariant, style = type.bodySmall)
            if (deadline != null) {
                val hour = deadline.hour.toString().padStart(2, '0')
                val minute = deadline.minute.toString().padStart(2, '0')
                val label = stringResource(Res.string.study_plan_deadline, deadline.date.day, deadline.date.month, "$hour:$minute")
                Text(label, color = onVariant, style = type.bodySmall)
            }
            val status = if (task.isScheduled) Res.string.study_plan_scheduled else Res.string.study_plan_unscheduled
            Text(stringResource(status), color = statusColor, style = type.labelMedium)
            task.gradeImprovement?.let { points ->
                Text(
                    stringResource(Res.string.study_plan_grade_improvement, points.toString()),
                    color = onVariant,
                    style = type.bodySmall
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            EstimateButton(
                icon = Icons.Default.Remove,
                label = stringResource(Res.string.study_plan_decrease_estimate, task.assignment.name),
                enabled = task.estimatedMinutes > EstimateStep,
                isMiuix = isMiuix,
                onClick = { onEstimatedMinutesChange(task.estimatedMinutes - EstimateStep) }
            )
            Text(stringResource(Res.string.study_plan_minutes, task.estimatedMinutes), color = onSurface, style = type.labelLarge)
            EstimateButton(
                icon = Icons.Default.Add,
                label = stringResource(Res.string.study_plan_increase_estimate, task.assignment.name),
                enabled = task.estimatedMinutes < MaxEstimate,
                isMiuix = isMiuix,
                onClick = { onEstimatedMinutesChange(task.estimatedMinutes + EstimateStep) }
            )
        }
    }
}

@Composable
private fun EstimateButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    isMiuix: Boolean,
    onClick: () -> Unit
) {
    if (isMiuix) {
        MiuixIconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(48.dp)) {
            val color = if (enabled) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            Icon(icon, contentDescription = label, tint = color)
        }
    } else {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(icon, contentDescription = label)
        }
    }
}
