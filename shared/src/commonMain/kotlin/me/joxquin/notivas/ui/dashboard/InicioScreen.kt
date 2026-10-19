package me.joxquin.notivas.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.shared.resources.dashboard_academic_title

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
    onNavigateToActivity: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToCourseProgreso: (me.joxquin.notivas.data.model.Course) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (themeStyle == AppThemeStyle.MIUIX) {
        InicioMiuixContent(
            viewModel = viewModel,
            listState = listState,
            onNavigateToAjustes = onNavigateToAjustes,
            onNavigateToCourseProgreso = onNavigateToCourseProgreso,
            onNavigateToCopilot = onNavigateToCopilot,
            onNavigateToHerramientas = onNavigateToHerramientas,
            modifier = modifier
        )
        return
    }

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

    // Filtrar tareas que son de hoy
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

    // Filtrar cursos si hay búsqueda activa
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
            themeStyle = AppThemeStyle.MATERIAL,
            lazyListState = listState,
            modifier = modifier
        )
        return
    }

    var selectedAssignmentForSubmenu by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<AssignmentUiModel?>(null)
    }

    val isScrolled by androidx.compose.runtime.remember {
        androidx.compose.runtime.derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40
        }
    }

    val recentSearches by viewModel.recentSearches.collectAsState()
    val isSearchExpanded by viewModel.isSearchExpanded.collectAsState()
    var searchOffsetY by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(140.dp) }
    var searchOverlayAnimStatus by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(me.joxquin.notivas.ui.dashboard.components.search.SearchAnimStatus.COLLAPSED)
    }

    val topBarAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (searchOverlayAnimStatus.shouldExpand()) 0f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
        label = "InicioTopBarAlpha"
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            InicioTopAppBar(
                isScrolled = isScrolled,
                title = "NotiVas",
                onRefresh = { viewModel.refreshData() },
                onNavigateToActivity = onNavigateToActivity,
                onNavigateToSchedule = onNavigateToSchedule,
                onNavigateToAjustes = onNavigateToAjustes,
                modifier = Modifier.graphicsLayer {
                    alpha = topBarAlpha
                }
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = topBarAlpha
                }
            ) {
                if (topBarAlpha > 0.05f) {
                    InicioQuickActionsFab(
                        onNavigateToCopilot = onNavigateToCopilot,
                        onNavigateToHerramientas = onNavigateToHerramientas
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 100.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ─── 0. Header de Bienvenida (Welcome, Salung) ───────────────────
                item {
                    val studentName = userProfile?.name ?: "Estudiante"
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                        Text(
                            text = "Bienvenido, ${studentName.substringBefore(" ")}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(Res.string.dashboard_academic_title),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ─── 1. Buscador Global (Cápsula Ancla con medición de coordenada Y) ──────────
                item {
                    me.joxquin.notivas.ui.dashboard.components.search.DashboardSearchBarAnchor(
                        query = searchQuery,
                        onSearchClick = { viewModel.setSearchExpanded(true) },
                        onPositionChanged = { searchOffsetY = it },
                        onAskAi = { query -> onNavigateToCopilot() },
                        themeStyle = AppThemeStyle.MATERIAL,
                        isVisible = searchOverlayAnimStatus == me.joxquin.notivas.ui.dashboard.components.search.SearchAnimStatus.COLLAPSED && !isSearchExpanded
                    )
                }

                // ─── 2. Carrusel Hero: Rendimiento Académico + Configuración de Fórmulas ─
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
                        themeStyle = AppThemeStyle.MATERIAL
                    )
                }

                // ─── 3. Today's Tasks: Entregas del Día ──────────────────────────
                item {
                    me.joxquin.notivas.ui.dashboard.components.DashboardTodayTasksSection(
                        tasks = if (todayTasks.isNotEmpty()) todayTasks else urgentAssignments.take(3),
                        onSeeAll = onNavigateToSchedule,
                        onTaskClick = { selectedAssignmentForSubmenu = it },
                        themeStyle = AppThemeStyle.MATERIAL
                    )
                }

                // ─── 4. Cursos Activos: Grid Rediseñado de Cursos ───────────────
                item {
                    CoursesMatrixSection(
                        courseStats = courseStats,
                        courses = filteredCourses,
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
                themeStyle = AppThemeStyle.MATERIAL,
                onAnimStatusChange = { searchOverlayAnimStatus = it }
            )
        }

        // Submenú Rápido de Opciones y Gestión de Tarea
        me.joxquin.notivas.ui.dashboard.components.AssignmentQuickSubmenuSheet(
            assignmentUi = selectedAssignmentForSubmenu,
            themeStyle = AppThemeStyle.MATERIAL,
            onDismissRequest = { selectedAssignmentForSubmenu = null },
            onOpenDraftScreen = { assignment ->
                viewModel.openDraft(assignment)
            }
        )
    }
}
