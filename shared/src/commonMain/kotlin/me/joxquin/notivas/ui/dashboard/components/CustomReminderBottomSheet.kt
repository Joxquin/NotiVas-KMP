package me.joxquin.notivas.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card as M3Card
import androidx.compose.material3.CardDefaults as M3CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton as M3TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.navigation.miuix.LocalMiuixSnackbarHostState
import me.joxquin.notivas.util.DateTimeUtil
import me.joxquin.notivas.util.PlatformAlarmScheduler
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.SnackbarDuration
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassBottomSheet
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.foundation.layout.navigationBarsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReminderBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    assignment: Assignment,
    courseName: String,
    themeStyle: AppThemeStyle,
    backdrop: Backdrop? = null,
    onReminderSet: () -> Unit
) {
    if (!show) return

    val initialDate = remember { DateTimeUtil.nowLocalDate() }
    val nowHour = remember { DateTimeUtil.nowLocalHour() }

    // Calcular hora inicial válida en el futuro (+1 hora o mañana 9 AM si es de noche)
    val initialDayOffset = remember(nowHour) { if (nowHour >= 23) 1 else 0 }
    val initialHour24 = remember(nowHour) { if (nowHour >= 23) 9 else nowHour + 1 }
    val initialPeriod = remember(initialHour24) { if (initialHour24 >= 12) 1 else 0 }
    val initialHour12 = remember(initialHour24) {
        when {
            initialHour24 == 0 -> 12
            initialHour24 > 12 -> initialHour24 - 12
            else -> initialHour24
        }
    }

    var selectedDayOffset by remember { mutableIntStateOf(initialDayOffset) }
    var selectedHour12 by remember { mutableIntStateOf(initialHour12) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var selectedPeriod by remember { mutableIntStateOf(initialPeriod) } // 0 = AM, 1 = PM

    val selectedHour24 = remember(selectedHour12, selectedPeriod) {
        if (selectedPeriod == 0) {
            if (selectedHour12 == 12) 0 else selectedHour12
        } else {
            if (selectedHour12 == 12) 12 else selectedHour12 + 12
        }
    }

    val selectedDate = remember(initialDate, selectedDayOffset) {
        DateTimeUtil.addDays(initialDate, selectedDayOffset)
    }

    val selectedTargetMillis = remember(selectedDate, selectedHour24, selectedMinute) {
        DateTimeUtil.toEpochMillis(
            selectedDate.year,
            selectedDate.month,
            selectedDate.day,
            selectedHour24,
            selectedMinute
        )
    }

    val dueMillis = remember(assignment.dueAt) {
        assignment.dueAt?.let {
            DateTimeUtil.parseIsoToComponents(it)?.epochMillis
        }
    }

    val isAfterDue = remember(selectedTargetMillis, dueMillis) {
        dueMillis != null && selectedTargetMillis > dueMillis
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = LocalMiuixSnackbarHostState.current

    val successMsg = stringResource(Res.string.custom_reminder_saved_toast)
    val pastMsg = stringResource(Res.string.custom_reminder_invalid_past)

    val isPast = remember(selectedTargetMillis) {
        selectedTargetMillis <= DateTimeUtil.nowEpochMillis()
    }

    val dayNames = listOf(
        stringResource(Res.string.custom_reminder_day_mon),
        stringResource(Res.string.custom_reminder_day_tue),
        stringResource(Res.string.custom_reminder_day_wed),
        stringResource(Res.string.custom_reminder_day_thu),
        stringResource(Res.string.custom_reminder_day_fri),
        stringResource(Res.string.custom_reminder_day_sat),
        stringResource(Res.string.custom_reminder_day_sun),
    )
    val monthNames = listOf(
        stringResource(Res.string.custom_reminder_month_1),
        stringResource(Res.string.custom_reminder_month_2),
        stringResource(Res.string.custom_reminder_month_3),
        stringResource(Res.string.custom_reminder_month_4),
        stringResource(Res.string.custom_reminder_month_5),
        stringResource(Res.string.custom_reminder_month_6),
        stringResource(Res.string.custom_reminder_month_7),
        stringResource(Res.string.custom_reminder_month_8),
        stringResource(Res.string.custom_reminder_month_9),
        stringResource(Res.string.custom_reminder_month_10),
        stringResource(Res.string.custom_reminder_month_11),
        stringResource(Res.string.custom_reminder_month_12),
    )
    val amLabel = stringResource(Res.string.custom_reminder_period_am)
    val pmLabel = stringResource(Res.string.custom_reminder_period_pm)
    val dateFormatPattern = stringResource(Res.string.custom_reminder_date_format)

    val formatDateLabel: (Int) -> String = remember(initialDate, dayNames, monthNames, dateFormatPattern) {
        { offset ->
            val date = DateTimeUtil.addDays(initialDate, offset)
            val dow = DateTimeUtil.dayOfWeek(date)
            val dayStr = dayNames.getOrElse(dow - 1) { "" }
            val monthStr = monthNames.getOrElse(date.month - 1) { "" }
            dateFormatPattern
                .replace("%1\$s", dayStr)
                .replace("%2\$d", date.day.toString())
                .replace("%3\$s", monthStr)
        }
    }

    val itemHeightPx = with(LocalDensity.current) { 44.dp.toPx() }
    val strokeWidthPx = with(LocalDensity.current) { 1.dp.toPx() }

    val onConfirm: () -> Unit = {
        if (isPast) {
            coroutineScope.launch {
                snackbarHostState?.showSnackbar(
                    message = pastMsg,
                    duration = SnackbarDuration.Short
                )
            }
        } else {
            val scheduled = PlatformAlarmScheduler.scheduleCustomReminder(
                assignmentId = assignment.id,
                courseName = courseName,
                assignmentName = assignment.name,
                triggerEpochMillis = selectedTargetMillis
            )
            if (scheduled) {
                coroutineScope.launch {
                    snackbarHostState?.showSnackbar(
                        message = successMsg,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            onReminderSet()
            onDismissRequest()
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        val dividerColor = MiuixTheme.colorScheme.dividerLine.copy(alpha = 0.45f)
        GlassBottomSheet(
            show = show,
            onDismissRequest = onDismissRequest,
            backdrop = backdrop,
            title = stringResource(Res.string.custom_reminder_title)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                    insideMargin = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                val y1 = itemHeightPx
                                val y2 = itemHeightPx * 2
                                drawLine(
                                    color = dividerColor,
                                    start = Offset(0f, y1),
                                    end = Offset(size.width, y1),
                                    strokeWidth = strokeWidthPx
                                )
                                drawLine(
                                    color = dividerColor,
                                    start = Offset(0f, y2),
                                    end = Offset(size.width, y2),
                                    strokeWidth = strokeWidthPx
                                )
                            }
                            .padding(horizontal = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NumberPicker(
                                value = selectedDayOffset,
                                onValueChange = { selectedDayOffset = it },
                                range = 0..180,
                                label = formatDateLabel,
                                visibleItemCount = 3,
                                wrapAround = false,
                                textStyle = MiuixTheme.textStyles.title3.copy(
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.weight(2.5f)
                            )
                            NumberPicker(
                                value = selectedHour12,
                                onValueChange = { selectedHour12 = it },
                                range = 1..12,
                                label = { it.toString() },
                                visibleItemCount = 3,
                                wrapAround = true,
                                textStyle = MiuixTheme.textStyles.title2.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            NumberPicker(
                                value = selectedMinute,
                                onValueChange = { selectedMinute = it },
                                range = 0..59,
                                label = { it.toString().padStart(2, '0') },
                                visibleItemCount = 3,
                                wrapAround = true,
                                textStyle = MiuixTheme.textStyles.title2.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            NumberPicker(
                                value = selectedPeriod,
                                onValueChange = { selectedPeriod = it },
                                range = 0..1,
                                label = { if (it == 0) amLabel else pmLabel },
                                visibleItemCount = 3,
                                wrapAround = false,
                                textStyle = MiuixTheme.textStyles.title3.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.weight(1.1f)
                            )
                        }
                    }
                }

                if (isPast) {
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
                                text = pastMsg,
                                fontSize = 12.sp,
                                color = MiuixTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else if (isAfterDue) {
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
        val mDividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ModalBottomSheet(
            onDismissRequest = onDismissRequest
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.custom_reminder_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                M3Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = M3CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                val y1 = itemHeightPx
                                val y2 = itemHeightPx * 2
                                drawLine(
                                    color = mDividerColor,
                                    start = Offset(0f, y1),
                                    end = Offset(size.width, y1),
                                    strokeWidth = strokeWidthPx
                                )
                                drawLine(
                                    color = mDividerColor,
                                    start = Offset(0f, y2),
                                    end = Offset(size.width, y2),
                                    strokeWidth = strokeWidthPx
                                )
                            }
                            .padding(horizontal = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NumberPicker(
                                value = selectedDayOffset,
                                onValueChange = { selectedDayOffset = it },
                                range = 0..180,
                                label = formatDateLabel,
                                visibleItemCount = 3,
                                wrapAround = false,
                                textStyle = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.weight(2.5f)
                            )
                            NumberPicker(
                                value = selectedHour12,
                                onValueChange = { selectedHour12 = it },
                                range = 1..12,
                                label = { it.toString() },
                                visibleItemCount = 3,
                                wrapAround = true,
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            NumberPicker(
                                value = selectedMinute,
                                onValueChange = { selectedMinute = it },
                                range = 0..59,
                                label = { it.toString().padStart(2, '0') },
                                visibleItemCount = 3,
                                wrapAround = true,
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            NumberPicker(
                                value = selectedPeriod,
                                onValueChange = { selectedPeriod = it },
                                range = 0..1,
                                label = { if (it == 0) amLabel else pmLabel },
                                visibleItemCount = 3,
                                wrapAround = false,
                                textStyle = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.weight(1.1f)
                            )
                        }
                    }
                }

                if (isPast) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
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
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = pastMsg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                } else if (isAfterDue) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
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
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(Res.string.custom_reminder_due_warning),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    M3TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(Res.string.task_mark_completed_dialog_cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(Res.string.custom_reminder_save))
                    }
                }
            }
        }
    }
}
