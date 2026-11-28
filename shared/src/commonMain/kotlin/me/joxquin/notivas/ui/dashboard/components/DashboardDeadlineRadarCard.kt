package me.joxquin.notivas.ui.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.deadline_radar_earlier
import me.joxquin.notivas.shared.resources.deadline_radar_later
import me.joxquin.notivas.shared.resources.deadline_radar_title
import me.joxquin.notivas.shared.resources.deadline_short_date_time
import me.joxquin.notivas.ui.components.AcademicFeatureCard
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.ui.dashboard.DeadlineRadarUiItem
import me.joxquin.notivas.util.DateTimeUtil
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.time.Instant

@Composable
internal fun DashboardDeadlineRadarCard(
    changes: List<DeadlineRadarUiItem>,
    themeStyle: AppThemeStyle,
    onAssignmentClick: (AssignmentUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val titleColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val detailColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val divider = if (isMiuix) MiuixTheme.colorScheme.dividerLine else MaterialTheme.colorScheme.outlineVariant

    AcademicFeatureCard(themeStyle, modifier) {
        Text(stringResource(Res.string.deadline_radar_title), color = titleColor, style = MaterialTheme.typography.titleMedium)
        changes.forEachIndexed { index, item ->
            if (index > 0) HorizontalDivider(color = divider)
            val entry = item.entry
            val click = item.task?.let { task -> Modifier.clickable { onAssignmentClick(task) } } ?: Modifier
            Column(Modifier.fillMaxWidth().then(click).padding(vertical = 4.dp)) {
                Text(entry.title, color = titleColor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                entry.courseName?.let { Text(it, color = detailColor, style = MaterialTheme.typography.labelMedium) }
                val format = if (entry.current < entry.previous) Res.string.deadline_radar_earlier else Res.string.deadline_radar_later
                Text(
                    stringResource(format, formattedDeadline(entry.previous), formattedDeadline(entry.current)),
                    color = detailColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
internal fun formattedDeadline(deadline: Instant): String {
    val dateTime = DateTimeUtil.parseIsoToComponents(deadline.toString()) ?: return deadline.toString()
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return stringResource(Res.string.deadline_short_date_time, dateTime.date.day, dateTime.date.month, "$hour:$minute")
}
