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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.dashboard_projects_section
import me.joxquin.notivas.ui.dashboard.CourseStat
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun DashboardProjectsBento(
    courseStats: List<CourseStat>,
    courses: List<Course>,
    onCourseClick: (Course) -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX

    // Tomar 2 asignaturas principales o destacadas
    val topCourses = remember(courses, courseStats) {
        if (courseStats.isNotEmpty()) {
            courseStats.take(2).map { it.course to it }
        } else {
            courses.take(2).map { it to null }
        }
    }

    if (topCourses.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(Res.string.dashboard_projects_section),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            topCourses.forEachIndexed { index, (course, stat) ->
                Box(modifier = Modifier.weight(1f)) {
                    ProjectCardItem(
                        course = course,
                        stat = stat,
                        index = index,
                        isMiuix = isMiuix,
                        onClick = { onCourseClick(course) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectCardItem(
    course: Course,
    stat: CourseStat?,
    index: Int,
    isMiuix: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary

    val score = stat?.averageScore ?: (14.0 + (index * 3.5))
    val progressRatio = ((score / 20.0).toFloat()).coerceIn(0.1f, 1f)

    if (isMiuix) {
        MiuixCard(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            colors = MiuixCardDefaults.defaultColors(color = containerColor),
            insideMargin = PaddingValues(14.dp),
            pressFeedbackType = PressFeedbackType.Sink,
            onClick = onClick
        ) {
            ProjectCardContent(
                course = course,
                score = score,
                progressRatio = progressRatio,
                onSurfaceColor = onSurfaceColor,
                onVariantColor = onVariantColor,
                primaryColor = primaryColor
            )
        }
    } else {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            modifier = Modifier.fillMaxWidth().height(160.dp).clickable(onClick = onClick)
        ) {
            Box(modifier = Modifier.padding(14.dp)) {
                ProjectCardContent(
                    course = course,
                    score = score,
                    progressRatio = progressRatio,
                    onSurfaceColor = onSurfaceColor,
                    onVariantColor = onVariantColor,
                    primaryColor = primaryColor
                )
            }
        }
    }
}

@Composable
private fun ProjectCardContent(
    course: Course,
    score: Double,
    progressRatio: Float,
    onSurfaceColor: androidx.compose.ui.graphics.Color,
    onVariantColor: androidx.compose.ui.graphics.Color,
    primaryColor: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(primaryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "${(progressRatio * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = primaryColor
            )
        }

        Spacer(Modifier.height(10.dp))

        Column {
            Text(
                text = course.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = onSurfaceColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = course.courseCode ?: "Nota: $score",
                style = MaterialTheme.typography.labelSmall,
                color = onVariantColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(12.dp))

        // Barra de progreso estilizada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(primaryColor.copy(alpha = 0.18f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressRatio)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(primaryColor)
            )
        }
    }
}
