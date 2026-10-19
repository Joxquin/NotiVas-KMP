package me.joxquin.notivas.ui

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.icon.extended.Alarm
import top.yukonga.miuix.kmp.icon.extended.Weeks
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.local.PreferencesManager
import me.joxquin.notivas.data.repository.CanvasRepository
import me.joxquin.notivas.data.repository.CopilotChatRepository
import me.joxquin.notivas.data.repository.CopilotRepository
import me.joxquin.notivas.data.model.UpdateInfo
import me.joxquin.notivas.data.repository.UpdateRepository
import me.joxquin.notivas.ui.components.UpdateDialog
import me.joxquin.notivas.ui.components.BackHandler
import me.joxquin.notivas.ui.components.miuix.FloatingNavigationBar
import me.joxquin.notivas.ui.components.miuix.MiuixTopAppBar
import me.joxquin.notivas.ui.copilot.CopilotViewModel
import me.joxquin.notivas.ui.dashboard.DashboardViewModel
import me.joxquin.notivas.ui.dashboard.InicioScreen
import me.joxquin.notivas.ui.navigation.MainDestination
import me.joxquin.notivas.ui.navigation.NotiVasNavigationBar
import me.joxquin.notivas.ui.notas.NotasViewModel
import me.joxquin.notivas.ui.profile.ProfileViewModel
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.*
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Filter
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs

@Stable
class MainPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set

    private var navJob: Job? = null

    fun animateToPage(targetIndex: Int) {
        if (targetIndex == selectedPage) return

        navJob?.cancel()
        selectedPage = targetIndex
        isNavigating = true

        navJob = coroutineScope.launch {
            val myJob = coroutineContext.job
            try {
                pagerState.scroll(MutatePriority.UserInput) {
                    val distance = abs(targetIndex - pagerState.currentPage).coerceAtLeast(1)
                    val duration = 80 * distance + 120
                    val layoutInfo = pagerState.layoutInfo
                    val pageSize = layoutInfo.pageSize + layoutInfo.pageSpacing
                    val currentDistanceInPages =
                        targetIndex - pagerState.currentPage - pagerState.currentPageOffsetFraction
                    val scrollPixels = currentDistanceInPages * pageSize

                    var previousValue = 0f
                    animate(
                        initialValue = 0f,
                        targetValue = scrollPixels,
                        animationSpec = tween(easing = EaseInOut, durationMillis = duration),
                    ) { currentValue, _ ->
                        previousValue += scrollBy(currentValue - previousValue)
                    }
                }

                if (pagerState.currentPage != targetIndex) {
                    pagerState.scrollToPage(targetIndex)
                }
            } finally {
                if (navJob == myJob) {
                    isNavigating = false
                    if (pagerState.currentPage != targetIndex) {
                        selectedPage = pagerState.currentPage
                    }
                }
            }
        }
    }

    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }
}

/**
 * MainAppShell:
 * Orquestador principal de navegación.
 * - En Material 3: Utiliza Scaffold con NavigationBar adosada al pie y pila de historial para BackHandler.
 * - En HyperOS Miuix: Utiliza la Arquitectura de 3 Capas Root Overlay con
 *   HorizontalPager interpolado para transiciones 100% fluidas sin tirones,
 *   MiuixTopAppBar colapsable, FloatingNavigationBar Liquid Glass y BackHandler con historial.
 */
