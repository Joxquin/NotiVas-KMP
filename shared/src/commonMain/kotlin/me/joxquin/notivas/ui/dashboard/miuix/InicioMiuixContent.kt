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
import androidx.compose.runtime.setValue
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
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.*

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
        bottom = 100.dp,
        start = 16.dp,
        end = 16.dp
    ),
    onNavigateToAjustes: () -> Unit = {},
    onNavigateToCourseProgreso: (me.joxquin.notivas.data.model.Course) -> Unit = {},
    onNavigateToCopilot: () -> Unit = {},
    onNavigateToHerramientas: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val urgentAssignments by viewModel.urgentAssignments.collectAsState()
    val allAssignments by viewModel.allAssignmentsUi.collectAsState()
    val courseStats by viewModel.courseStats.collectAsState()
    val unconfiguredBannerState by viewModel.unconfiguredBannerState.collectAsState()
    val institutionName by viewModel.institutionName.collectAsState()
    val selectedAssignmentForDraft by viewModel.selectedAssignmentForDraft.collectAsState()
    val morningBriefing by viewModel.morningBriefing.collectAsState()

    var searchQuery by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    val todayTasks = androidx.compose.runtime.remember(allAssignments) {
        val today = me.joxquin.notivas.util.DateTimeUtil.nowLocalDate()
        allAssignments.filter { ui ->
            val due = ui.assignment.dueAt ?: ui.assignment.lockAt ?: return@filter false
            val comp = me.joxquin.notivas.util.DateTimeUtil.parseIsoToComponents(due) ?: return@filter false
            comp.date == today
        }.sortedBy { ui ->
            val due = ui.assignment.dueAt ?: ui.assignment.lockAt ?: ""
            me.joxquin.notivas.util.DateTimeUtil.parseIsoToComponents(due)?.epochMillis ?: Long.MAX_VALUE
        }
    }

    val filteredCourses = androidx.compose.runtime.remember(courses, searchQuery) {
        if (searchQuery.isBlank()) courses
        else courses.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    (it.courseCode?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    if (selectedAssignmentForDraft != null) {
        me.joxquin.notivas.ui.dashboard.draft.AssignmentDraftScreen(
            assignmentUi = selectedAssignmentForDraft!!,
            onBack = { viewModel.closeDraft() },
            viewModel = viewModel,
            themeStyle = me.joxquin.notivas.data.local.AppThemeStyle.MIUIX,
            lazyListState = listState,
            modifier = modifier
        )
        return
    }

    var selectedAssignmentForSubmenu by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<me.joxquin.notivas.ui.dashboard.AssignmentUiModel?>(null)
    }

    val recentSearches by viewModel.recentSearches.collectAsState()
    val isSearchExpanded by viewModel.isSearchExpanded.collectAsState()
    var searchOffsetY by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(140.dp) }
    var searchOverlayAnimStatus by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(me.joxquin.notivas.ui.dashboard.components.search.SearchAnimStatus.COLLAPSED)
    }

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
            // 0. Welcome Header (32.sp) con scroll fade
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

                val studentName = userProfile?.name ?: "Estudiante"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .graphicsLayer {
                            alpha = largeTitleAlpha
                            translationY = largeTitleTranslationY
                        }
                ) {
                    MiuixText(
                        text = "Bienvenido, ${studentName.substringBefore(" ")}",
                        fontSize = MiuixTheme.textStyles.title1.fontSize,
                        fontWeight = FontWeight.Normal,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    MiuixText(
                        text = org.jetbrains.compose.resources.stringResource(me.joxquin.notivas.shared.resources.Res.string.dashboard_academic_title),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }

            // 1. Buscador Global (Cápsula Ancla con medición de coordenada Y)
            item {
                me.joxquin.notivas.ui.dashboard.components.search.DashboardSearchBarAnchor(
                    query = searchQuery,
                    onSearchClick = { viewModel.setSearchExpanded(true) },
                    onPositionChanged = { searchOffsetY = it },
                    onAskAi = { query -> onNavigateToCopilot() },
                    themeStyle = me.joxquin.notivas.data.local.AppThemeStyle.MIUIX,
                    isVisible = searchOverlayAnimStatus == me.joxquin.notivas.ui.dashboard.components.search.SearchAnimStatus.COLLAPSED && !isSearchExpanded
                )
            }

            // 2. Carrusel Hero: Rendimiento Académico + Configuración de Fórmulas
            item {
                me.joxquin.notivas.ui.dashboard.components.DashboardHeroCarousel(
                    courseStats = courseStats,
                    unconfiguredCount = unconfiguredBannerState.first,
                    firstUnconfiguredCourse = unconfiguredBannerState.second,
                    morningBriefing = morningBriefing,
                    onOpenRoadmap = { viewModel.openMorningRoadmap() },
                    onCheckPerformance = { onNavigateToHerramientas() },
                    onConfigureFormulas = { course ->
                        if (course != null) {
                            onNavigateToCourseProgreso(course)
                        } else if (courses.isNotEmpty()) {
                            onNavigateToCourseProgreso(courses.first())
                        }
                    },
                    themeStyle = me.joxquin.notivas.data.local.AppThemeStyle.MIUIX
                )
            }

            // 3. Today's Tasks: Entregas del Día
            item {
                me.joxquin.notivas.ui.dashboard.components.DashboardTodayTasksSection(
                    tasks = if (todayTasks.isNotEmpty()) todayTasks else urgentAssignments.take(3),
                    onSeeAll = onNavigateToSchedule,
                    onTaskClick = { selectedAssignmentForSubmenu = it },
                    themeStyle = me.joxquin.notivas.data.local.AppThemeStyle.MIUIX
                )
            }

            // 4. Courses Summary List (Grid de 2 columnas de cursos estilo MIUIX)
            item {
                MiuixCourseSummaryList(
                    courses = filteredCourses,
                    courseStats = courseStats,
                    onCourseClick = { course -> onNavigateToCourseProgreso(course) }
                )
            }
        }

        // Vista Incrustada de Búsqueda con Animación Ease Continua
        me.joxquin.notivas.ui.dashboard.components.search.DashboardAnimatedSearchOverlay(
            isExpanded = isSearchExpanded,
            onExpandedChange = { viewModel.setSearchExpanded(it) },
            initialOffsetY = searchOffsetY,
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearchCommit = { viewModel.addRecentSearch(it) },
            recentSearches = recentSearches,
            onRemoveRecentQuery = { viewModel.removeRecentSearch(it) },
            onClearRecentHistory = { viewModel.clearRecentSearches() },
            courses = courses,
            courseStats = courseStats,
            assignments = allAssignments,
            onCourseClick = onNavigateToCourseProgreso,
            onAssignmentClick = { selectedAssignmentForSubmenu = it },
            onAskAi = { query -> onNavigateToCopilot() },
            themeStyle = me.joxquin.notivas.data.local.AppThemeStyle.MIUIX,
            onAnimStatusChange = { searchOverlayAnimStatus = it }
        )

        // Submenú Rápido de Opciones y Gestión de Tarea (WindowBottomSheet)
        me.joxquin.notivas.ui.dashboard.components.AssignmentQuickSubmenuSheet(
            assignmentUi = selectedAssignmentForSubmenu,
            themeStyle = me.joxquin.notivas.data.local.AppThemeStyle.MIUIX,
            onDismissRequest = { selectedAssignmentForSubmenu = null },
            onOpenDraftScreen = { assignment ->
                viewModel.openDraft(assignment)
            }
        )
    }
}
