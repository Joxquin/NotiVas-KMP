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
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
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
    onNavigateToRoadmap: () -> Unit = {},
    onOpenAssignmentDraft: ((me.joxquin.notivas.ui.dashboard.AssignmentUiModel) -> Unit)? = null,
    onNavigateToForoDetail: ((me.joxquin.notivas.data.model.CanvasDiscussionTopic) -> Unit)? = null,
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
    val morningBriefing by viewModel.morningBriefing.collectAsState()
    val discussions by viewModel.discussions.collectAsState()

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


    var selectedAssignmentForSubmenu by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<me.joxquin.notivas.ui.dashboard.AssignmentUiModel?>(null)
    }

    val recentSearches by viewModel.recentSearches.collectAsState()
    val isSearchExpanded by viewModel.isSearchExpanded.collectAsState()
    val searchStatus by viewModel.searchStatus.collectAsState()
    var searchOffsetY by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(140.dp) }
    var searchOverlayAnimStatus by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(me.joxquin.notivas.ui.dashboard.components.search.SearchAnimStatus.COLLAPSED)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface)
    ) {
        val autoSettleConnection = me.joxquin.notivas.ui.components.miuix.rememberMiuixAutoSettleScrollConnection(
            listState = listState,
            thresholdDp = 72.dp
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(autoSettleConnection),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 0. Welcome Header (32.sp) con scroll fade sincronizado con el TopAppBar
            item {
                val firstIndex = listState.firstVisibleItemIndex
                val firstOffset = listState.firstVisibleItemScrollOffset
                val isHeaderCollapsed = firstIndex > 0 || firstOffset >= 72

                val largeTitleAlpha by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isHeaderCollapsed) 0f else 1f,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
                    label = "LargeTitleAlpha"
                )
                val largeTitleTranslationY by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isHeaderCollapsed) -20f else 0f,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
                    label = "LargeTitleTranslationY"
                )

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
                    onSearchClick = {
                        viewModel.updateSearchStatus(searchStatus.copy(current = me.joxquin.notivas.ui.components.miuix.SearchStatus.Status.EXPANDING))
                        viewModel.setSearchExpanded(true)
                    },
                    onPositionChanged = {
                        searchOffsetY = it
                        viewModel.setSearchOffsetY(it)
                    },
                    onAskAi = { query -> onNavigateToCopilot() },
                    themeStyle = me.joxquin.notivas.data.local.AppThemeStyle.MIUIX,
                    isVisible = searchStatus.isCollapsed() && searchOverlayAnimStatus.isCollapsed() && !isSearchExpanded
                )
            }

            // 2. Carrusel Hero: Rendimiento Académico + Configuración de Fórmulas
            item {
                me.joxquin.notivas.ui.dashboard.components.DashboardHeroCarousel(
                    courseStats = courseStats,
                    unconfiguredCount = unconfiguredBannerState.first,
                    firstUnconfiguredCourse = unconfiguredBannerState.second,
                    morningBriefing = morningBriefing,
                    onOpenRoadmap = {
                        viewModel.openMorningRoadmap()
                        onNavigateToRoadmap()
                    },
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

        VerticalScrollBar(
            adapter = rememberScrollBarAdapter(listState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            trackPadding = contentPadding
        )

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
            discussions = discussions,
            onCourseClick = onNavigateToCourseProgreso,
            onAssignmentClick = { selectedAssignmentForSubmenu = it },
            onForumClick = { onNavigateToForoDetail?.invoke(it) },
            onAskAi = { query -> onNavigateToCopilot() },
            themeStyle = me.joxquin.notivas.data.local.AppThemeStyle.MIUIX,
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

        // Submenú Rápido de Opciones y Gestión de Tarea (WindowBottomSheet)
        me.joxquin.notivas.ui.dashboard.components.AssignmentQuickSubmenuSheet(
            assignmentUi = selectedAssignmentForSubmenu,
            themeStyle = me.joxquin.notivas.data.local.AppThemeStyle.MIUIX,
            onDismissRequest = { selectedAssignmentForSubmenu = null },
            onOpenDraftScreen = { assignment ->
                selectedAssignmentForSubmenu = null
                viewModel.openDraft(assignment)
                onOpenAssignmentDraft?.invoke(assignment)
            }
        )
    }
}
