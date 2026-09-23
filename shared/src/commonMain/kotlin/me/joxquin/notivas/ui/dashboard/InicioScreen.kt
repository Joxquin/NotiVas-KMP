package me.joxquin.notivas.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.ui.dashboard.components.CoursesMatrixSection
import me.joxquin.notivas.ui.dashboard.components.GreetingSection
import me.joxquin.notivas.ui.dashboard.components.InicioQuickActionsFab
import me.joxquin.notivas.ui.dashboard.components.InicioTopAppBar
import me.joxquin.notivas.ui.dashboard.components.SelectedDateAssignmentsSection
import me.joxquin.notivas.ui.dashboard.components.UrgentAssignmentsSection
import me.joxquin.notivas.ui.dashboard.components.WeeklyTimelineStrip
import me.joxquin.notivas.ui.dashboard.miuix.InicioMiuixContent

/**
 * Orquestador declarativo de la pantalla de Inicio de NotiVas.
 * Conmuta entre Material Design 3 y HyperOS Miuix según el estilo visual activo.
 */
@Composable
fun InicioScreen(
    viewModel: DashboardViewModel,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    onNavigateToCopilot: () -> Unit = {},
    onNavigateToHerramientas: () -> Unit = {},
    onNavigateToAjustes: () -> Unit = {},
    onNavigateToCourseProgreso: (me.joxquin.notivas.data.model.Course) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (themeStyle == AppThemeStyle.MIUIX) {
        InicioMiuixContent(
            viewModel = viewModel,
            listState = listState,
            onNavigateToAjustes = onNavigateToAjustes,
            onNavigateToCourseProgreso = onNavigateToCourseProgreso,
            modifier = modifier
        )
        return
    }

    val userProfile by viewModel.userProfile.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val urgentAssignments by viewModel.urgentAssignments.collectAsState()
    val courseStats by viewModel.courseStats.collectAsState()
    val unconfiguredBannerState by viewModel.unconfiguredBannerState.collectAsState()
    val institutionName by viewModel.institutionName.collectAsState()
    val weeklySchedule by viewModel.weeklySchedule.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedDateAssignments by viewModel.selectedDateAssignments.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()


    val isScrolled by androidx.compose.runtime.remember {
        androidx.compose.runtime.derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            InicioTopAppBar(
                isScrolled = isScrolled,
                title = "NotiVas",
                onRefresh = { viewModel.refreshData() },
                onNavigateToAjustes = onNavigateToAjustes
            )
        },
        floatingActionButton = {
            InicioQuickActionsFab(
                onNavigateToCopilot = onNavigateToCopilot,
                onNavigateToHerramientas = onNavigateToHerramientas
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 100.dp
            ),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── 0. Header con título ────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NotiVas",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dashboard Académico y Seguimiento Canvas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ─── 1. Top Greeting Bar & Status ────────────────────────────────
            item {
                GreetingSection(
                    profile = userProfile,
                    institution = institutionName,
                    syncStatus = syncStatus,
                    onRefresh = { viewModel.refreshData() },
                    onAvatarClick = onNavigateToAjustes
                )
            }

            // ─── 1.1 Banner Contextual de Cursos sin Configurar ──────────────
            item {
                me.joxquin.notivas.ui.dashboard.components.UnconfiguredCoursesBanner(
                    unconfiguredCount = unconfiguredBannerState.first,
                    firstUnconfiguredCourse = unconfiguredBannerState.second,
                    visible = unconfiguredBannerState.third,
                    onDismiss = { viewModel.dismissUnconfiguredBanner() },
                    onConfigureClick = { course ->
                        onNavigateToCourseProgreso(course)
                    }
                )
            }

            // ─── 2. Weekly Timeline Strip ────────────────────────────────────
            item {
                WeeklyTimelineStrip(
                    weeklySchedule = weeklySchedule,
                    selectedDate = selectedDate,
                    onDateSelect = { date -> viewModel.selectDate(date) }
                )
            }


            // ─── 2.1 Entregas de Fecha Seleccionada ─────────────────────────
            selectedDate?.let { date ->
                item {
                    SelectedDateAssignmentsSection(
                        selectedDate = date,
                        assignments = selectedDateAssignments,
                        onClearDate = { viewModel.selectDate(null) }
                    )
                }
            }

            // ─── 3. Para Hoy y Mañana Carousel ───────────────────────────────
            item {
                UrgentAssignmentsSection(
                    urgentAssignments = urgentAssignments,
                    onViewAll = onNavigateToHerramientas
                )
            }

            // ─── 4. Cursos del Semestre ──────────────────────────────────────
            item {
                CoursesMatrixSection(
                    courseStats = courseStats,
                    courses = courses,
                    onCourseClick = { /* navegar a detalles del curso */ }
                )
            }
        }
    }
}
