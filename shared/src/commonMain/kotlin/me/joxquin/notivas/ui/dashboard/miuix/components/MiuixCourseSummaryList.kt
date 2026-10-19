package me.joxquin.notivas.ui.dashboard.miuix.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
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
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallTitle(
                text = stringResource(Res.string.courses_section_title).uppercase(),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(0.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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
            modifier = Modifier.fillMaxWidth(),
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
    val completedCount = stat?.completedCount ?: 0
    val totalTasks = pendingCount + completedCount
    val progressRatio = if (totalTasks > 0) {
        (completedCount.toFloat() / totalTasks.toFloat()).coerceIn(0f, 1f)
    } else {
        ((stat?.averageScore ?: 18.0) / 20.0).toFloat().coerceIn(0f, 1f)
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(durationMillis = 600)
    )
    val percentage = (progressRatio * 100).toInt()

    val shortCode = remember(course.courseCode, course.name) {
        course.courseCode?.takeIf { it.isNotBlank() }
            ?: if (course.name.contains("Mov", ignoreCase = true)) "ProgMov" else "CS-${course.id % 1000}"
    }

    MiuixCard(
        modifier = modifier.fillMaxWidth(),
        colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        insideMargin = PaddingValues(10.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cabecera: Portada de Canvas o Placeholder estilizado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MiuixTheme.colorScheme.surfaceContainerHigh)
            ) {
                if (!course.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = course.imageUrl,
                        contentDescription = course.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder estilizado con degradado Monet y gráficos mini bento
                    MiuixCourseCoverPlaceholder(
                        courseId = course.id,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Badge de código de curso flotante sobre la portada
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
                            cornerRadius = 6.dp
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = shortCode,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                }
            }

            // Cuerpo Central: Título y Subtítulo descriptivo
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = course.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 2,
                    lineHeight = 17.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(34.dp)
                )

                Text(
                    text = if (pendingCount > 0) {
                        stringResource(Res.string.course_pending_format, pendingCount)
                    } else {
                        stringResource(Res.string.course_up_to_date)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (pendingCount > 0) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Footer: Barra de Progreso y Porcentaje numérico
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "$percentage%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }
    }
}

/**
 * Placeholder estilizado con degradado suave Monet y barras mini que emulan la referencia visual.
 */
@Composable
private fun MiuixCourseCoverPlaceholder(
    courseId: Long,
    modifier: Modifier = Modifier
) {
    val baseHigh = MiuixTheme.colorScheme.surfaceContainerHigh
    val baseHighest = MiuixTheme.colorScheme.surfaceContainerHighest
    val accent = MiuixTheme.colorScheme.primary

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(baseHigh, baseHighest)
                )
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val h1 = (30 + ((courseId * 7) % 25).toInt()).dp
            val h2 = (45 + ((courseId * 13) % 25).toInt()).dp
            val h3 = (25 + ((courseId * 19) % 30).toInt()).dp

            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(h1)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(baseHighest.copy(alpha = 0.9f))
            )
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(h2)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(accent.copy(alpha = 0.8f))
            )
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(h3)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(baseHighest.copy(alpha = 0.9f))
            )
        }
    }
}
