package me.joxquin.notivas.ui.dashboard.miuix.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.ui.dashboard.CourseStat
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val MiuixBlue = Color(0xFF3482FF)
private val MiuixAmber = Color(0xFFFFB74D)
private val MiuixGreen = Color(0xFF66BB6A)
private val MiuixGreenBg = Color(0x3366BB6A)

/**
 * MiuixCourseSummaryList: Grid de 2 columnas de Cursos Activos estilo Stitch.
 */
@Composable
fun MiuixCourseSummaryList(
    courses: List<Course>,
    courseStats: List<CourseStat>,
    onCourseClick: (Course) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val barColors = listOf(
        MiuixBlue,
        Color(0xFFA855F7), // Morado
        MiuixAmber,
        Color(0xFF2DD4BF), // Teal
        Color(0xFFEC4899)  // Rosa
    )

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
                text = "CURSOS ACTIVOS",
                modifier = Modifier.weight(1f, fill = false)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(end = 20.dp)
            ) {
                Text(
                    text = "${courses.size} inscritos",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier
                        .squircleSurface(
                            color = Color.White.copy(alpha = 0.06f),
                            cornerRadius = 10.dp
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Text(
                    text = "Gestionar",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixBlue,
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
            coursePairs.forEachIndexed { pairIndex, pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val firstCourse = pair[0]
                    val firstIndex = pairIndex * 2
                    MiuixCourseGridCard(
                        course = firstCourse,
                        stat = courseStats.find { it.course.id == firstCourse.id },
                        accentColor = barColors[firstIndex % barColors.size],
                        onClick = { onCourseClick(firstCourse) },
                        modifier = Modifier.weight(1f)
                    )

                    if (pair.size > 1) {
                        val secondCourse = pair[1]
                        val secondIndex = firstIndex + 1
                        MiuixCourseGridCard(
                            course = secondCourse,
                            stat = courseStats.find { it.course.id == secondCourse.id },
                            accentColor = barColors[secondIndex % barColors.size],
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
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingCount = stat?.pendingCount ?: 0
    val avg = stat?.averageScore ?: 18.0

    Box(
        modifier = modifier
            .squircleSurface(
                color = MiuixTheme.colorScheme.surfaceContainer,
                cornerRadius = 20.dp
            )
            .squircleBorder(
                width = 0.5.dp,
                color = MiuixTheme.colorScheme.dividerLine,
                cornerRadius = 20.dp
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Barra superior de acento (h: 3dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )

                Text(
                    text = course.courseCode ?: "CS-301",
                    fontSize = 10.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )

                Text(
                    text = course.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 2,
                    lineHeight = 17.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer con badge de pendientes y nota sobre 20
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pendingCount > 0) {
                    Box(
                        modifier = Modifier
                            .squircleSurface(
                                color = Color(0xFF4A2800),
                                cornerRadius = 4.dp
                            )
                            .squircleBorder(
                                width = 0.5.dp,
                                color = MiuixAmber.copy(alpha = 0.3f),
                                cornerRadius = 4.dp
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$pendingCount pendientes",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = MiuixAmber
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .squircleSurface(
                                color = MiuixGreenBg,
                                cornerRadius = 4.dp
                            )
                            .squircleBorder(
                                width = 0.5.dp,
                                color = MiuixGreen.copy(alpha = 0.2f),
                                cornerRadius = 4.dp
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Al día ✓",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = MiuixGreen
                        )
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = (if (avg % 1.0 == 0.0) "${avg.toInt()}.0" else "$avg").take(4),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (avg >= 14.0) MiuixGreen else MiuixAmber
                    )
                    Text(
                        text = " /20",
                        fontSize = 9.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }
        }
    }
}
