package me.joxquin.notivas.ui.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.util.DateTimeUtil
import me.joxquin.notivas.util.PlatformAlarmScheduler
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReminderBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    assignment: Assignment,
    courseName: String,
    themeStyle: AppThemeStyle,
    onReminderSet: () -> Unit
) {
    if (!show) return

    val initialDate = remember { DateTimeUtil.nowLocalDate() }

    var selectedYear by remember { mutableIntStateOf(initialDate.year) }
    var selectedMonth by remember { mutableIntStateOf(initialDate.month) }
    var selectedDay by remember { mutableIntStateOf(initialDate.day) }
    var selectedHour by remember { mutableIntStateOf(12) }
    var selectedMinute by remember { mutableIntStateOf(0) }

    val daysInMonth = remember(selectedYear, selectedMonth) {
        when (selectedMonth) {
            2 -> if ((selectedYear % 4 == 0 && selectedYear % 100 != 0) || (selectedYear % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }

    LaunchedEffect(daysInMonth) {
        if (selectedDay > daysInMonth) {
            selectedDay = daysInMonth
        }
    }

    val selectedTargetMillis = remember(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute) {
        DateTimeUtil.toEpochMillis(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute)
    }

    val dueMillis = remember(assignment.dueAt) {
        assignment.dueAt?.let {
            DateTimeUtil.parseIsoToComponents(it)?.epochMillis
        }
    }

    val isAfterDue = remember(selectedTargetMillis, dueMillis) {
        dueMillis != null && selectedTargetMillis > dueMillis
    }

    val onConfirm = {
        PlatformAlarmScheduler.scheduleCustomReminder(
            assignmentId = assignment.id,
            courseName = courseName,
            assignmentName = assignment.name,
            triggerEpochMillis = selectedTargetMillis
        )
        onReminderSet()
        onDismissRequest()
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        WindowBottomSheet(
            show = show,
            onDismissRequest = onDismissRequest,
            title = stringResource(Res.string.custom_reminder_title)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SmallTitle(text = stringResource(Res.string.custom_reminder_date_label))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumberPicker(
                            value = selectedDay,
                            onValueChange = { selectedDay = it },
                            range = 1..daysInMonth,
                            label = { it.toString().padStart(2, '0') },
                            wrapAround = true,
                            modifier = Modifier.weight(1f)
                        )
                        MiuixText(
                            text = "/",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        NumberPicker(
                            value = selectedMonth,
                            onValueChange = { selectedMonth = it },
                            range = 1..12,
                            label = { it.toString().padStart(2, '0') },
                            wrapAround = true,
                            modifier = Modifier.weight(1f)
                        )
                        MiuixText(
                            text = "/",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        NumberPicker(
                            value = selectedYear,
                            onValueChange = { selectedYear = it },
                            range = initialDate.year..(initialDate.year + 2),
                            label = { it.toString() },
                            wrapAround = false,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }

                SmallTitle(text = stringResource(Res.string.custom_reminder_time_label))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumberPicker(
                            value = selectedHour,
                            onValueChange = { selectedHour = it },
                            range = 0..23,
                            label = { it.toString().padStart(2, '0') },
                            wrapAround = true,
                            modifier = Modifier.weight(1f)
                        )
                        MiuixText(
                            text = ":",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        NumberPicker(
                            value = selectedMinute,
                            onValueChange = { selectedMinute = it },
                            range = 0..59,
                            label = { it.toString().padStart(2, '0') },
                            wrapAround = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (isAfterDue) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            MiuixText(
                                text = stringResource(Res.string.custom_reminder_due_warning),
                                fontSize = 12.sp,
                                color = MiuixTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        text = stringResource(Res.string.task_mark_completed_dialog_cancel),
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            color = MiuixTheme.colorScheme.surfaceContainerHigh,
                            textColor = MiuixTheme.colorScheme.onSurface
                        )
                    )
                    TextButton(
                        text = stringResource(Res.string.custom_reminder_save),
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.custom_reminder_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "Fecha: $selectedDay/$selectedMonth/$selectedYear",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Hora: ${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (isAfterDue) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = stringResource(Res.string.custom_reminder_due_warning),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(Res.string.task_mark_completed_dialog_cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onConfirm) {
                        Text(stringResource(Res.string.custom_reminder_save))
                    }
                }
            }
        }
    }
}
