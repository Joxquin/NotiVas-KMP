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
import me.joxquin.notivas.util.rememberFilePickerLauncher
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
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.Filter
import top.yukonga.miuix.kmp.icon.extended.Recent
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.NavCornerClipMode
import top.yukonga.miuix.kmp.nav.core.rememberNavBackStack
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import me.joxquin.notivas.ui.navigation.miuix.CrossActivityTransition
import me.joxquin.notivas.ui.navigation.miuix.LocalMiuixNavigator
import me.joxquin.notivas.ui.navigation.miuix.MiuixNavigator
import me.joxquin.notivas.ui.navigation.miuix.MiuixRoute
import me.joxquin.notivas.ui.notas.NotasScreen
import me.joxquin.notivas.ui.foros.ForosScreen
import me.joxquin.notivas.ui.activity.RecentActivityScreen
import me.joxquin.notivas.ui.dashboard.schedule.ScheduleScreen
import me.joxquin.notivas.ui.dashboard.roadmap.MorningRoadmapScreen
import me.joxquin.notivas.ui.dashboard.draft.AssignmentDraftScreen
import me.joxquin.notivas.ui.herramientas.archive.TaskHistoryArchiveScreen
import me.joxquin.notivas.ui.foros.detail.ForoDetailScreen
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.data.model.Assignment

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
            inicioListState.firstVisibleItemIndex > 0 || inicioListState.firstVisibleItemScrollOffset > 72
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

    val filePicker = rememberFilePickerLauncher(
        onFileSelected = { content ->
            if (content.isNotBlank()) {
                notasViewModel.importGlobalGroupsFromJson(content.trim()) {}
            }
        },
        onRequestSaveContent = { onReadyToWrite ->
            notasViewModel.exportGlobalGroups { jsonContent ->
                onReadyToWrite(jsonContent)
            }
        }
    )

    if (themeStyle == AppThemeStyle.MIUIX) {
        val miuixBackStack = rememberNavBackStack<MiuixRoute>(MiuixRoute.Main)
        val miuixNavigator = remember(miuixBackStack) { MiuixNavigator(miuixBackStack) }
        val navCornerRadius = rememberNavSystemCornerRadius()
        val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
        val backdropColor = MiuixTheme.colorScheme.surface

        val navDisplayEffects = remember<NavDisplayEffects>(navCornerRadius, backdropColor, isDarkTheme) {
            NavDisplayEffects(
                enableCornerClip = true,
                cornerClipRadius = if (navCornerRadius <= 0.dp) 32.dp else navCornerRadius,
                cornerClipMode = NavCornerClipMode.All,
                dimAmount = if (isDarkTheme) 0.8f else 0.2f,
                backdropColor = backdropColor,
            )
        }

        fun handleMiuixBack(): Boolean {
            val current = miuixNavigator.current()
            when (current) {
                is MiuixRoute.MorningRoadmap -> dashboardViewModel.closeMorningRoadmap()
                is MiuixRoute.AssignmentDraft -> dashboardViewModel.closeDraft()
                is MiuixRoute.ForoDetail -> forosViewModel.clearSelectedDiscussion()
                else -> {}
            }
            return miuixNavigator.pop()
        }

        // Sincronizar estado reactivo de Roadmap con la navegacion de HyperOS
        LaunchedEffect(showingMorningRoadmap) {
            if (showingMorningRoadmap) {
                if (miuixNavigator.current() !is MiuixRoute.MorningRoadmap) {
                    miuixNavigator.push(MiuixRoute.MorningRoadmap)
                }
            } else {
                if (miuixNavigator.current() is MiuixRoute.MorningRoadmap) {
                    miuixNavigator.pop()
                }
            }
        }

        // Sincronizar estado reactivo de Borrador de Tarea con la navegacion de HyperOS
        LaunchedEffect(selectedAssignmentForDraft) {
            val draft = selectedAssignmentForDraft
            if (draft != null) {
                val targetRoute = MiuixRoute.AssignmentDraft(draft.assignment.id, draft.courseName)
                if (miuixNavigator.current() != targetRoute) {
                    miuixNavigator.push(targetRoute)
                }
            } else {
                if (miuixNavigator.current() is MiuixRoute.AssignmentDraft) {
                    miuixNavigator.pop()
                }
            }
        }

        // Interceptar boton Atras fisico/gestual para HyperOS MIUIX:
        // Si hay vistas hijas apiladas en el backstack, retroceder con animacion HyperOS.
        // Si estamos en la raiz (MiuixRoute.Main), animar al Inicio si estamos en otra pestaña.
        val isMainBackHandlerEnabled by remember {
            derivedStateOf {
                miuixNavigator.canPop ||
                    (miuixNavigator.current() is MiuixRoute.Main && mainPagerState.selectedPage != 0)
            }
        }

        BackHandler(enabled = isMainBackHandlerEnabled) {
            if (miuixNavigator.canPop) {
                handleMiuixBack()
            } else if (mainPagerState.selectedPage != 0) {
                mainPagerState.animateToPage(0)
            }
        }

        CompositionLocalProvider(
            LocalMiuixNavigator provides miuixNavigator
        ) {
            NavDisplay(
                backStack = miuixBackStack,
                modifier = Modifier.fillMaxSize(),
                onBack = { handleMiuixBack() },
                transition = CrossActivityTransition,
                effects = navDisplayEffects,
            ) {
                entry<MiuixRoute.Main> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MiuixTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .layerBackdrop(contentBackdrop)
                        ) {
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
                                                miuixNavigator.push(MiuixRoute.RecentActivity)
                                            },
                                            onNavigateToSchedule = {
                                                miuixNavigator.push(MiuixRoute.Schedule)
                                            },
                                            onNavigateToRoadmap = {
                                                miuixNavigator.push(MiuixRoute.MorningRoadmap)
                                            },
                                            onOpenAssignmentDraft = { assignmentUi ->
                                                dashboardViewModel.openDraft(assignmentUi)
                                                miuixNavigator.push(
                                                    MiuixRoute.AssignmentDraft(
                                                        assignmentUi.assignment.id,
                                                        assignmentUi.courseName
                                                    )
                                                )
                                            },
                                            onNavigateToCourseProgreso = { course ->
                                                notasViewModel.selectCourse(course)
                                                miuixNavigator.push(MiuixRoute.CourseProgreso(course.id))
                                            },
                                            onNavigateToForoDetail = { topic ->
                                                forosViewModel.selectDiscussion(topic)
                                                miuixNavigator.push(MiuixRoute.ForoDetail(topic.id))
                                            }
                                        )
                                    }

                                    MainDestination.Herramientas -> {
                                        me.joxquin.notivas.ui.herramientas.HerramientasScreen(
                                            viewModel = notasViewModel,
                                            forosViewModel = forosViewModel,
                                            archiveViewModel = archiveViewModel,
                                            showingNotasDetail = false,
                                            showingForosDetail = false,
                                            showingArchiveDetail = false,
                                            onOpenProgreso = {
                                                val selectedCourseId = notasViewModel.uiState.value.selectedCourse?.id ?: 0L
                                                miuixNavigator.push(MiuixRoute.CourseProgreso(selectedCourseId))
                                            },
                                            onOpenForos = {
                                                miuixNavigator.push(MiuixRoute.ForosHub)
                                            },
                                            onOpenForoDetail = { topic ->
                                                forosViewModel.selectDiscussion(topic)
                                                miuixNavigator.push(MiuixRoute.ForoDetail(topic.id))
                                            },
                                            onOpenArchive = {
                                                miuixNavigator.push(MiuixRoute.TaskHistoryArchive)
                                            },
                                            onOpenAssignmentDetail = { assignment ->
                                                val courseName = notasViewModel.uiState.value.courses.find { it.id == assignment.courseId }?.name ?: "Curso"
                                                val assignmentUi = me.joxquin.notivas.ui.dashboard.AssignmentUiModel(
                                                    assignment = assignment,
                                                    courseName = courseName
                                                )
                                                dashboardViewModel.openDraft(assignmentUi)
                                                miuixNavigator.push(MiuixRoute.AssignmentDraft(assignment.id, courseName))
                                            },
                                            onOpenTuneSettings = { showHerramientasTuneSheet = true },
                                            onBack = {},
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

                        val copilotUiState by copilotViewModel.uiState.collectAsState()
                        val dashboardSearchStatus by dashboardViewModel.searchStatus.collectAsState()
                        val isDashboardSearchExpandingOrExpanded = currentDestination == MainDestination.Inicio && dashboardSearchStatus.shouldExpand()
                        val topBarContentAlpha = if (isDashboardSearchExpandingOrExpanded) 0f else 1f

                        // Capa 2: Barra Superior Canonica Unificada (Top Overlay) para la ruta principal
                        MiuixTopAppBar(
                            title = when (currentDestination) {
                                MainDestination.Inicio -> stringResource(Res.string.app_name)
                                MainDestination.Herramientas -> stringResource(Res.string.herramientas_title)
                                MainDestination.Copilot -> stringResource(Res.string.copilot_title)
                                MainDestination.Ajustes -> stringResource(Res.string.settings_title)
                                else -> stringResource(Res.string.app_name)
                            },
                            isScrolled = when {
                                currentDestination == MainDestination.Inicio -> isInicioScrolled
                                currentDestination == MainDestination.Herramientas -> herramientasScrollOffset > 0f
                                currentDestination == MainDestination.Ajustes -> isAjustesScrolled
                                currentDestination == MainDestination.Copilot -> true
                                else -> false
                            },
                            scrollOffset = when {
                                currentDestination == MainDestination.Inicio -> inicioScrollOffset
                                currentDestination == MainDestination.Herramientas -> herramientasScrollOffset
                                currentDestination == MainDestination.Ajustes -> ajustesScrollOffset
                                currentDestination == MainDestination.Copilot -> if (isCopilotScrolled) copilotScrollOffset else 52f
                                else -> 0f
                            },
                            backdrop = contentBackdrop,
                            titleAlwaysVisible = false,
                            navigationIcon = when (currentDestination) {
                                MainDestination.Copilot -> {
                                    {
                                        me.joxquin.notivas.ui.components.miuix.MiuixTopAppBarAction(
                                            onClick = { copilotViewModel.showHistorySheet() }
                                        ) {
                                            top.yukonga.miuix.kmp.basic.Icon(
                                                imageVector = MiuixIcons.Recent,
                                                contentDescription = "Historial",
                                                tint = MiuixTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                }
                                else -> null
                            },
                            actions = {
                                when {
                                    isDashboardSearchExpandingOrExpanded -> {}
                                    currentDestination == MainDestination.Inicio -> {
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
                                                    tint = MiuixTheme.colorScheme.onBackground
                                                )
                                            }
                                            me.joxquin.notivas.ui.components.miuix.MiuixTopAppBarAction(
                                                onClick = { miuixNavigator.push(MiuixRoute.Schedule) }
                                            ) {
                                                top.yukonga.miuix.kmp.basic.Icon(
                                                    imageVector = MiuixIcons.Weeks,
                                                    contentDescription = stringResource(Res.string.schedule_screen_title),
                                                    tint = MiuixTheme.colorScheme.onBackground
                                                )
                                            }
                                            me.joxquin.notivas.ui.components.miuix.MiuixTopAppBarAction(
                                                onClick = { miuixNavigator.push(MiuixRoute.RecentActivity) }
                                            ) {
                                                top.yukonga.miuix.kmp.basic.Icon(
                                                    imageVector = MiuixIcons.Alarm,
                                                    contentDescription = stringResource(Res.string.recent_activity_title),
                                                    tint = MiuixTheme.colorScheme.onBackground
                                                )
                                            }
                                        }
                                    }
                                    currentDestination == MainDestination.Copilot -> {
                                        me.joxquin.notivas.ui.components.miuix.MiuixTopAppBarAction(
                                            onClick = { copilotViewModel.startNewSession() }
                                        ) {
                                            top.yukonga.miuix.kmp.basic.Icon(
                                                imageVector = MiuixIcons.Create,
                                                contentDescription = "Nuevo Chat",
                                                tint = MiuixTheme.colorScheme.onBackground
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
                                                tint = MiuixTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                    currentDestination == MainDestination.Herramientas -> {
                                        val importCatalogLabel = stringResource(Res.string.groups_menu_import_catalog)
                                        val importJsonLabel = stringResource(Res.string.groups_menu_import_json)
                                        val exportJsonLabel = stringResource(Res.string.groups_menu_export_json)
                                        val resetGroupsLabel = stringResource(Res.string.groups_menu_reset_groups)

                                        val herramientasDropdownEntries = remember(importCatalogLabel, importJsonLabel, exportJsonLabel, resetGroupsLabel) {
                                            listOf(
                                                top.yukonga.miuix.kmp.basic.DropdownEntry(
                                                    items = listOf(
                                                        top.yukonga.miuix.kmp.basic.DropdownItem(
                                                            text = importCatalogLabel,
                                                            onClick = { showCatalogImportDialog = true }
                                                        ),
                                                        top.yukonga.miuix.kmp.basic.DropdownItem(
                                                            text = importJsonLabel,
                                                            onClick = { filePicker.launchFilePicker() }
                                                        ),
                                                        top.yukonga.miuix.kmp.basic.DropdownItem(
                                                            text = exportJsonLabel,
                                                            onClick = { filePicker.launchFileCreator("notivas_ponderacion.json") }
                                                        ),
                                                        top.yukonga.miuix.kmp.basic.DropdownItem(
                                                            text = resetGroupsLabel,
                                                            onClick = { notasViewModel.resetAllSimulationGroups() }
                                                        )
                                                    )
                                                )
                                            )
                                        }

                                        top.yukonga.miuix.kmp.menu.WindowIconDropdownMenu(
                                            entries = herramientasDropdownEntries,
                                            collapseOnSelection = true
                                        ) {
                                            top.yukonga.miuix.kmp.basic.Icon(
                                                imageVector = MiuixIcons.Tune,
                                                contentDescription = stringResource(Res.string.groups_menu_options_title),
                                                tint = MiuixTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                    else -> {}
                                }
                            },
                            bottomContent = null,
                            onNavigationClick = null,
                            contentAlpha = topBarContentAlpha,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )

                        // Capa 3: FloatingNavigationBar ("Liquid Glass")
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !isDashboardSearchExpandingOrExpanded,
                            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(200)) +
                                    androidx.compose.animation.slideInVertically(animationSpec = androidx.compose.animation.core.tween(300)) { it },
                            exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) +
                                    androidx.compose.animation.slideOutVertically(animationSpec = androidx.compose.animation.core.tween(250)) { it },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            FloatingNavigationBar(
                                currentDestination = currentDestination,
                                onNavigate = { destination ->
                                    showingNotasDetail = false
                                    showingForosDetail = false
                                    val targetIndex = destinations.indexOf(destination)
                                    if (targetIndex != -1) {
                                        mainPagerState.animateToPage(targetIndex)
                                    }
                                },
                                backdrop = contentBackdrop
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
                                                MiuixTheme.colorScheme.primary,
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
                }

                entry<MiuixRoute.RecentActivity>(swipeDismiss = NavSwipeDirection.LeftToRight) {
                    RecentActivityScreen(
                        viewModel = activityViewModel,
                        themeStyle = AppThemeStyle.MIUIX,
                        lazyListState = activityListState,
                        onBack = { handleMiuixBack() },
                        onNavigateToAssignment = { assignmentId: Long ->
                            coroutineScope.launch {
                                val assignment = canvasRepository.getAssignment(assignmentId)
                                if (assignment != null) {
                                    val courseName = notasViewModel.uiState.value.courses.find { it.id == assignment.courseId }?.name ?: "Curso"
                                    val assignmentUi = AssignmentUiModel(
                                        assignment = assignment,
                                        courseName = courseName
                                    )
                                    dashboardViewModel.openDraft(assignmentUi)
                                    miuixNavigator.push(MiuixRoute.AssignmentDraft(assignment.id, courseName))
                                }
                            }
                        },
                        onNavigateToForum = { topicId: Long ->
                            val topic = forosUiState.discussions.find { it.id == topicId }
                            if (topic != null) {
                                forosViewModel.selectDiscussion(topic)
                                miuixNavigator.push(MiuixRoute.ForoDetail(topic.id))
                            }
                        },
                        onOpenUrl = { url: String ->
                            try {
                                uriHandler.openUri(url)
                            } catch (_: Exception) {}
                        }
                    )
                }

                entry<MiuixRoute.Schedule>(swipeDismiss = NavSwipeDirection.LeftToRight) {
                    ScheduleScreen(
                        viewModel = dashboardViewModel,
                        themeStyle = AppThemeStyle.MIUIX,
                        onBack = { handleMiuixBack() },
                        onAssignmentClick = { assignmentUi: AssignmentUiModel ->
                            dashboardViewModel.openDraft(assignmentUi)
                            miuixNavigator.push(MiuixRoute.AssignmentDraft(assignmentUi.assignment.id, assignmentUi.courseName))
                        }
                    )
                }

                entry<MiuixRoute.MorningRoadmap>(swipeDismiss = NavSwipeDirection.LeftToRight) {
                    MorningRoadmapScreen(
                        viewModel = dashboardViewModel,
                        onBack = { handleMiuixBack() },
                        onNavigateToCopilotWithPrompt = { prompt: String ->
                            handleMiuixBack()
                            mainPagerState.animateToPage(2)
                            copilotViewModel.sendMessage(prompt)
                        },
                        onOpenDraft = { assignmentUi ->
                            dashboardViewModel.openDraft(assignmentUi)
                            miuixNavigator.push(
                                MiuixRoute.AssignmentDraft(
                                    assignmentUi.assignment.id,
                                    assignmentUi.courseName
                                )
                            )
                        },
                        themeStyle = AppThemeStyle.MIUIX
                    )
                }

                entry<MiuixRoute.AssignmentDraft>(swipeDismiss = NavSwipeDirection.LeftToRight) { route: MiuixRoute.AssignmentDraft ->
                    val currentDraft = selectedAssignmentForDraft ?: remember(route) {
                        dashboardViewModel.allAssignmentsUi.value.find { it.assignment.id == route.assignmentId }
                    }
                    if (currentDraft != null) {
                        AssignmentDraftScreen(
                            assignmentUi = currentDraft,
                            onBack = { handleMiuixBack() },
                            viewModel = dashboardViewModel,
                            themeStyle = AppThemeStyle.MIUIX
                        )
                    }
                }

                entry<MiuixRoute.TaskHistoryArchive>(swipeDismiss = NavSwipeDirection.LeftToRight) {
                    TaskHistoryArchiveScreen(
                        viewModel = archiveViewModel,
                        themeStyle = AppThemeStyle.MIUIX,
                        onBack = { handleMiuixBack() },
                        onOpenAssignmentDetail = { assignment: Assignment ->
                            val courseName = notasViewModel.uiState.value.courses.find { it.id == assignment.courseId }?.name ?: "Curso"
                            val assignmentUi = AssignmentUiModel(
                                assignment = assignment,
                                courseName = courseName
                            )
                            dashboardViewModel.openDraft(assignmentUi)
                            miuixNavigator.push(MiuixRoute.AssignmentDraft(assignment.id, courseName))
                        }
                    )
                }

                entry<MiuixRoute.ForoDetail>(swipeDismiss = NavSwipeDirection.LeftToRight) { route: MiuixRoute.ForoDetail ->
                    val topic = forosUiState.discussions.find { it.id == route.topicId } ?: forosUiState.selectedDiscussion
                    if (topic != null) {
                        ForoDetailScreen(
                            viewModel = forosViewModel,
                            themeStyle = AppThemeStyle.MIUIX,
                            onBack = { handleMiuixBack() }
                        )
                    }
                }

                entry<MiuixRoute.CourseProgreso>(swipeDismiss = NavSwipeDirection.LeftToRight) {
                    NotasScreen(
                        viewModel = notasViewModel,
                        themeStyle = AppThemeStyle.MIUIX,
                        onBack = { handleMiuixBack() }
                    )
                }

                entry<MiuixRoute.ForosHub>(swipeDismiss = NavSwipeDirection.LeftToRight) {
                    ForosScreen(
                        viewModel = forosViewModel,
                        themeStyle = AppThemeStyle.MIUIX,
                        onBack = { handleMiuixBack() },
                        onOpenTopic = { topic ->
                            forosViewModel.selectDiscussion(topic)
                            miuixNavigator.push(MiuixRoute.ForoDetail(topic.id))
                        }
                    )
                }
            }
        }
    } else {
        // ─── ARQUITECTURA MATERIAL DESIGN 3 (SCAFFOLD) ────────────────────────
        var materialDestination by remember { mutableStateOf(MainDestination.Inicio) }
        fun navigateToMaterial(destination: MainDestination) {
            materialDestination = destination
        }

        val dashboardSearchStatus by dashboardViewModel.searchStatus.collectAsState()
        val isMaterialDashboardSearchExpandingOrExpanded = materialDestination == MainDestination.Inicio && dashboardSearchStatus.shouldExpand()

        // Interceptar gesto/botón Atrás del sistema para Material 3:
        BackHandler(
            enabled = themeStyle == AppThemeStyle.MATERIAL && (
                isMaterialDashboardSearchExpandingOrExpanded ||
                showingMorningRoadmap ||
                showingScheduleDetail ||
                showingActivityDetail ||
                selectedAssignmentForDraft != null ||
                showingNotasDetail ||
                showingForosDetail ||
                showingArchiveDetail ||
                materialDestination != MainDestination.Inicio
            )
        ) {
            if (isMaterialDashboardSearchExpandingOrExpanded) {
                dashboardViewModel.setSearchExpanded(false)
            } else if (showingMorningRoadmap) {
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
        val isMaterialBottomBarVisible = !isImeVisible && !showingMorningRoadmap && !showingScheduleDetail && !showingActivityDetail && selectedAssignmentForDraft == null && !isMaterialDashboardSearchExpandingOrExpanded

        Scaffold(
            bottomBar = {
                NotiVasNavigationBar(
                    currentDestination = materialDestination,
                    isVisible = isMaterialBottomBarVisible,
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
            val targetBottom = if (isMaterialBottomBarVisible) innerPadding.calculateBottomPadding() else 0.dp
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
                                },
                                onNavigateToForoDetail = { topic ->
                                    forosViewModel.selectDiscussion(topic)
                                    showingNotasDetail = false
                                    showingArchiveDetail = false
                                    showingForosDetail = true
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
                                onOpenCatalogImport = { showCatalogImportDialog = true },
                                onImportJsonClick = { filePicker.launchFilePicker() },
                                onExportJsonClick = { filePicker.launchFileCreator("notivas_ponderacion.json") },
                                onResetGroups = { notasViewModel.resetAllSimulationGroups() },
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
