package me.joxquin.notivas.ui.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
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

@Composable
fun CoursesMatrixSection(
    courseStats: List<CourseStat>,
    courses: List<Course>,
    onCourseClick: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayCourses = if (courses.isNotEmpty()) {
        courses
    } else {
        emptyList()
    }

    if (displayCourses.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.courses_section_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = stringResource(Res.string.courses_enrolled_format, displayCourses.size),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = stringResource(Res.string.manage),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { }
            )
        }

        // Renderizado en Grid de 2 Columnas homogéneas
        val coursePairs = displayCourses.chunked(2)
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
                    MaterialCourseGridCard(
                        course = firstCourse,
                        stat = courseStats.find { it.course.id == firstCourse.id },
                        onClick = { onCourseClick(firstCourse) },
                        modifier = Modifier.weight(1f)
                    )

                    if (pair.size > 1) {
                        val secondCourse = pair[1]
                        MaterialCourseGridCard(
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
private fun MaterialCourseGridCard(
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
        me.joxquin.notivas.util.CourseUtil.extractCourseTag(course.courseCode, course.name)
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cabecera: Portada de Canvas o Placeholder estilizado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                if (!course.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = course.imageUrl,
                        contentDescription = course.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    MaterialCourseCoverPlaceholder(
                        courseId = course.id,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Badge de código de curso flotante sobre la portada
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.88f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = shortCode,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(34.dp)
                )

                Text(
                    text = if (pendingCount > 0) {
                        stringResource(Res.string.course_pending_format, pendingCount)
                    } else {
                        stringResource(Res.string.course_up_to_date)
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = if (pendingCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    progress = { animatedProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeCap = StrokeCap.Round
                )

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Placeholder estilizado con degradado suave Monet y barras mini para Material 3 Expressive.
 */
@Composable
private fun MaterialCourseCoverPlaceholder(
    courseId: Long,
    modifier: Modifier = Modifier
) {
    val baseHigh = MaterialTheme.colorScheme.surfaceContainerHigh
    val baseHighest = MaterialTheme.colorScheme.surfaceContainerHighest
    val accent = MaterialTheme.colorScheme.primary

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
