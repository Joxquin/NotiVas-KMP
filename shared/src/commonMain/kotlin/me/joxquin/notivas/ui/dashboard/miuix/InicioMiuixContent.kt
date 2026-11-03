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
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Alarm
import top.yukonga.miuix.kmp.icon.extended.Weeks
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import me.joxquin.notivas.ui.animation.staggeredEntrance
import me.joxquin.notivas.ui.dashboard.DashboardViewModel
import me.joxquin.notivas.ui.dashboard.components.QuickReloginSheet
import me.joxquin.notivas.ui.dashboard.components.SessionExpiredBanner
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import me.joxquin.notivas.ui.dashboard.miuix.components.MiuixCourseHeader
import me.joxquin.notivas.ui.dashboard.miuix.components.MiuixCourseRowItem
import me.joxquin.notivas.ui.dashboard.miuix.components.MiuixCourseSummaryList
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme
import me.joxquin.notivas.ui.components.miuix.SearchBox
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.MiuixGlassyActionsContainer
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.*

/**
 * Dashboard Académico Miuix:
 * Implementación fiel del diseño HyperOS Miuix de Stitch y arquitectura canónica estilo KernelSU.
 */
@Composable
fun InicioMiuixContent(
    viewModel: DashboardViewModel,
    listState: LazyListState,
    contentPadding: PaddingValues? = null,
    onNavigateToAjustes: () -> Unit = {},
    onNavigateToCourseProgreso: (me.joxquin.notivas.data.model.Course) -> Unit = {},
    onNavigateToCourseDetail: ((me.joxquin.notivas.data.model.Course) -> Unit)? = null,
    onNavigateToCopilot: () -> Unit = {},
    onNavigateToHerramientas: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToRecentActivity: () -> Unit = {},
    onNavigateToAcademicPerformance: () -> Unit = onNavigateToHerramientas,
    onOpenAssignmentDraft: ((me.joxquin.notivas.ui.dashboard.AssignmentUiModel) -> Unit)? = null,
    onNavigateToForoDetail: ((me.joxquin.notivas.data.model.CanvasDiscussionTopic) -> Unit)? = null,
    isEntranceTriggered: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isWideScreen = me.joxquin.notivas.ui.util.LocalIsWideScreen.current
    val handleCourseClick: (me.joxquin.notivas.data.model.Course) -> Unit = { course ->
        if (onNavigateToCourseDetail != null) onNavigateToCourseDetail(course)
        else onNavigateToCourseProgreso(course)
    }
    val userProfile by viewModel.userProfile.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val courses by viewModel.courses.collectAsState()
    val urgentAssignments by viewModel.urgentAssignments.collectAsState()
    val allAssignments by viewModel.allAssignmentsUi.collectAsState()
    val courseStats by viewModel.courseStats.collectAsState()
    val weeklyDayBlocks by viewModel.weeklyDayBlocks.collectAsState()
    val unconfiguredBannerState by viewModel.unconfiguredBannerState.collectAsState()
    val institutionName by viewModel.institutionName.collectAsState()
    val discussions by viewModel.discussions.collectAsState()
    val isSessionExpired by viewModel.isSessionExpired.collectAsState()
    val showQuickRelogin by viewModel.showQuickRelogin.collectAsState()
    val universityUrl by viewModel.universityUrl.collectAsState()

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

    val backdrop = rememberMiuixBlurBackdrop()
    val scrollBehavior = MiuixScrollBehavior()
    val studentName = userProfile?.name?.substringBefore(" ") ?: "Estudiante"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface)
    ) {
        top.yukonga.miuix.kmp.basic.Scaffold(
            topBar = {
            MiuixBlurredBar(
                backdrop = backdrop,
                scrollBehavior = scrollBehavior
            ) {
                MiuixPageTopAppBar(
                    title = stringResource(Res.string.app_name),
                    largeTitle = "Bienvenido, ${studentName.uppercase()}",
                    subtitle = "",
                    scrollBehavior = scrollBehavior,
                    actions = {
                        me.joxquin.notivas.ui.components.miuix.MiuixGlassyActionsContainer(
                            backdrop = backdrop
                        ) {
                            top.yukonga.miuix.kmp.basic.IconButton(
                                onClick = onNavigateToSchedule
                            ) {
                                top.yukonga.miuix.kmp.basic.Icon(
                                    imageVector = MiuixIcons.Weeks,
                                    contentDescription = stringResource(Res.string.schedule_screen_title),
                                    tint = MiuixTheme.colorScheme.onBackground
                                )
                            }
                            top.yukonga.miuix.kmp.basic.IconButton(
                                onClick = onNavigateToRecentActivity
                            ) {
                                top.yukonga.miuix.kmp.basic.Icon(
                                    imageVector = MiuixIcons.Alarm,
                                    contentDescription = stringResource(Res.string.recent_activity_title),
                                    tint = MiuixTheme.colorScheme.onBackground
                                )
                            }
                        }
                    },
                    bottomContent = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            val collapsedFraction = scrollBehavior.state.collapsedFraction
                            val subtitleAlpha = (1f - (collapsedFraction * 2.5f)).coerceIn(0f, 1f)
                            val density = androidx.compose.ui.platform.LocalDensity.current
                            val searchTranslationY = with(density) {
                                (-26.dp * collapsedFraction).toPx()
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer { alpha = subtitleAlpha }
                                    .padding(bottom = 6.dp)
                            ) {
                                MiuixText(
                                    text = "Dashboard Académico y Seguimiento Canvas",
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            }

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
                                isVisible = searchStatus.isCollapsed() && searchOverlayAnimStatus.isCollapsed() && !isSearchExpanded,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        translationY = searchTranslationY
                                    }
                            )
                        }
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val pagePadding = miuixPageContentPadding(
            innerPadding = innerPadding,
            extraTop = 8.dp,
            extraStart = 0.dp,
            extraEnd = 0.dp,
            extraBottom = if (isWideScreen) 24.dp else 100.dp
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.surface)
                .miuixBackdropModifier(backdrop)
        ) {
            searchStatus.SearchBox {
                val refreshTexts = listOf(
                    stringResource(Res.string.refresh_pulling),
                    stringResource(Res.string.refresh_release),
                    stringResource(Res.string.refresh_refresh),
                    stringResource(Res.string.refresh_complete),
                )

                PullToRefresh(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refreshData() },
                    pullToRefreshState = pullToRefreshState,
                    topAppBarScrollBehavior = scrollBehavior,
                    refreshTexts = refreshTexts,
                    contentPadding = pagePadding,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .miuixPageScrollModifiers(scrollBehavior),
                        contentPadding = pagePadding,
                        overscrollEffect = null,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isSessionExpired) {
                            item {
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    SessionExpiredBanner(
                                        onReconnect = { viewModel.openQuickRelogin() },
                                        themeStyle = me.joxquin.notivas.data.local.AppThemeStyle.MIUIX
                                    )
                                }
                            }
                        }

                        // 1. Carrusel Hero: Rendimiento Académico + Configuración de Fórmulas
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .staggeredEntrance(index = 1, trigger = isEntranceTriggered)
                            ) {
                                me.joxquin.notivas.ui.dashboard.components.DashboardHeroCarousel(
                                    courseStats = courseStats,
                                    weeklyDayBlocks = weeklyDayBlocks,
                                    unconfiguredCount = unconfiguredBannerState.first,
                                    firstUnconfiguredCourse = unconfiguredBannerState.second,
                                    onCheckPerformance = onNavigateToAcademicPerformance,
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
                        }

                        // 2. Today's Tasks: Entregas del Día
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .staggeredEntrance(index = 2, trigger = isEntranceTriggered)
                            ) {
                                me.joxquin.notivas.ui.dashboard.components.DashboardTodayTasksSection(
                                    tasks = if (todayTasks.isNotEmpty()) todayTasks else urgentAssignments.take(3),
                                    onSeeAll = onNavigateToSchedule,
                                    onTaskClick = { selectedAssignmentForSubmenu = it },
                                    themeStyle = me.joxquin.notivas.data.local.AppThemeStyle.MIUIX,
                                    sectionTitle = if (todayTasks.isNotEmpty()) null else if (urgentAssignments.isNotEmpty()) "Próximas entregas" else null
                                )
                            }
                        }

                        // 3. Courses Summary List (Grid de 2 columnas de cursos estilo MIUIX virtualizado)
                        if (filteredCourses.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .staggeredEntrance(index = 3, trigger = isEntranceTriggered)
                                ) {
                                    MiuixCourseHeader(totalCourses = filteredCourses.size)
                                }
                            }

                            val coursePairs = filteredCourses.chunked(2)
                            itemsIndexed(
                                items = coursePairs,
                                key = { _, pair -> "miuix_course_pair_${pair.map { it.id }.joinToString("_")}" }
                            ) { pairIndex, pair ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .staggeredEntrance(index = 4 + pairIndex, trigger = isEntranceTriggered)
                                ) {
                                    MiuixCourseRowItem(
                                        firstCourse = pair[0],
                                        secondCourse = pair.getOrNull(1),
                                        courseStats = courseStats,
                                        onCourseClick = handleCourseClick
                                    )
                                }
                            }
                        }
                    }
                }
            }

                if (searchStatus.shouldCollapsed()) {
                    VerticalScrollBar(
                        adapter = rememberScrollBarAdapter(listState),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight(),
                        trackPadding = pagePadding
                    )
                }
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
            discussions = discussions,
            onCourseClick = handleCourseClick,
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
