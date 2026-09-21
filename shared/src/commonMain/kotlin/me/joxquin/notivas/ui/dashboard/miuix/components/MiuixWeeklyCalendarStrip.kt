package me.joxquin.notivas.ui.dashboard.miuix.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.ui.dashboard.DaySchedule
import me.joxquin.notivas.util.DateComponents
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val MiuixBlue = Color(0xFF3482FF)
private val MiuixGreen = Color(0xFF66BB6A)

/**
 * MiuixWeeklyCalendarStrip: Grid de 7 días con píldora azul activa estilo Stitch.
 */
@Composable
fun MiuixWeeklyCalendarStrip(
    weeklySchedule: List<DaySchedule>,
    selectedDate: DateComponents?,
    onDateSelect: (DateComponents) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonthYear = remember(selectedDate, weeklySchedule) {
        val targetDate = selectedDate
            ?: weeklySchedule.firstOrNull { it.isToday }?.date
            ?: weeklySchedule.firstOrNull()?.date
            ?: me.joxquin.notivas.util.DateTimeUtil.nowLocalDate()
        me.joxquin.notivas.util.DateTimeUtil.formatMonthYear(targetDate)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallTitle(
                text = "ESTA SEMANA",
                modifier = Modifier.weight(1f, fill = false)
            )
            Text(
                text = currentMonthYear,
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(end = 20.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .squircleSurface(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                    cornerRadius = 22.dp
                )
                .squircleBorder(
                    width = 0.5.dp,
                    color = MiuixTheme.colorScheme.dividerLine,
                    cornerRadius = 22.dp
                )
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklySchedule.forEach { day ->
                val isSelected = selectedDate == day.date
                val isToday = day.isToday

                val containerColor = when {
                    isSelected -> MiuixBlue
                    isToday -> Color.White.copy(alpha = 0.08f)
                    else -> Color.Transparent
                }

                val textColor = when {
                    isSelected || isToday -> Color.White
                    else -> MiuixTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                }

                val dotColor = when {
                    day.taskCount > 0 && isSelected -> Color.White
                    day.taskCount > 0 -> MiuixGreen
                    else -> Color.Transparent
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .squircleSurface(
                            color = containerColor,
                            cornerRadius = 16.dp
                        )
                        .clickable { onDateSelect(day.date) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = day.dayName.take(3),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                    Text(
                        text = "${day.dayNumber}",
                        fontSize = 14.sp,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}
