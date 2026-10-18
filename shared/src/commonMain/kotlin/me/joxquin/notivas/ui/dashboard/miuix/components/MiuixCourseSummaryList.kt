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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.course_pending_format
import me.joxquin.notivas.shared.resources.course_up_to_date
import me.joxquin.notivas.shared.resources.courses_enrolled_format
import me.joxquin.notivas.shared.resources.courses_section_title
import me.joxquin.notivas.shared.resources.manage
import me.joxquin.notivas.ui.dashboard.CourseStat
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/**
 * MiuixCourseSummaryList: Grid de 2 columnas de Cursos Activos estilo HyperOS MIUIX.
 */
@Composable
fun MiuixCourseSummaryList(
    courses: List<Course>,
    courseStats: List<CourseStat>,
    onCourseClick: (Course) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (courses.isEmpty()) return

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
                text = stringResource(Res.string.courses_section_title).uppercase(),
                modifier = Modifier.weight(1f, fill = false)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(end = 20.dp)
            ) {
                Text(
                    text = stringResource(Res.string.courses_enrolled_format, courses.size),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.surfaceContainerHigh,
                            cornerRadius = 10.dp
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Text(
                    text = stringResource(Res.string.manage),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.clickable { }
                )
            }
        }

        // Renderizado en Grid de 2 Columnas
        val coursePairs = courses.chunked(2)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            coursePairs.forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val firstCourse = pair[0]
                    MiuixCourseGridCard(
                        course = firstCourse,
                        stat = courseStats.find { it.course.id == firstCourse.id },
                        onClick = { onCourseClick(firstCourse) },
                        modifier = Modifier.weight(1f)
                    )

                    if (pair.size > 1) {
                        val secondCourse = pair[1]
                        MiuixCourseGridCard(
                            course = secondCourse,
                            stat = courseStats.find { it.course.id == secondCourse.id },
                            onClick = { onCourseClick(secondCourse) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixCourseGridCard(
    course: Course,
    stat: CourseStat?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingCount = stat?.pendingCount ?: 0
    val avg = stat?.averageScore ?: 18.0

    MiuixCard(
        modifier = modifier.fillMaxWidth(),
        colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        insideMargin = PaddingValues(14.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Header: Código del curso y Nota sobre 20
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .squircleSurface(
                                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                cornerRadius = 6.dp
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = course.courseCode ?: "CS-${course.id % 1000}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = (if (avg % 1.0 == 0.0) "${avg.toInt()}.0" else "$avg").take(4),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (avg >= 13.0) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.error
                        )
                        Text(
                            text = " /20",
                            fontSize = 9.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }

                // Nombre del curso (altura fija para alineación homogénea en grid)
                Text(
                    text = course.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 2,
                    lineHeight = 17.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer con badge elegante de estado
            if (pendingCount > 0) {
                Box(
                    modifier = Modifier
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                            cornerRadius = 10.dp
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(MiuixTheme.colorScheme.error)
                        )
                        Text(
                            text = stringResource(Res.string.course_pending_format, pendingCount),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.surfaceContainerHigh,
                            cornerRadius = 10.dp
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(MiuixTheme.colorScheme.primary)
                        )
                        Text(
                            text = stringResource(Res.string.course_up_to_date),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
