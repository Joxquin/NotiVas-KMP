package me.joxquin.notivas.ui.dashboard.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.ui.dashboard.DashboardViewModel
import me.joxquin.notivas.ui.dashboard.components.SelectedDateAssignmentsSection
import me.joxquin.notivas.ui.dashboard.miuix.components.MiuixCourseSummaryList
import me.joxquin.notivas.ui.dashboard.miuix.components.MiuixStudentHeader
import me.joxquin.notivas.ui.dashboard.miuix.components.MiuixUrgentTasksCarousel
import me.joxquin.notivas.ui.dashboard.miuix.components.MiuixWeeklyCalendarStrip
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Dashboard Académico Miuix:
 * Implementación fiel del diseño HyperOS Miuix de Stitch.
 * Orquestador declarativo (< 120 líneas) que delega en MiuixDashboardComponents.
 */
@Composable
fun InicioMiuixContent(
    viewModel: DashboardViewModel,
    listState: LazyListState,
    contentPadding: PaddingValues = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 68.dp,
        bottom = 100.dp
    ),
    onNavigateToAjustes: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val urgentAssignments by viewModel.urgentAssignments.collectAsState()
    val courseStats by viewModel.courseStats.collectAsState()
    val institutionName by viewModel.institutionName.collectAsState()
    val weeklySchedule by viewModel.weeklySchedule.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedDateAssignments by viewModel.selectedDateAssignments.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 0. Large Title Canónico HyperOS Miuix (32.sp) con scroll fade
            item {
                val firstIndex = listState.firstVisibleItemIndex
                val firstOffset = listState.firstVisibleItemScrollOffset
                val largeTitleAlpha = if (firstIndex == 0) {
                    (1f - (firstOffset / 90f)).coerceIn(0f, 1f)
                } else {
                    0f
                }
                val largeTitleTranslationY = if (firstIndex == 0) {
                    -firstOffset * 0.25f
                } else {
                    -25f
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .graphicsLayer {
                            alpha = largeTitleAlpha
                            translationY = largeTitleTranslationY
                        }
                ) {
                    MiuixText(
                        text = "NotiVas",
                        fontSize = MiuixTheme.textStyles.title1.fontSize,
                        fontWeight = FontWeight.Normal,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    MiuixText(
                        text = "Dashboard Académico y Seguimiento Canvas",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }

            // 1. Student Profile Header (Avatar Squircle & Sync Capsule)
            item {
                MiuixStudentHeader(
                    profile = userProfile,
                    institution = institutionName,
                    syncStatus = syncStatus,
                    onRefresh = { viewModel.refreshData() },
                    onAvatarClick = onNavigateToAjustes
                )
            }

            // 2. Urgent Tasks Horizontal Snap Carousel (<12h, 12-48h, upcoming)
            item {
                MiuixUrgentTasksCarousel(
                    urgentAssignments = urgentAssignments
                )
            }

            // 3. Weekly 7-Day Scheduler Strip
            item {
                MiuixWeeklyCalendarStrip(
                    weeklySchedule = weeklySchedule,
                    selectedDate = selectedDate,
                    onDateSelect = { date -> viewModel.selectDate(date) }
                )
            }

            // 3.1 Tareas del día seleccionado si hay filtro
            selectedDate?.let { date ->
                item {
                    SelectedDateAssignmentsSection(
                        selectedDate = date,
                        assignments = selectedDateAssignments,
                        onClearDate = { viewModel.selectDate(null) }
                    )
                }
            }

            // 4. Courses Summary List (Grid de 2 columnas de cursos con barras de color y notas sobre 20)
            item {
                MiuixCourseSummaryList(
                    courses = courses,
                    courseStats = courseStats,
                    onCourseClick = { course -> viewModel.inspectCourse(course) }
                )
            }
        }
    }
}
