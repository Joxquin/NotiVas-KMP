package me.joxquin.notivas.ui.dashboard.miuix.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.util.DateTimeUtil
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val MiuixBlue = Color(0xFF3482FF)
private val MiuixRed = Color(0xFFFF5449)
private val MiuixRedBg = Color(0x33FF5449)
private val MiuixAmber = Color(0xFFFFB74D)

/**
 * MiuixUrgentTasksCarousel: Carrusel horizontal snap con tarjetas de urgencia HyperOS Stitch.
 */
@Composable
fun MiuixUrgentTasksCarousel(
    urgentAssignments: List<AssignmentUiModel>,
    onAssignmentClick: (AssignmentUiModel) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallTitle(
                text = "PARA HOY Y MAÑANA",
                modifier = Modifier.weight(1f, fill = false)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .padding(end = 20.dp)
                    .clickable { }
            ) {
                if (urgentAssignments.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .squircleSurface(
                                color = MiuixRedBg,
                                cornerRadius = 6.dp
                            )
                            .squircleBorder(
                                width = 0.5.dp,
                                color = MiuixRed.copy(alpha = 0.3f),
                                cornerRadius = 6.dp
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${urgentAssignments.size} Críticas",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixRed
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                }

                Text(
                    text = "Ver todas",
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Icon(
                    imageVector = MiuixIcons.ChevronForward,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        if (urgentAssignments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .squircleSurface(
                        color = MiuixTheme.colorScheme.surfaceContainer,
                        cornerRadius = 20.dp
                    )
                    .squircleBorder(
                        width = 0.5.dp,
                        color = MiuixTheme.colorScheme.dividerLine,
                        cornerRadius = 20.dp
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "¡Todo al día! No tienes entregas urgentes pendientes.",
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(urgentAssignments) { item ->
                    MiuixUrgentCarouselCard(
                        item = item,
                        onClick = { onAssignmentClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MiuixUrgentCarouselCard(
    item: AssignmentUiModel,
    onClick: () -> Unit
) {
    val now = DateTimeUtil.nowEpochMillis()
    val dueComp = item.assignment.dueAt?.let { DateTimeUtil.parseIsoToComponents(it) }
    val dueEpoch = dueComp?.epochMillis
    val isOverdue = dueEpoch != null && dueEpoch < now
    val diffMillis = if (dueEpoch != null) dueEpoch - now else null
    val isWithin12Hours = diffMillis != null && diffMillis in 1..(12 * 3600 * 1000L)
    val today = DateTimeUtil.nowLocalDate()
    val tomorrow = DateTimeUtil.addDays(today, 1)
    val isDueToday = dueComp != null && dueComp.date == today
    val isDueTomorrow = dueComp != null && dueComp.date == tomorrow

    val timeBadgeText = when {
        isOverdue -> "Vencida"
        isWithin12Hours && diffMillis != null -> {
            val hoursLeft = (diffMillis / (3600 * 1000L)).toInt()
            if (hoursLeft <= 0) "< 1h" else "En ${hoursLeft}h"
        }

        isDueToday -> "Hoy"
        isDueTomorrow -> "Mañana"
        diffMillis != null && diffMillis > 0 -> {
            val hoursLeft = (diffMillis / (3600 * 1000L)).toInt()
            "En ${hoursLeft}h"
        }

        else -> "Pendiente"
    }

    val barColor = when {
        isOverdue -> MiuixRed
        isWithin12Hours || isDueToday -> MiuixAmber
        else -> MiuixBlue
    }

    val countdownPillColor = when {
        isOverdue -> Pair(Color(0xFF410002), MiuixRed)
        isWithin12Hours || isDueToday -> Pair(Color(0xFF4A2800), MiuixAmber)
        else -> Pair(Color(0xFF00224A), MiuixBlue)
    }

    val ptsText = item.assignment.pointsPossible?.let { "${it.toInt()} pts" }

    Box(
        modifier = Modifier
            .width(285.dp)
            .squircleSurface(
                color = MiuixTheme.colorScheme.surfaceContainer,
                cornerRadius = 24.dp
            )
            .squircleBorder(
                width = 0.5.dp,
                color = MiuixTheme.colorScheme.dividerLine,
                cornerRadius = 24.dp
            )
            .clickable(onClick = onClick)
    ) {
        // Barra lateral de urgencia (5dp)
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(140.dp)
                .background(barColor)
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(start = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.courseName.take(16),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    modifier = Modifier
                        .squircleSurface(
                            color = Color.White.copy(alpha = 0.08f),
                            cornerRadius = 8.dp
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Box(
                    modifier = Modifier
                        .squircleSurface(
                            color = countdownPillColor.first,
                            cornerRadius = 12.dp
                        )
                        .squircleBorder(
                            width = 0.5.dp,
                            color = countdownPillColor.second.copy(alpha = 0.35f),
                            cornerRadius = 12.dp
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = timeBadgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = countdownPillColor.second
                    )
                }
            }

            Text(
                text = item.assignment.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.onSurface,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$ptsText • Virtual",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier
                        .squircleSurface(
                            color = Color.White.copy(alpha = 0.05f),
                            cornerRadius = 6.dp
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Row(
                    modifier = Modifier
                        .squircleSurface(
                            color = countdownPillColor.first,
                            cornerRadius = 14.dp
                        )
                        .squircleBorder(
                            width = 0.5.dp,
                            color = countdownPillColor.second.copy(alpha = 0.35f),
                            cornerRadius = 14.dp
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = MiuixIcons.Ok,
                        contentDescription = null,
                        tint = countdownPillColor.second,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Pendiente",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = countdownPillColor.second
                    )
                }
            }
        }
    }
}
