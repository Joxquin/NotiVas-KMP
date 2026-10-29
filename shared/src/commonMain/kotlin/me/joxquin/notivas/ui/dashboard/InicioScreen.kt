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
import me.joxquin.notivas.ui.animation.staggeredEntrance
import me.joxquin.notivas.ui.dashboard.components.CoursesMatrixSection
import me.joxquin.notivas.ui.dashboard.components.GreetingSection
import me.joxquin.notivas.ui.dashboard.components.QuickReloginSheet
import me.joxquin.notivas.ui.dashboard.components.SessionExpiredBanner
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
    onOpenAssignmentDraft: ((AssignmentUiModel) -> Unit)? = null,
    onNavigateToCourseProgreso: (me.joxquin.notivas.data.model.Course) -> Unit = {},
    onNavigateToCourseDetail: ((me.joxquin.notivas.data.model.Course) -> Unit)? = null,
    onNavigateToForoDetail: ((me.joxquin.notivas.data.model.CanvasDiscussionTopic) -> Unit)? = null,
    isEntranceTriggered: Boolean = true,
    modifier: Modifier = Modifier
) {
    val handleCourseClick: (me.joxquin.notivas.data.model.Course) -> Unit = { course ->
        if (onNavigateToCourseDetail != null) onNavigateToCourseDetail(course)
        else onNavigateToCourseProgreso(course)
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        InicioMiuixContent(
            viewModel = viewModel,
            listState = listState,
            onNavigateToAjustes = onNavigateToAjustes,
            onNavigateToCourseProgreso = onNavigateToCourseProgreso,
            onNavigateToCourseDetail = onNavigateToCourseDetail,
            onNavigateToCopilot = onNavigateToCopilot,
            onNavigateToHerramientas = onNavigateToHerramientas,
            onNavigateToSchedule = onNavigateToSchedule,
            onOpenAssignmentDraft = onOpenAssignmentDraft,
            onNavigateToForoDetail = onNavigateToForoDetail,
            isEntranceTriggered = isEntranceTriggered,
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
    val discussions by viewModel.discussions.collectAsState()
    val isSessionExpired by viewModel.isSessionExpired.collectAsState()
    val showQuickRelogin by viewModel.showQuickRelogin.collectAsState()
    val universityUrl by viewModel.universityUrl.collectAsState()

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
    val searchStatus by viewModel.searchStatus.collectAsState()
    var searchOffsetY by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(140.dp) }
    var searchOverlayAnimStatus by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(me.joxquin.notivas.ui.dashboard.components.search.SearchAnimStatus.COLLAPSED)
    }

    val topBarAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (searchOverlayAnimStatus.shouldExpand()) 0f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
        label = "InicioTopBarAlpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                InicioTopAppBar(
                    isScrolled = isScrolled,
                    title = "NotiVas",
                    onRefresh = { viewModel.refreshData() },
                    onNavigateToActivity = onNavigateToActivity,
                    onNavigateToSchedule = onNavigateToSchedule,
                    onNavigateToAjustes = onNavigateToAjustes,
                    actionsEnabled = !searchOverlayAnimStatus.shouldExpand(),
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .staggeredEntrance(index = 0, trigger = isEntranceTriggered)
                    ) {
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

                if (isSessionExpired) {
                    item {
                        SessionExpiredBanner(
                            onReconnect = { viewModel.openQuickRelogin() },
                            themeStyle = AppThemeStyle.MATERIAL
                        )
                    }
                }

                // ─── 1. Buscador Global (Cápsula Ancla con medición de coordenada Y) ──────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(index = 1, trigger = isEntranceTriggered)
                    ) {
                        me.joxquin.notivas.ui.dashboard.components.search.DashboardSearchBarAnchor(
                            query = searchQuery,
                            onSearchClick = { viewModel.setSearchExpanded(true) },
                            onPositionChanged = { searchOffsetY = it },
                            onAskAi = { query -> onNavigateToCopilot() },
                            themeStyle = AppThemeStyle.MATERIAL,
                            isVisible = searchOverlayAnimStatus.isCollapsed() && !isSearchExpanded
                        )
                    }
                }

                // ─── 2. Carrusel Hero: Rendimiento Académico + Configuración de Fórmulas ─
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(index = 2, trigger = isEntranceTriggered)
                    ) {
                        me.joxquin.notivas.ui.dashboard.components.DashboardHeroCarousel(
                            courseStats = courseStats,
                            unconfiguredCount = unconfiguredBannerState.first,
                            firstUnconfiguredCourse = unconfiguredBannerState.second,
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
                }

                // ─── 3. Today's Tasks: Entregas del Día ──────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(index = 3, trigger = isEntranceTriggered)
                    ) {
                        me.joxquin.notivas.ui.dashboard.components.DashboardTodayTasksSection(
                            tasks = if (todayTasks.isNotEmpty()) todayTasks else urgentAssignments.take(3),
                            onSeeAll = onNavigateToSchedule,
                            onTaskClick = { selectedAssignmentForSubmenu = it },
                            themeStyle = AppThemeStyle.MATERIAL
                        )
                    }
                }

                // ─── 4. Cursos Activos: Grid Rediseñado de Cursos ───────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(index = 4, trigger = isEntranceTriggered)
                    ) {
                        CoursesMatrixSection(
                            courseStats = courseStats,
                            courses = filteredCourses,
                            onCourseClick = handleCourseClick
                        )
                    }
                }
            }
        }

        // Vista Incrustada de Búsqueda con Animación Ease Continua (por encima de Scaffold)
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
            discussions = discussions,
            onCourseClick = handleCourseClick,
            onAssignmentClick = { selectedAssignmentForSubmenu = it },
            onForumClick = { onNavigateToForoDetail?.invoke(it) },
            onAskAi = { query -> onNavigateToCopilot() },
            themeStyle = AppThemeStyle.MATERIAL,
            onAnimStatusChange = {
                searchOverlayAnimStatus = it
                val mappedStatus = when (it) {
                    me.joxquin.notivas.ui.dashboard.components.search.SearchAnimStatus.EXPANDED -> me.joxquin.notivas.ui.components.miuix.SearchStatus.Status.EXPANDED
                    me.joxquin.notivas.ui.dashboard.components.search.SearchAnimStatus.EXPANDING -> me.joxquin.notivas.ui.components.miuix.SearchStatus.Status.EXPANDING
                    me.joxquin.notivas.ui.dashboard.components.search.SearchAnimStatus.COLLAPSING -> me.joxquin.notivas.ui.components.miuix.SearchStatus.Status.COLLAPSING
                    me.joxquin.notivas.ui.dashboard.components.search.SearchAnimStatus.COLLAPSED -> me.joxquin.notivas.ui.components.miuix.SearchStatus.Status.COLLAPSED
                }
                if (searchStatus.current != mappedStatus) {
                    viewModel.updateSearchStatus(searchStatus.copy(current = mappedStatus))
                }
            }
        )

        // Submenú Rápido de Opciones y Gestión de Tarea
        me.joxquin.notivas.ui.dashboard.components.AssignmentQuickSubmenuSheet(
            assignmentUi = selectedAssignmentForSubmenu,
            themeStyle = AppThemeStyle.MATERIAL,
            onDismissRequest = { selectedAssignmentForSubmenu = null },
            onOpenDraftScreen = { assignment ->
                selectedAssignmentForSubmenu = null
                viewModel.openDraft(assignment)
                onOpenAssignmentDraft?.invoke(assignment)
            },
            onMarkCompleted = { assignmentId ->
                viewModel.markAssignmentCompleted(assignmentId, true)
            }
        )

        if (showQuickRelogin) {
            QuickReloginSheet(
                universityUrl = universityUrl,
                onDismiss = { viewModel.closeQuickRelogin() },
                onSuccess = { cookies, csrfToken ->
                    viewModel.onReloginSuccess(cookies, csrfToken)
                }
            )
        }
    }
}
