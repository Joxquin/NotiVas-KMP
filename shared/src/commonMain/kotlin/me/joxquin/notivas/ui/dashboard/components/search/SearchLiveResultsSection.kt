package me.joxquin.notivas.ui.dashboard.components.search

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
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.search_ask_copilot_format
import me.joxquin.notivas.shared.resources.search_filter_assignments
import me.joxquin.notivas.shared.resources.search_filter_courses
import me.joxquin.notivas.shared.resources.search_no_results
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.ui.dashboard.CourseStat
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun SearchLiveResultsSection(
    query: String,
    courses: List<Course>,
    courseStats: List<CourseStat>,
    assignments: List<AssignmentUiModel>,
    onCourseClick: (Course) -> Unit,
    onAssignmentClick: (AssignmentUiModel) -> Unit,
    onAskCopilot: (String) -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val containerColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow

    val trimmedQuery = query.trim()

    // Filtrado en vivo
    val matchingCourses = courses.filter {
        it.name.contains(trimmedQuery, ignoreCase = true) ||
                (it.courseCode?.contains(trimmedQuery, ignoreCase = true) == true)
    }

    val matchingAssignments = assignments.filter {
        it.assignment.name.contains(trimmedQuery, ignoreCase = true) ||
                it.courseName.contains(trimmedQuery, ignoreCase = true)
    }

    val hasResults = matchingCourses.isNotEmpty() || matchingAssignments.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!hasResults) {
            // Estado vacío con acción de IA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = onVariantColor,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = stringResource(Res.string.search_no_results, query),
                    style = MaterialTheme.typography.bodyMedium,
                    color = onVariantColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (isMiuix) {
                    TextButton(
                        text = stringResource(Res.string.search_ask_copilot_format, query),
                        onClick = { onAskCopilot(query) },
                        colors = MiuixButtonDefaults.textButtonColorsPrimary()
                    )
                } else {
                    Button(
                        onClick = { onAskCopilot(query) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(Res.string.search_ask_copilot_format, query),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        } else {
            // ─── 1. Cursos Encontrados ──────────────────────────────────────
            if (matchingCourses.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isMiuix) {
                        SmallTitle(
                            text = "${stringResource(Res.string.search_filter_courses).uppercase()} (${matchingCourses.size})",
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    } else {
                        Text(
                            text = "${stringResource(Res.string.search_filter_courses)} (${matchingCourses.size})",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = onSurfaceColor
                        )
                    }

                    matchingCourses.forEach { course ->
                        val stat = courseStats.find { it.course.id == course.id }
                        val avg = stat?.averageScore ?: 17.5
                        if (isMiuix) {
                            MiuixCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = MiuixCardDefaults.defaultColors(color = containerColor),
                                insideMargin = PaddingValues(12.dp),
                                pressFeedbackType = PressFeedbackType.Sink,
                                onClick = { onCourseClick(course) }
                            ) {
                                CourseResultRow(
                                    course = course,
                                    avg = avg,
                                    pendingCount = stat?.pendingCount ?: 0,
                                    isMiuix = true,
                                    onSurfaceColor = onSurfaceColor,
                                    onVariantColor = onVariantColor,
                                    primaryColor = primaryColor
                                )
                            }
                        } else {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = containerColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCourseClick(course) }
                            ) {
                                Box(modifier = Modifier.padding(12.dp)) {
                                    CourseResultRow(
                                        course = course,
                                        avg = avg,
                                        pendingCount = stat?.pendingCount ?: 0,
                                        isMiuix = false,
                                        onSurfaceColor = onSurfaceColor,
                                        onVariantColor = onVariantColor,
                                        primaryColor = primaryColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ─── 2. Tareas Encontradas ──────────────────────────────────────
            if (matchingAssignments.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isMiuix) {
                        SmallTitle(
                            text = "${stringResource(Res.string.search_filter_assignments).uppercase()} (${matchingAssignments.size})",
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    } else {
                        Text(
                            text = "${stringResource(Res.string.search_filter_assignments)} (${matchingAssignments.size})",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = onSurfaceColor
                        )
                    }

                    matchingAssignments.forEach { uiModel ->
                        val isCompleted = uiModel.assignment.isCompleted || uiModel.assignment.status == "completed"
                        if (isMiuix) {
                            MiuixCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = MiuixCardDefaults.defaultColors(color = containerColor),
                                insideMargin = PaddingValues(12.dp),
                                pressFeedbackType = PressFeedbackType.Sink,
                                onClick = { onAssignmentClick(uiModel) }
                            ) {
                                AssignmentResultRow(
                                    uiModel = uiModel,
                                    isCompleted = isCompleted,
                                    isMiuix = true,
                                    onSurfaceColor = onSurfaceColor,
                                    onVariantColor = onVariantColor,
                                    primaryColor = primaryColor
                                )
                            }
                        } else {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = containerColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAssignmentClick(uiModel) }
                            ) {
                                Box(modifier = Modifier.padding(12.dp)) {
                                    AssignmentResultRow(
                                        uiModel = uiModel,
                                        isCompleted = isCompleted,
                                        isMiuix = false,
                                        onSurfaceColor = onSurfaceColor,
                                        onVariantColor = onVariantColor,
                                        primaryColor = primaryColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ─── 3. Botón inferior para consultar con Copilot IA ────────────
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onAskCopilot(query) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(Res.string.search_ask_copilot_format, query),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = onSurfaceColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseResultRow(
    course: Course,
    avg: Double,
    pendingCount: Int,
    isMiuix: Boolean,
    onSurfaceColor: androidx.compose.ui.graphics.Color,
    onVariantColor: androidx.compose.ui.graphics.Color,
    primaryColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .then(
                    if (isMiuix) Modifier.squircleSurface(
                        color = MiuixTheme.colorScheme.surfaceContainerHigh,
                        cornerRadius = 10.dp
                    ) else Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.School,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = onSurfaceColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = course.courseCode ?: "CS-${course.id % 1000}",
                    style = MaterialTheme.typography.labelSmall,
                    color = onVariantColor
                )
                if (pendingCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(onVariantColor.copy(alpha = 0.5f))
                    )
                    Text(
                        text = "$pendingCount pendientes",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMiuix) MiuixTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = (avg * 10).toInt().toDouble().div(10).toString(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = primaryColor
            )
            Text(
                text = "/20",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = onVariantColor
            )
        }
    }
}

@Composable
private fun AssignmentResultRow(
    uiModel: AssignmentUiModel,
    isCompleted: Boolean,
    isMiuix: Boolean,
    onSurfaceColor: androidx.compose.ui.graphics.Color,
    onVariantColor: androidx.compose.ui.graphics.Color,
    primaryColor: androidx.compose.ui.graphics.Color
) {
    val statusColor = if (isCompleted) {
        if (isMiuix) MiuixTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    } else {
        primaryColor
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(statusColor)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = uiModel.assignment.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = onSurfaceColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = uiModel.courseName,
                style = MaterialTheme.typography.bodySmall,
                color = onVariantColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = onVariantColor,
            modifier = Modifier.size(14.dp)
        )
    }
}