@Composable
fun MainAppShell(
    canvasRepository: CanvasRepository,
    copilotRepository: CopilotRepository,
    chatRepository: CopilotChatRepository,
    preferencesManager: PreferencesManager
) {
    val themeStyle by preferencesManager.themeStyle.collectAsState(
        initial = preferencesManager.getThemeStyle()
    )

    val destinations = remember {
        listOf(
            MainDestination.Inicio,
            MainDestination.Herramientas,
            MainDestination.Copilot,
            MainDestination.Ajustes
        )
    }

    val pagerState = rememberPagerState(pageCount = { destinations.size })
    val coroutineScope = rememberCoroutineScope()
    val mainPagerState = remember(pagerState, coroutineScope) {
        MainPagerState(pagerState, coroutineScope)
    }

    // Interceptar gesto/botón Atrás del sistema para HyperOS Miuix:
    // Si no estamos en Inicio (página 0), volver a Inicio.
    // Si ya estamos en Inicio, el BackHandler se deshabilita para que el sistema cierre/minimice la app.
    BackHandler(enabled = themeStyle == AppThemeStyle.MIUIX && mainPagerState.selectedPage != 0) {
        mainPagerState.animateToPage(0)
    }

    val currentDestination by remember {
        derivedStateOf { destinations.getOrElse(mainPagerState.selectedPage) { MainDestination.Inicio } }
    }

    val dashboardViewModel = remember { DashboardViewModel(canvasRepository) }
    val notasViewModel = remember { NotasViewModel(canvasRepository) }
    val forosViewModel = remember {
        me.joxquin.notivas.ui.foros.ForosViewModel(canvasRepository, copilotRepository, preferencesManager)
    }
    val copilotViewModel = remember {
        CopilotViewModel(canvasRepository, copilotRepository, chatRepository, preferencesManager)
    }
    val profileViewModel = remember {
        ProfileViewModel(canvasRepository, copilotRepository, preferencesManager)
    }
    val archiveViewModel = remember {
        me.joxquin.notivas.ui.herramientas.archive.ArchiveViewModel(canvasRepository, preferencesManager)
    }
    val activityViewModel = remember {
        me.joxquin.notivas.ui.activity.ActivityViewModel(canvasRepository)
    }

    val updateRepository = remember { UpdateRepository() }
    var activeUpdateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    LaunchedEffect(Unit) {
        val info = updateRepository.checkForUpdates()
        if (info.isUpdateAvailable) {
            activeUpdateInfo = info
        }
    }

    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(dashboardViewModel) {
        dashboardViewModel.uiEffect.collect { effect ->
            when (effect) {
                is me.joxquin.notivas.ui.dashboard.DashboardViewModel.UiEffect.OpenUrl -> {
                    try {
                        uriHandler.openUri(effect.url)
                    } catch (_: Exception) {}
                }
                is me.joxquin.notivas.ui.dashboard.DashboardViewModel.UiEffect.ShowToast -> {
                    toastMessage = effect.message
                }
            }
        }
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(3500)
            toastMessage = null
        }
    }

    val inicioListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isInicioScrolled by remember {
        derivedStateOf {
            inicioListState.firstVisibleItemIndex > 0 || inicioListState.firstVisibleItemScrollOffset > 40
        }
    }
    val inicioScrollOffset by remember {
        derivedStateOf {
            if (inicioListState.firstVisibleItemIndex > 0) 100f
            else inicioListState.firstVisibleItemScrollOffset.toFloat()
        }
    }

    val activityListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isActivityScrolled by remember {
        derivedStateOf {
            activityListState.firstVisibleItemIndex > 0 || activityListState.firstVisibleItemScrollOffset > 40
        }
    }
    val activityScrollOffset by remember {
        derivedStateOf {
            if (activityListState.firstVisibleItemIndex > 0) 100f
            else activityListState.firstVisibleItemScrollOffset.toFloat()
        }
    }

    val herramientasListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isHerramientasScrolled by remember {
        derivedStateOf {
            herramientasListState.firstVisibleItemIndex > 0 || herramientasListState.firstVisibleItemScrollOffset > 40
        }
    }
    val herramientasScrollOffset by remember {
        derivedStateOf {
            if (herramientasListState.firstVisibleItemIndex > 0) 100f
            else herramientasListState.firstVisibleItemScrollOffset.toFloat()
        }
    }

    val ajustesListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isAjustesScrolled by remember {
        derivedStateOf {
            ajustesListState.firstVisibleItemIndex > 0 || ajustesListState.firstVisibleItemScrollOffset > 40
        }
    }
    val ajustesScrollOffset by remember {
        derivedStateOf {
            if (ajustesListState.firstVisibleItemIndex > 0) 100f
            else ajustesListState.firstVisibleItemScrollOffset.toFloat()
        }
    }

    val copilotListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isCopilotScrolled by remember {
        derivedStateOf {
            copilotListState.firstVisibleItemIndex > 0 || copilotListState.firstVisibleItemScrollOffset > 40
        }
    }
    val copilotScrollOffset by remember {
        derivedStateOf {
            if (copilotListState.firstVisibleItemIndex > 0) 100f
            else copilotListState.firstVisibleItemScrollOffset.toFloat()
        }
    }

    val contentBackdrop = rememberLayerBackdrop()
    var showingNotasDetail by remember { mutableStateOf(false) }
    var showingForosDetail by remember { mutableStateOf(false) }
    var showingArchiveDetail by remember { mutableStateOf(false) }
    var showingActivityDetail by remember { mutableStateOf(false) }
    var showingScheduleDetail by remember { mutableStateOf(false) }
    val showingMorningRoadmap by dashboardViewModel.showingMorningRoadmap.collectAsState()
    var showHerramientasTuneSheet by remember { mutableStateOf(false) }
    var showCatalogImportDialog by remember { mutableStateOf(false) }

    val forosUiState by forosViewModel.uiState.collectAsState()
    val activityUiState by activityViewModel.uiState.collectAsState()
    val selectedAssignmentForDraft by dashboardViewModel.selectedAssignmentForDraft.collectAsState()

    if (themeStyle == AppThemeStyle.MIUIX) {
        // En Miuix: Interceptar botón Atrás físico para volver de Roadmap -> Agenda -> Actividad -> Borrador -> Detalle del Foro -> Hub de Foros -> Hub de Herramientas o a Inicio
        BackHandler(enabled = showingMorningRoadmap || showingScheduleDetail || showingActivityDetail || selectedAssignmentForDraft != null || showingNotasDetail || showingForosDetail || showingArchiveDetail || currentDestination != MainDestination.Inicio) {
            if (showingMorningRoadmap) {
                dashboardViewModel.closeMorningRoadmap()
            } else if (showingScheduleDetail) {
                showingScheduleDetail = false
            } else if (showingActivityDetail) {
                showingActivityDetail = false
            } else if (selectedAssignmentForDraft != null) {
                dashboardViewModel.closeDraft()
            } else if (currentDestination == MainDestination.Herramientas) {
                if (showingForosDetail && forosUiState.selectedDiscussion != null) {
                    forosViewModel.clearSelectedDiscussion()
                } else if (showingNotasDetail || showingForosDetail || showingArchiveDetail) {
                    showingNotasDetail = false
                    showingForosDetail = false
                    showingArchiveDetail = false
                } else {
                    mainPagerState.animateToPage(0)
                }
            } else if (currentDestination != MainDestination.Inicio) {
                mainPagerState.animateToPage(0)
            }
        }

        // ─── ARQUITECTURA DE 3 CAPAS HYPEROS MIUIX ───────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.surface)
        ) {
            // Capa 1: Contenido de la Pantalla Activa con HorizontalPager o Borrador de Tarea
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(contentBackdrop)
            ) {
                if (showingActivityDetail) {
                    me.joxquin.notivas.ui.activity.RecentActivityScreen(
                        viewModel = activityViewModel,
                        themeStyle = AppThemeStyle.MIUIX,
                        onBack = { showingActivityDetail = false },
                        onNavigateToAssignment = { assignmentId ->
                            showingActivityDetail = false
                            coroutineScope.launch {
                                val assignment = canvasRepository.getAssignment(assignmentId)
                                if (assignment != null) {
                                    val courseName = notasViewModel.uiState.value.courses.find { it.id == assignment.courseId }?.name ?: "Curso"
                                    dashboardViewModel.openDraft(
                                        me.joxquin.notivas.ui.dashboard.AssignmentUiModel(
                                            assignment = assignment,
                                            courseName = courseName
                                        )
                                    )
                                }
                            }
                        },
                        onNavigateToForum = { topicId ->
                            showingActivityDetail = false
                            val topic = forosUiState.discussions.find { it.id == topicId }
                            if (topic != null) {
                                forosViewModel.selectDiscussion(topic)
                                showingNotasDetail = false
                                showingArchiveDetail = false
                                showingForosDetail = true
                                mainPagerState.animateToPage(1)
                            }
                        },
                        onOpenUrl = { url ->
                            try {
                                uriHandler.openUri(url)
                            } catch (_: Exception) {}
                        },
                        lazyListState = activityListState,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (showingMorningRoadmap) {
                    me.joxquin.notivas.ui.dashboard.roadmap.MorningRoadmapScreen(
                        viewModel = dashboardViewModel,
                        onBack = { dashboardViewModel.closeMorningRoadmap() },
                        onNavigateToCopilotWithPrompt = { prompt ->
                            dashboardViewModel.closeMorningRoadmap()
                            showingNotasDetail = false
                            showingForosDetail = false
                            mainPagerState.animateToPage(2)
                            copilotViewModel.sendMessage(prompt)
                        },
                        themeStyle = AppThemeStyle.MIUIX,
                        lazyListState = inicioListState,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (showingScheduleDetail) {
                    me.joxquin.notivas.ui.dashboard.schedule.ScheduleScreen(
                        viewModel = dashboardViewModel,
                        themeStyle = AppThemeStyle.MIUIX,
                        onBack = { showingScheduleDetail = false },
                        onAssignmentClick = { assignmentUi ->
                            dashboardViewModel.openDraft(assignmentUi)
                        },
                        lazyListState = inicioListState,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (selectedAssignmentForDraft != null) {
                    me.joxquin.notivas.ui.dashboard.draft.AssignmentDraftScreen(
                        assignmentUi = selectedAssignmentForDraft!!,
                        onBack = { dashboardViewModel.closeDraft() },
                        viewModel = dashboardViewModel,
                        themeStyle = AppThemeStyle.MIUIX,
                        lazyListState = inicioListState,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    HorizontalPager(
                        state = mainPagerState.pagerState,
                        userScrollEnabled = false,
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.Top,
                        beyondViewportPageCount = 1
                    ) { page ->
                        when (destinations[page]) {
                            MainDestination.Inicio -> {
                                InicioScreen(
                                    viewModel = dashboardViewModel,
                                    themeStyle = AppThemeStyle.MIUIX,
                                    listState = inicioListState,
                                    onNavigateToCopilot = {
                                        showingNotasDetail = false
                                        showingForosDetail = false
                                        mainPagerState.animateToPage(2)
                                    },
                                    onNavigateToHerramientas = {
                                        showingNotasDetail = false
                                        showingForosDetail = false
                                        mainPagerState.animateToPage(1)
                                    },
                                    onNavigateToAjustes = {
                                        showingNotasDetail = false
                                        showingForosDetail = false
                                        mainPagerState.animateToPage(3)
                                    },
                                    onNavigateToActivity = {
                                        showingScheduleDetail = false
                                        showingActivityDetail = true
                                    },
                                    onNavigateToSchedule = {
                                        showingActivityDetail = false
                                        showingScheduleDetail = true
                                    },
                                    onNavigateToCourseProgreso = { course ->
                                        notasViewModel.selectCourse(course)
                                        showingForosDetail = false
                                        showingNotasDetail = true
                                        mainPagerState.animateToPage(1)
                                    }
                                )
                            }


                            MainDestination.Herramientas -> {
                                me.joxquin.notivas.ui.herramientas.HerramientasScreen(
                                    viewModel = notasViewModel,
                                    forosViewModel = forosViewModel,
                                    archiveViewModel = archiveViewModel,
                                    showingNotasDetail = showingNotasDetail,
                                    showingForosDetail = showingForosDetail,
                                    showingArchiveDetail = showingArchiveDetail,
                                    onOpenProgreso = {
                                        showingForosDetail = false
                                        showingArchiveDetail = false
                                        showingNotasDetail = true
                                    },
                                    onOpenForos = {
                                        showingNotasDetail = false
                                        showingArchiveDetail = false
                                        showingForosDetail = true
                                    },
                                    onOpenArchive = {
                                        showingNotasDetail = false
                                        showingForosDetail = false
                                        showingArchiveDetail = true
                                    },
                                    onOpenAssignmentDetail = { assignment ->
                                        val courseName = notasViewModel.uiState.value.courses.find { it.id == assignment.courseId }?.name ?: "Curso"
                                        dashboardViewModel.openDraft(
                                            me.joxquin.notivas.ui.dashboard.AssignmentUiModel(
                                                assignment = assignment,
                                                courseName = courseName
                                            )
                                        )
                                    },
                                    onOpenTuneSettings = { showHerramientasTuneSheet = true },
                                    onBack = {
                                        showingNotasDetail = false
                                        showingForosDetail = false
                                        showingArchiveDetail = false
                                    },
                                    themeStyle = AppThemeStyle.MIUIX,
                                    lazyListState = herramientasListState
                                )
                            }

                            MainDestination.Copilot -> {
                                me.joxquin.notivas.ui.copilot.CopilotScreen(
                                    viewModel = copilotViewModel,
                                    themeStyle = AppThemeStyle.MIUIX,
                                    lazyListState = copilotListState,
                                    onNavigateToSettings = {
                                        showingNotasDetail = false
                                        showingForosDetail = false
                                        mainPagerState.animateToPage(3)
                                    }
                                )
                            }

                            MainDestination.Ajustes -> {
                                me.joxquin.notivas.ui.ajustes.AjustesScreen(
                                    viewModel = profileViewModel,
                                    themeStyle = AppThemeStyle.MIUIX,
                                    lazyListState = ajustesListState
                                )
                            }
                        }
                    }
                }
            }

            val copilotUiState by copilotViewModel.uiState.collectAsState()
            val isDashboardSearchExpanded by dashboardViewModel.isSearchExpanded.collectAsState()
            val topBarAlpha by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (currentDestination == MainDestination.Inicio && isDashboardSearchExpanded) 0f else 1f,
                animationSpec = androidx.compose.animation.core.tween(200)
            )

            // Capa 2: Barra Superior Canónica Unificada (Top Overlay)
            MiuixTopAppBar(
                title = when {
                    showingMorningRoadmap -> stringResource(Res.string.daily_roadmap_title)
                    showingScheduleDetail -> stringResource(Res.string.schedule_screen_title)
                    showingActivityDetail -> stringResource(Res.string.recent_activity_title)
                    selectedAssignmentForDraft != null -> stringResource(Res.string.assignment_detail_title)
                    currentDestination == MainDestination.Herramientas && showingArchiveDetail -> stringResource(Res.string.archive_screen_title)
                    currentDestination == MainDestination.Herramientas && showingNotasDetail -> stringResource(Res.string.grades_progreso_title)
                    currentDestination == MainDestination.Herramientas && showingForosDetail -> {
                        if (forosUiState.selectedDiscussion != null) stringResource(Res.string.foro_detail_title) else stringResource(Res.string.foros_hub_title)
                    }
                    currentDestination == MainDestination.Inicio -> stringResource(Res.string.app_name)
                    currentDestination == MainDestination.Herramientas -> stringResource(Res.string.herramientas_title)
                    currentDestination == MainDestination.Copilot -> stringResource(Res.string.copilot_title)
                    currentDestination == MainDestination.Ajustes -> stringResource(Res.string.settings_title)
                    else -> stringResource(Res.string.app_name)
                },
                isScrolled = when {
                    showingMorningRoadmap -> isInicioScrolled
                    showingScheduleDetail -> isInicioScrolled
                    showingActivityDetail -> isActivityScrolled
                    selectedAssignmentForDraft != null -> true
                    currentDestination == MainDestination.Inicio -> isInicioScrolled
                    currentDestination == MainDestination.Herramientas -> herramientasScrollOffset > 0f
                    currentDestination == MainDestination.Ajustes -> isAjustesScrolled
                    currentDestination == MainDestination.Copilot -> true
                    else -> false
                },
                scrollOffset = when {
                    showingMorningRoadmap -> inicioScrollOffset
                    showingScheduleDetail -> inicioScrollOffset
                    showingActivityDetail -> activityScrollOffset
                    selectedAssignmentForDraft != null -> 64f
                    currentDestination == MainDestination.Inicio -> inicioScrollOffset
                    currentDestination == MainDestination.Herramientas -> herramientasScrollOffset
                    currentDestination == MainDestination.Ajustes -> ajustesScrollOffset
                    currentDestination == MainDestination.Copilot -> if (isCopilotScrolled) copilotScrollOffset else 64f
                    else -> 0f
                },
                backdrop = contentBackdrop,
                titleAlwaysVisible = showingMorningRoadmap || showingScheduleDetail || showingActivityDetail || (selectedAssignmentForDraft != null) ||
                        (currentDestination == MainDestination.Herramientas && (showingNotasDetail || showingForosDetail || showingArchiveDetail)),
                navigationIcon = when {
                    showingMorningRoadmap -> {
                        {
                            me.joxquin.notivas.ui.components.miuix.BackNavigationIcon(
                                onClick = { dashboardViewModel.closeMorningRoadmap() }
                            )
                        }
                    }
                    showingScheduleDetail -> {
                        {
                            me.joxquin.notivas.ui.components.miuix.BackNavigationIcon(
                                onClick = { showingScheduleDetail = false }
                            )
                        }
                    }
                    showingActivityDetail -> {
                        {
                            me.joxquin.notivas.ui.components.miuix.BackNavigationIcon(
                                onClick = { showingActivityDetail = false }
                            )
                        }
                    }
                    selectedAssignmentForDraft != null -> {
                        {
                            me.joxquin.notivas.ui.components.miuix.BackNavigationIcon(
                                onClick = { dashboardViewModel.closeDraft() }
                            )
                        }
                    }
                    currentDestination == MainDestination.Herramientas && showingArchiveDetail -> {
                        {
                            me.joxquin.notivas.ui.components.miuix.BackNavigationIcon(
                                onClick = { showingArchiveDetail = false }
                            )
                        }
                    }
                    currentDestination == MainDestination.Herramientas && showingNotasDetail -> {
                        {
                            me.joxquin.notivas.ui.components.miuix.BackNavigationIcon(
                                onClick = { showingNotasDetail = false }
                            )
                        }
                    }
                    currentDestination == MainDestination.Herramientas && showingForosDetail -> {
                        {
                            me.joxquin.notivas.ui.components.miuix.BackNavigationIcon(
                                onClick = {
                                    if (forosUiState.selectedDiscussion != null) {
                                        forosViewModel.clearSelectedDiscussion()
                                    } else {
                                        showingForosDetail = false
                                    }
                                }
                            )
                        }
                    }
                    currentDestination == MainDestination.Copilot -> {
                        {
                            me.joxquin.notivas.ui.components.miuix.MiuixTopAppBarAction(
                                onClick = { copilotViewModel.showHistorySheet() }
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.History,
                                    contentDescription = "Historial",
                                    tint = MiuixTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    else -> null
                },
                actions = {
                    when {
                        showingScheduleDetail || showingActivityDetail -> {}
                        currentDestination == MainDestination.Inicio && selectedAssignmentForDraft == null && !isDashboardSearchExpanded -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                me.joxquin.notivas.ui.components.miuix.MiuixTopAppBarAction(
                                    onClick = { dashboardViewModel.refreshData() }
                                ) {
                                    top.yukonga.miuix.kmp.basic.Icon(
                                        imageVector = MiuixIcons.Refresh,
                                        contentDescription = "Sincronizar",
                                        tint = MiuixTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                me.joxquin.notivas.ui.components.miuix.MiuixTopAppBarAction(
                                    onClick = { showingScheduleDetail = true }
                                ) {
                                    top.yukonga.miuix.kmp.basic.Icon(
                                        imageVector = MiuixIcons.Weeks,
                                        contentDescription = stringResource(Res.string.schedule_screen_title),
                                        tint = MiuixTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                me.joxquin.notivas.ui.components.miuix.MiuixTopAppBarAction(
                                    onClick = { showingActivityDetail = true }
                                ) {
                                    top.yukonga.miuix.kmp.basic.Icon(
                                        imageVector = MiuixIcons.Alarm,
                                        contentDescription = stringResource(Res.string.recent_activity_title),
                                        tint = MiuixTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        selectedAssignmentForDraft != null -> {
                            val assignment = selectedAssignmentForDraft?.assignment
                            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                            val openCanvasLabel = stringResource(Res.string.task_menu_open_canvas)
                            val exploreTemplatesLabel = stringResource(Res.string.task_menu_explore_templates)
                            val copyCanvasLinkLabel = stringResource(Res.string.task_menu_copy_canvas_link)

                            val dropdownEntries = remember(assignment, openCanvasLabel, exploreTemplatesLabel, copyCanvasLinkLabel) {
                                listOf(
                                    top.yukonga.miuix.kmp.basic.DropdownEntry(
                                        items = listOf(
                                            top.yukonga.miuix.kmp.basic.DropdownItem(
                                                text = openCanvasLabel,
                                                onClick = {
                                                    assignment?.htmlUrl?.let { url ->
                                                        try { uriHandler.openUri(url) } catch (_: Exception) {}
                                                    }
                                                }
                                            ),
                                            top.yukonga.miuix.kmp.basic.DropdownItem(
                                                text = exploreTemplatesLabel,
                                                onClick = {
                                                    if (assignment != null) {
                                                        dashboardViewModel.openTemplatePicker(
                                                            courseId = assignment.courseId,
                                                            assignmentId = assignment.id,
                                                            assignmentDescription = assignment.description
                                                        )
                                                    }
                                                }
                                            ),
                                            top.yukonga.miuix.kmp.basic.DropdownItem(
                                                text = copyCanvasLinkLabel,
                                                onClick = {
                                                    assignment?.htmlUrl?.let { url ->
                                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(url))
                                                    }
                                                }
                                            )
                                        )
                                    )
                                )
                            }

                            top.yukonga.miuix.kmp.menu.WindowIconDropdownMenu(
                                entries = dropdownEntries,
                                collapseOnSelection = true
                            ) {
                                top.yukonga.miuix.kmp.basic.Icon(
                                    imageVector = MiuixIcons.Tune,
                                    contentDescription = "Options",
                                    tint = MiuixTheme.colorScheme.onSurface
                                )
                            }
                        }
                        currentDestination == MainDestination.Copilot -> {
                            me.joxquin.notivas.ui.components.miuix.MiuixTopAppBarAction(
                                onClick = { copilotViewModel.startNewSession() }
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.AddComment,
                                    contentDescription = "Nuevo Chat",
                                    tint = MiuixTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        currentDestination == MainDestination.Ajustes -> {
                            me.joxquin.notivas.ui.components.miuix.MiuixTopAppBarAction(
                                onClick = { profileViewModel.verifyToken() }
                            ) {
                                top.yukonga.miuix.kmp.basic.Icon(
                                    imageVector = MiuixIcons.Refresh,
                                    contentDescription = "Sincronizar",
                                    tint = MiuixTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        currentDestination == MainDestination.Herramientas -> {
                            if (showingForosDetail) {
                                if (forosUiState.selectedDiscussion == null) {
                                    me.joxquin.notivas.ui.components.miuix.MiuixTopAppBarAction(
                                        onClick = { forosViewModel.setShowFilterBottomSheet(true) }
                                    ) {
                                        top.yukonga.miuix.kmp.basic.Icon(
                                            imageVector = MiuixIcons.Filter,
                                            contentDescription = "Filtros y Ordenamiento",
                                            tint = MiuixTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            } else if (!showingNotasDetail && !showingArchiveDetail) {
                                me.joxquin.notivas.ui.components.miuix.MiuixTopAppBarAction(
                                    onClick = { showHerramientasTuneSheet = true }
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Outlined.Tune,
                                        contentDescription = "Ajustes de Ponderación",
                                        tint = MiuixTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        else -> {}
                    }
                },
                bottomContent = when {
                    showingActivityDetail -> {
                        {
                            me.joxquin.notivas.ui.activity.components.ActivitySearchAndFilters(
                                searchQuery = activityUiState.searchQuery,
                                onSearchQueryChange = { query: String -> activityViewModel.onSearchQueryChange(query) },
                                themeStyle = AppThemeStyle.MIUIX,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            )
                        }
                    }
                    selectedAssignmentForDraft == null && currentDestination == MainDestination.Herramientas && showingForosDetail && forosUiState.selectedDiscussion == null -> {
                        {
                            me.joxquin.notivas.ui.foros.components.ForosSearchAndFilters(
                                searchQuery = forosUiState.searchQuery,
                                onSearchQueryChange = { query: String -> forosViewModel.onSearchQueryChange(query) },
                                themeStyle = AppThemeStyle.MIUIX,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            )
                        }
                    }
                    else -> null
                },
                onNavigationClick = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        alpha = topBarAlpha
                    }
            )

            // Capa 3: FloatingNavigationBar Independiente ("Liquid Glass") - Oculto durante el borrador, actividad reciente, agenda, roadmap o búsqueda
            if (!showingMorningRoadmap && !showingScheduleDetail && !showingActivityDetail && selectedAssignmentForDraft == null && !isDashboardSearchExpanded) {
                FloatingNavigationBar(
                    currentDestination = currentDestination,
                    onNavigate = { destination ->
                        if (selectedAssignmentForDraft != null) {
                            dashboardViewModel.closeDraft()
                        }
                        showingNotasDetail = false
                        showingForosDetail = false
                        val targetIndex = destinations.indexOf(destination)
                        if (targetIndex != -1) {
                            mainPagerState.animateToPage(targetIndex)
                        }
                    },
                    backdrop = contentBackdrop,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Feedback Toast / Banner Flotante en MIUIX
            androidx.compose.animation.AnimatedVisibility(
                visible = toastMessage != null,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it / 2 },
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { it / 2 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp, start = 20.dp, end = 20.dp)
            ) {
                toastMessage?.let { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .squircleSurface(
                                color = if (msg.contains("Error", ignoreCase = true) || msg.contains("Fallo", ignoreCase = true) || msg.contains("No se pudo", ignoreCase = true))
                                    androidx.compose.ui.graphics.Color(0xFFE53935)
                                else if (msg.contains("exitosamente", ignoreCase = true) || msg.contains("listo", ignoreCase = true) || msg.contains("cancelada", ignoreCase = true))
                                    androidx.compose.ui.graphics.Color(0xFF388E3C)
                                else
                                    top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.primary,
                                cornerRadius = 18.dp
                            )
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 13.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            maxLines = 3
                        )
                    }
                }
            }
        }
    } else {
        // ─── ARQUITECTURA MATERIAL DESIGN 3 (SCAFFOLD) ────────────────────────
        var materialDestination by remember { mutableStateOf(MainDestination.Inicio) }
        fun navigateToMaterial(destination: MainDestination) {
            materialDestination = destination
        }

        // Interceptar gesto/botón Atrás del sistema para Material 3:
        BackHandler(enabled = themeStyle == AppThemeStyle.MATERIAL && (showingMorningRoadmap || showingScheduleDetail || showingActivityDetail || selectedAssignmentForDraft != null || showingNotasDetail || showingForosDetail || showingArchiveDetail || materialDestination != MainDestination.Inicio)) {
            if (showingMorningRoadmap) {
                dashboardViewModel.closeMorningRoadmap()
            } else if (showingScheduleDetail) {
                showingScheduleDetail = false
            } else if (showingActivityDetail) {
                showingActivityDetail = false
            } else if (selectedAssignmentForDraft != null) {
                dashboardViewModel.closeDraft()
            } else if (materialDestination == MainDestination.Herramientas) {
                if (showingForosDetail && forosUiState.selectedDiscussion != null) {
                    forosViewModel.clearSelectedDiscussion()
                } else if (showingNotasDetail || showingForosDetail || showingArchiveDetail) {
                    showingNotasDetail = false
                    showingForosDetail = false
                    showingArchiveDetail = false
                } else {
                    materialDestination = MainDestination.Inicio
                }
            } else {
                materialDestination = MainDestination.Inicio
            }
        }

        val isImeVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

        Scaffold(
            bottomBar = {
                NotiVasNavigationBar(
                    currentDestination = materialDestination,
                    isVisible = !isImeVisible && !showingMorningRoadmap && !showingScheduleDetail && !showingActivityDetail && selectedAssignmentForDraft == null,
                    onNavigate = { destination ->
                        if (selectedAssignmentForDraft != null) {
                            dashboardViewModel.closeDraft()
                        }
                        showingNotasDetail = false
                        showingForosDetail = false
                        materialDestination = destination
                    }
                )
            }
        ) { innerPadding ->
            val targetBottom = if (!isImeVisible && !showingMorningRoadmap && !showingScheduleDetail && !showingActivityDetail && selectedAssignmentForDraft == null) innerPadding.calculateBottomPadding() else 0.dp
            val animatedBottomPadding by androidx.compose.animation.core.animateDpAsState(
                targetValue = targetBottom,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
                label = "materialBottomPadding"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = animatedBottomPadding)
            ) {
                if (showingMorningRoadmap) {
                    me.joxquin.notivas.ui.dashboard.roadmap.MorningRoadmapScreen(
                        viewModel = dashboardViewModel,
                        onBack = { dashboardViewModel.closeMorningRoadmap() },
                        onNavigateToCopilotWithPrompt = { prompt ->
                            dashboardViewModel.closeMorningRoadmap()
                            showingNotasDetail = false
                            showingForosDetail = false
                            navigateToMaterial(MainDestination.Copilot)
                            copilotViewModel.sendMessage(prompt)
                        },
                        themeStyle = AppThemeStyle.MATERIAL,
                        lazyListState = inicioListState,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (showingScheduleDetail) {
                    me.joxquin.notivas.ui.dashboard.schedule.ScheduleScreen(
                        viewModel = dashboardViewModel,
                        themeStyle = AppThemeStyle.MATERIAL,
                        onBack = { showingScheduleDetail = false },
                        onAssignmentClick = { assignmentUi ->
                            dashboardViewModel.openDraft(assignmentUi)
                        },
                        lazyListState = inicioListState,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (showingActivityDetail) {
                    me.joxquin.notivas.ui.activity.RecentActivityScreen(
                        viewModel = activityViewModel,
                        themeStyle = AppThemeStyle.MATERIAL,
                        onBack = { showingActivityDetail = false },
                        onNavigateToAssignment = { assignmentId ->
                            showingActivityDetail = false
                            coroutineScope.launch {
                                val assignment = canvasRepository.getAssignment(assignmentId)
                                if (assignment != null) {
                                    val courseName = notasViewModel.uiState.value.courses.find { it.id == assignment.courseId }?.name ?: "Curso"
                                    dashboardViewModel.openDraft(
                                        me.joxquin.notivas.ui.dashboard.AssignmentUiModel(
                                            assignment = assignment,
                                            courseName = courseName
                                        )
                                    )
                                }
                            }
                        },
                        onNavigateToForum = { topicId ->
                            showingActivityDetail = false
                            val topic = forosUiState.discussions.find { it.id == topicId }
                            if (topic != null) {
                                forosViewModel.selectDiscussion(topic)
                                showingNotasDetail = false
                                showingArchiveDetail = false
                                showingForosDetail = true
                                navigateToMaterial(MainDestination.Herramientas)
                            }
                        },
                        onOpenUrl = { url ->
                            try {
                                uriHandler.openUri(url)
                            } catch (_: Exception) {}
                        },
                        lazyListState = activityListState,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (selectedAssignmentForDraft != null) {
                    selectedAssignmentForDraft?.let { draftAssignment ->
                        me.joxquin.notivas.ui.dashboard.draft.AssignmentDraftScreen(
                            assignmentUi = draftAssignment,
                            onBack = { dashboardViewModel.closeDraft() },
                            viewModel = dashboardViewModel,
                            themeStyle = AppThemeStyle.MATERIAL
                        )
                    }
                } else {
                    androidx.compose.animation.AnimatedContent(
                        targetState = materialDestination,
                        transitionSpec = {
                            val targetIndex = destinations.indexOf(targetState)
                            val initialIndex = destinations.indexOf(initialState)
                            val isForward = targetIndex >= initialIndex
                            if (isForward) {
                                (androidx.compose.animation.slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(250)) { it } +
                                        androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(250)))
                                    .togetherWith(
                                        androidx.compose.animation.slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(250)) { -it / 3 } +
                                                androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                                    )
                            } else {
                                (androidx.compose.animation.slideInHorizontally(animationSpec = androidx.compose.animation.core.tween(250)) { -it } +
                                        androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(250)))
                                    .togetherWith(
                                        androidx.compose.animation.slideOutHorizontally(animationSpec = androidx.compose.animation.core.tween(250)) { it / 3 } +
                                                androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                                    )
                            }
                        },
                        label = "materialDestinationTransition"
                    ) { currentScreen ->
                    when (currentScreen) {
                        MainDestination.Inicio -> {
                            InicioScreen(
                                viewModel = dashboardViewModel,
                                themeStyle = AppThemeStyle.MATERIAL,
                                onNavigateToCopilot = {
                                    showingNotasDetail = false
                                    showingForosDetail = false
                                    navigateToMaterial(MainDestination.Copilot)
                                },
                                onNavigateToHerramientas = {
                                    showingNotasDetail = false
                                    showingForosDetail = false
                                    navigateToMaterial(MainDestination.Herramientas)
                                },
                                onNavigateToAjustes = {
                                    showingNotasDetail = false
                                    showingForosDetail = false
                                    navigateToMaterial(MainDestination.Ajustes)
                                },
                                onNavigateToActivity = {
                                    showingActivityDetail = true
                                },
                                onNavigateToSchedule = {
                                    showingScheduleDetail = true
                                },
                                onNavigateToCourseProgreso = { course ->
                                    notasViewModel.selectCourse(course)
                                    showingForosDetail = false
                                    showingNotasDetail = true
                                    navigateToMaterial(MainDestination.Herramientas)
                                }
                            )

                        }

                        MainDestination.Herramientas -> {
                            me.joxquin.notivas.ui.herramientas.HerramientasScreen(
                                viewModel = notasViewModel,
                                forosViewModel = forosViewModel,
                                archiveViewModel = archiveViewModel,
                                showingNotasDetail = showingNotasDetail,
                                showingForosDetail = showingForosDetail,
                                showingArchiveDetail = showingArchiveDetail,
                                onOpenProgreso = {
                                    showingForosDetail = false
                                    showingArchiveDetail = false
                                    showingNotasDetail = true
                                },
                                onOpenForos = {
                                    showingNotasDetail = false
                                    showingArchiveDetail = false
                                    showingForosDetail = true
                                },
                                onOpenArchive = {
                                    showingNotasDetail = false
                                    showingForosDetail = false
                                    showingArchiveDetail = true
                                },
                                onOpenAssignmentDetail = { assignment ->
                                    val courseName = notasViewModel.uiState.value.courses.find { it.id == assignment.courseId }?.name ?: "Curso"
                                    dashboardViewModel.openDraft(
                                        me.joxquin.notivas.ui.dashboard.AssignmentUiModel(
                                            assignment = assignment,
                                            courseName = courseName
                                        )
                                    )
                                },
                                onOpenTuneSettings = { showHerramientasTuneSheet = true },
                                onBack = {
                                    showingNotasDetail = false
                                    showingForosDetail = false
                                    showingArchiveDetail = false
                                },
                                themeStyle = AppThemeStyle.MATERIAL,
                                lazyListState = herramientasListState
                            )
                        }

                        MainDestination.Copilot -> {
                            me.joxquin.notivas.ui.copilot.CopilotScreen(
                                viewModel = copilotViewModel,
                                themeStyle = AppThemeStyle.MATERIAL,
                                onNavigateToSettings = {
                                    showingNotasDetail = false
                                    showingForosDetail = false
                                    navigateToMaterial(MainDestination.Ajustes)
                                }
                            )
                        }

                        MainDestination.Ajustes -> {
                            me.joxquin.notivas.ui.ajustes.AjustesScreen(
                                viewModel = profileViewModel
                            )
                        }
                    }
                }

                // Feedback Toast / Banner Flotante en Material 3
                androidx.compose.animation.AnimatedVisibility(
                    visible = toastMessage != null,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it / 2 },
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { it / 2 },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 20.dp, end = 20.dp)
                ) {
                    toastMessage?.let { msg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (msg.contains("Error", ignoreCase = true) || msg.contains("Fallo", ignoreCase = true) || msg.contains("No se pudo", ignoreCase = true))
                                        androidx.compose.ui.graphics.Color(0xFFE53935)
                                    else if (msg.contains("exitosamente", ignoreCase = true) || msg.contains("listo", ignoreCase = true) || msg.contains("cancelada", ignoreCase = true))
                                        androidx.compose.ui.graphics.Color(0xFF388E3C)
                                    else
                                        androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                )
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = msg,
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 13.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                maxLines = 3
                            )
                        }
                    }
                }
            }
        }
    }
}

    activeUpdateInfo?.let { info ->
        UpdateDialog(
            updateInfo = info,
            onDismiss = { activeUpdateInfo = null }
        )
    }

    if (showHerramientasTuneSheet) {
        me.joxquin.notivas.ui.herramientas.components.GroupsSettingsBottomSheet(
            onDismiss = { showHerramientasTuneSheet = false },
            onOpenCatalogImport = { showCatalogImportDialog = true },
            onImportJson = { json ->
                notasViewModel.importGlobalGroupsFromJson(json) { result ->
                    // Result handling
                }
            },
            onRequestExportContent = { callback ->
                notasViewModel.exportGlobalGroups { json ->
                    callback(json)
                }
            },
            onResetGroups = {
                notasViewModel.resetAllSimulationGroups()
            },
            themeStyle = themeStyle
        )
    }

    if (showCatalogImportDialog) {
        me.joxquin.notivas.ui.notas.components.ImportTemplateDialog(
            onDismiss = { showCatalogImportDialog = false },
            showApplyToAllToggle = true,
            onApplyTemplate = { template, applyToAll ->
                if (applyToAll) {
                    notasViewModel.applyTemplateToAllCourses(template)
                } else {
                    notasViewModel.applyTemplate(template)
                }
                showCatalogImportDialog = false
            }
        )
    }
}
