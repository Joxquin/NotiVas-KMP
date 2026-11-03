package me.joxquin.notivas.ui

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.runtime.CompositionLocalProvider
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.NavCornerClipMode
import top.yukonga.miuix.kmp.nav.core.rememberNavBackStack
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import me.joxquin.notivas.ui.components.miuix.MiuixPersistentNavigationRail
import me.joxquin.notivas.ui.navigation.NotiVasNavigationRail
import me.joxquin.notivas.ui.util.LocalIsWideScreen
import me.joxquin.notivas.ui.util.shouldShowSplitPane
import me.joxquin.notivas.ui.navigation.miuix.CrossActivityTransition
import me.joxquin.notivas.ui.navigation.miuix.LocalMiuixNavigator
import me.joxquin.notivas.ui.navigation.miuix.LocalMiuixSnackbarHostState
import me.joxquin.notivas.ui.navigation.miuix.MiuixNavigator
import me.joxquin.notivas.ui.navigation.miuix.MiuixRoute
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import me.joxquin.notivas.ui.notas.NotasScreen
import me.joxquin.notivas.ui.foros.ForosScreen
import me.joxquin.notivas.ui.activity.RecentActivityScreen
import me.joxquin.notivas.ui.dashboard.schedule.ScheduleScreen
import me.joxquin.notivas.ui.dashboard.draft.AssignmentDraftScreen
import me.joxquin.notivas.ui.herramientas.archive.TaskHistoryArchiveScreen
import me.joxquin.notivas.ui.foros.detail.ForoDetailScreen
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.ui.course.CourseDetailScreen
import me.joxquin.notivas.ui.course.CourseDetailViewModel
private val MiuixPagerSpringSpec: SpringSpec<Float> = spring(
    stiffness = 322.2f,
    dampingRatio = 32.31f / (2f * kotlin.math.sqrt(322.2f)),
    visibilityThreshold = 0.5f,
)

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
                if (targetIndex in 0 until pagerState.pageCount) {
                    pagerState.scroll(MutatePriority.UserInput) {
                        val destination = targetIndex.coerceIn(0, pagerState.pageCount - 1)
                        val pageSize = pagerState.layoutInfo.pageSize + pagerState.layoutInfo.pageSpacing
                        if (pageSize > 0) {
                            val distance = (destination - pagerState.currentPage - pagerState.currentPageOffsetFraction) * pageSize
                            var previousValue = 0f
                            animate(
                                initialValue = 0f,
                                targetValue = distance,
                                animationSpec = MiuixPagerSpringSpec,
                            ) { currentValue, _ ->
                                previousValue += scrollBy(currentValue - previousValue)
                            }
                        }
                    }
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
    preferencesManager: PreferencesManager,
    isEntranceTriggered: Boolean = true
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

    LaunchedEffect(mainPagerState.pagerState.currentPage) {
        mainPagerState.syncPage()
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
    val miuixSnackbarHostState = remember { SnackbarHostState() }

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
    val activityListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val herramientasListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val ajustesListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val copilotListState = androidx.compose.foundation.lazy.rememberLazyListState()

    val surfaceColor = MiuixTheme.colorScheme.surface
    val contentBackdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
    var showingNotasDetail by remember { mutableStateOf(false) }
    var showingForosDetail by remember { mutableStateOf(false) }
    var showingArchiveDetail by remember { mutableStateOf(false) }
    var showingActivityDetail by remember { mutableStateOf(false) }
    var showingScheduleDetail by remember { mutableStateOf(false) }
    var selectedCourseForDetail by remember { mutableStateOf<Course?>(null) }
    var selectedCourseForModules by remember { mutableStateOf<Course?>(null) }
    var showHerramientasTuneSheet by remember { mutableStateOf(false) }
    var showCatalogImportDialog by remember { mutableStateOf(false) }

    val forosUiState by forosViewModel.uiState.collectAsState()
    val activityUiState by activityViewModel.uiState.collectAsState()

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

    val isWideScreen = shouldShowSplitPane()

    if (themeStyle == AppThemeStyle.MIUIX) {
        val miuixBackStack = rememberNavBackStack<MiuixRoute>(MiuixRoute.Main)
        val miuixNavigator = remember(miuixBackStack) { MiuixNavigator(miuixBackStack) }
        val navCornerRadius = if (isWideScreen) 0.dp else rememberNavSystemCornerRadius()
        val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
        val backdropColor = MiuixTheme.colorScheme.surface

        val navDisplayEffects = remember<NavDisplayEffects>(navCornerRadius, backdropColor, isDarkTheme, isWideScreen) {
            NavDisplayEffects(
                enableCornerClip = !isWideScreen && navCornerRadius > 0.dp,
                cornerClipRadius = if (navCornerRadius <= 0.dp) 0.dp else navCornerRadius,
                cornerClipMode = NavCornerClipMode.Leading,
                dimAmount = 0.35f,
                backdropColor = backdropColor,
            )
        }

        fun handleMiuixBack(): Boolean {
            val current = miuixNavigator.current()
            when (current) {
                is MiuixRoute.AssignmentDraft -> dashboardViewModel.closeDraft()
                is MiuixRoute.ForoDetail -> forosViewModel.clearSelectedDiscussion()
                else -> {}
            }
            return miuixNavigator.pop()
        }


        // Interceptar boton Atras fisico/gestual para HyperOS MIUIX:
        // IMPORTANTE: Unicamente habilitado cuando estemos en la ruta raíz (MiuixRoute.Main) y en una pestaña
        // distinta de la primera (Inicio). Cuando haya pantallas apiladas (miuixNavigator.canPop == true),
        // este callback DEBE estar deshabilitado para que el NavDisplay (PredictiveBackHandlerWithSessions)
        // reciba los eventos del gesto predictivo del sistema de Android y ejecute la animación 1:1.
        val isMainBackHandlerEnabled by remember {
            derivedStateOf {
                miuixNavigator.current() is MiuixRoute.Main && !miuixNavigator.canPop && mainPagerState.selectedPage != 0
            }
        }

        BackHandler(enabled = isMainBackHandlerEnabled) {
            if (mainPagerState.selectedPage != 0) {
                mainPagerState.animateToPage(0)
            }
        }

        val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
        val swipeBackDirection = if (layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl) {
            NavSwipeDirection.RightToLeft
        } else {
            NavSwipeDirection.LeftToRight
        }

        CompositionLocalProvider(
            LocalMiuixNavigator provides miuixNavigator,
            LocalMiuixSnackbarHostState provides miuixSnackbarHostState,
            LocalIsWideScreen provides isWideScreen,
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (isWideScreen) {
                    MiuixPersistentNavigationRail(
                        destinations = destinations,
                        currentDestination = currentDestination,
                        onNavigate = { destination ->
                            showingNotasDetail = false
                            showingForosDetail = false
                            miuixNavigator.popUntil { it is MiuixRoute.Main }
                            val targetIndex = destinations.indexOf(destination)
                            if (targetIndex != -1) {
                                mainPagerState.animateToPage(targetIndex)
                            }
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(if (isWideScreen) Modifier.clipToBounds() else Modifier)
                ) {
                    NavDisplay(
                        backStack = miuixBackStack,
                        modifier = Modifier.fillMaxSize(),
                        onBack = { handleMiuixBack() },
                        transition = NavTransitions.MiuixDefault,
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
                                            onNavigateToAcademicPerformance = {
                                                miuixNavigator.push(MiuixRoute.AcademicPerformance)
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
                                            onNavigateToCourseDetail = { course ->
                                                miuixNavigator.push(MiuixRoute.CourseDetail(course.id))
                                            },
                                            onNavigateToForoDetail = { topic ->
                                                forosViewModel.selectDiscussion(topic)
                                                miuixNavigator.push(MiuixRoute.ForoDetail(topic.id))
                                            },
                                            isEntranceTriggered = isEntranceTriggered
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
                                            },
                                            onOpenAssignmentDraft = { assignmentId, courseName ->
                                                coroutineScope.launch {
                                                    val assignmentUi = dashboardViewModel.allAssignmentsUi.value.find { it.assignment.id == assignmentId }
                                                        ?: canvasRepository.getAssignment(assignmentId)?.let {
                                                            me.joxquin.notivas.ui.dashboard.AssignmentUiModel(it, courseName)
                                                        }
                                                    if (assignmentUi != null) {
                                                        dashboardViewModel.openDraft(assignmentUi)
                                                        miuixNavigator.push(MiuixRoute.AssignmentDraft(assignmentId, courseName))
                                                    }
                                                }
                                            },
                                            onOpenForoDetail = { topicId ->
                                                miuixNavigator.push(MiuixRoute.ForoDetail(topicId))
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

                        val dashboardSearchStatus by dashboardViewModel.searchStatus.collectAsState()
                        val isDashboardSearchExpandingOrExpanded = currentDestination == MainDestination.Inicio && dashboardSearchStatus.shouldExpand()

                        // Capa 3: FloatingNavigationBar ("Liquid Glass")
                        if (!isWideScreen) {
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

                entry<MiuixRoute.RecentActivity>(swipeDismiss = swipeBackDirection) {
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

                entry<MiuixRoute.Schedule>(swipeDismiss = swipeBackDirection) {
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

                entry<MiuixRoute.AssignmentDraft>(swipeDismiss = swipeBackDirection) { route: MiuixRoute.AssignmentDraft ->
                    val selectedAssignmentForDraft by dashboardViewModel.selectedAssignmentForDraft.collectAsState()
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

                entry<MiuixRoute.TaskHistoryArchive>(swipeDismiss = swipeBackDirection) {
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

                entry<MiuixRoute.ForoDetail>(swipeDismiss = swipeBackDirection) { route: MiuixRoute.ForoDetail ->
                    LaunchedEffect(route.topicId) {
                        if (forosUiState.selectedDiscussion?.id != route.topicId) {
                            val existing = forosUiState.discussions.find { it.id == route.topicId }
                            if (existing != null) {
                                forosViewModel.selectDiscussion(existing)
                            }
                        }
                    }
                    ForoDetailScreen(
                        viewModel = forosViewModel,
                        themeStyle = AppThemeStyle.MIUIX,
                        onBack = { handleMiuixBack() }
                    )
                }

                entry<MiuixRoute.CourseProgreso>(swipeDismiss = swipeBackDirection) { route: MiuixRoute.CourseProgreso ->
                    LaunchedEffect(route.courseId) {
                        if (route.courseId != 0L && notasViewModel.uiState.value.selectedCourse?.id != route.courseId) {
                            val target = notasViewModel.uiState.value.courses.find { it.id == route.courseId }
                            if (target != null) {
                                notasViewModel.selectCourse(target)
                            }
                        }
                    }
                    NotasScreen(
                        viewModel = notasViewModel,
                        themeStyle = AppThemeStyle.MIUIX,
                        onBack = { handleMiuixBack() }
                    )
                }

                entry<MiuixRoute.ForosHub>(swipeDismiss = swipeBackDirection) {
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

                entry<MiuixRoute.CourseDetail>(swipeDismiss = swipeBackDirection) { route: MiuixRoute.CourseDetail ->
                    val detailViewModel = remember(route.courseId) {
                        CourseDetailViewModel(
                            courseId = route.courseId,
                            repository = canvasRepository
                        )
                    }
                    CourseDetailScreen(
                        viewModel = detailViewModel,
                        themeStyle = AppThemeStyle.MIUIX,
                        onBack = { handleMiuixBack() },
                        onNavigateToAssignmentDraft = { assignmentId, courseName ->
                            coroutineScope.launch {
                                val assignment = canvasRepository.getAssignment(assignmentId)
                                if (assignment != null) {
                                    val assignmentUi = AssignmentUiModel(assignment, courseName)
                                    dashboardViewModel.openDraft(assignmentUi)
                                    miuixNavigator.push(MiuixRoute.AssignmentDraft(assignment.id, courseName))
                                }
                            }
                        },
                        onNavigateToForoDetail = { topicId ->
                            val topic = forosUiState.discussions.find { it.id == topicId }
                            if (topic != null) {
                                forosViewModel.selectDiscussion(topic)
                                miuixNavigator.push(MiuixRoute.ForoDetail(topic.id))
                            } else {
                                coroutineScope.launch {
                                    val fetched = canvasRepository.getDiscussionTopic(route.courseId, topicId)
                                    if (fetched != null) {
                                        forosViewModel.selectDiscussion(fetched)
                                        miuixNavigator.push(MiuixRoute.ForoDetail(fetched.id))
                                    }
                                }
                            }
                        },
                        onNavigateToProgreso = {
                            val course = notasViewModel.uiState.value.courses.find { it.id == route.courseId }
                            if (course != null) {
                                notasViewModel.selectCourse(course)
                            }
                            miuixNavigator.push(MiuixRoute.CourseProgreso(route.courseId))
                        },
                        onNavigateToCopilot = { prompt ->
                            handleMiuixBack()
                            mainPagerState.animateToPage(2)
                            copilotViewModel.sendMessage(prompt)
                        },
                        onNavigateToAnnouncements = {
                            miuixNavigator.push(MiuixRoute.ForosHub)
                        },
                        onNavigateToAllModules = {
                            miuixNavigator.push(MiuixRoute.CourseModules(route.courseId))
                        },
                        onOpenUrl = { url ->
                            try {
                                uriHandler.openUri(url)
                            } catch (_: Exception) {}
                        }
                    )
                }

                entry<MiuixRoute.CourseModules>(swipeDismiss = swipeBackDirection) { route: MiuixRoute.CourseModules ->
                    val detailViewModel = remember(route.courseId) {
                        CourseDetailViewModel(
                            courseId = route.courseId,
                            repository = canvasRepository
                        )
                    }
                    CourseModulesScreen(
                        viewModel = detailViewModel,
                        themeStyle = AppThemeStyle.MIUIX,
                        onBack = { handleMiuixBack() },
                        onNavigateToAssignmentDraft = { assignmentId, courseName ->
                            coroutineScope.launch {
                                val assignment = canvasRepository.getAssignment(assignmentId)
                                if (assignment != null) {
                                    val assignmentUi = AssignmentUiModel(assignment, courseName)
                                    dashboardViewModel.openDraft(assignmentUi)
                                    miuixNavigator.push(MiuixRoute.AssignmentDraft(assignment.id, courseName))
                                }
                            }
                        },
                        onNavigateToForoDetail = { topicId ->
                            val topic = forosUiState.discussions.find { it.id == topicId }
                            if (topic != null) {
                                forosViewModel.selectDiscussion(topic)
                                miuixNavigator.push(MiuixRoute.ForoDetail(topic.id))
                            } else {
                                coroutineScope.launch {
                                    val fetched = canvasRepository.getDiscussionTopic(route.courseId, topicId)
                                    if (fetched != null) {
                                        forosViewModel.selectDiscussion(fetched)
                                        miuixNavigator.push(MiuixRoute.ForoDetail(fetched.id))
                                    }
                                }
                            }
                        },
                        onOpenUrl = { url ->
                            try {
                                uriHandler.openUri(url)
                            } catch (_: Exception) {}
                        }
                    )
                }

                entry<MiuixRoute.AcademicPerformance>(swipeDismiss = swipeBackDirection) {
                    me.joxquin.notivas.ui.dashboard.performance.AcademicPerformanceScreen(
                        viewModel = dashboardViewModel,
                        themeStyle = AppThemeStyle.MIUIX,
                        onBack = { handleMiuixBack() },
                        onNavigateToCourseProgreso = { courseId ->
                            coroutineScope.launch {
                                val course = canvasRepository.getCourse(courseId)
                                if (course != null) notasViewModel.selectCourse(course)
                                miuixNavigator.push(MiuixRoute.CourseProgreso(courseId))
                            }
                        },
                        onNavigateToAssignmentDraft = { assignmentId, courseName ->
                            coroutineScope.launch {
                                val assignment = canvasRepository.getAssignment(assignmentId)
                                if (assignment != null) {
                                    val assignmentUi = AssignmentUiModel(assignment, courseName)
                                    dashboardViewModel.openDraft(assignmentUi)
                                    miuixNavigator.push(MiuixRoute.AssignmentDraft(assignment.id, courseName))
                                }
                            }
                        }
                    )
                }
            }

            SnackbarHost(
                state = miuixSnackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (!isWideScreen) 88.dp else 24.dp)
            )
        }
    }
}
} else {
        // ─── ARQUITECTURA MATERIAL DESIGN 3 (AOSP PREDICTIVE BACK & NAVDISPLAY) ───
        val materialBackStack = rememberNavBackStack<MiuixRoute>(MiuixRoute.Main)
        val materialNavigator = remember(materialBackStack) { MiuixNavigator(materialBackStack) }

        var materialDestination by remember { mutableStateOf(MainDestination.Inicio) }
        fun navigateToMaterial(destination: MainDestination) {
            materialDestination = destination
        }

        val dashboardSearchStatus by dashboardViewModel.searchStatus.collectAsState()
        val isMaterialDashboardSearchExpandingOrExpanded = materialDestination == MainDestination.Inicio && dashboardSearchStatus.shouldExpand()

        fun handleMaterialBack(): Boolean {
            val current = materialNavigator.current()
            when (current) {
                is MiuixRoute.AssignmentDraft -> dashboardViewModel.closeDraft()
                is MiuixRoute.ForoDetail -> forosViewModel.clearSelectedDiscussion()
                else -> {}
            }
            return materialNavigator.pop()
        }

        val isMaterialMainBackHandlerEnabled by remember {
            derivedStateOf {
                materialNavigator.current() is MiuixRoute.Main && !materialNavigator.canPop && (
                    isMaterialDashboardSearchExpandingOrExpanded || materialDestination != MainDestination.Inicio
                )
            }
        }

        BackHandler(enabled = isMaterialMainBackHandlerEnabled) {
            if (isMaterialDashboardSearchExpandingOrExpanded) {
                dashboardViewModel.setSearchExpanded(false)
            } else if (materialDestination != MainDestination.Inicio) {
                materialDestination = MainDestination.Inicio
            }
        }

        val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
        val materialNavCornerRadius = if (isWideScreen) 0.dp else rememberNavSystemCornerRadius()
        val materialAospEffects = remember(isDarkTheme, materialNavCornerRadius, isWideScreen) {
            NavDisplayEffects(
                enableCornerClip = !isWideScreen,
                cornerClipRadius = if (materialNavCornerRadius <= 0.dp) 32.dp else materialNavCornerRadius,
                cornerClipMode = NavCornerClipMode.All,
                dimAmount = if (isDarkTheme) 0.8f else 0.2f,
            )
        }

        val materialLayoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
        val materialSwipeBackDirection = if (materialLayoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl) {
            NavSwipeDirection.RightToLeft
        } else {
            NavSwipeDirection.LeftToRight
        }

        CompositionLocalProvider(
            LocalMiuixNavigator provides materialNavigator,
            LocalMiuixSnackbarHostState provides miuixSnackbarHostState,
            LocalIsWideScreen provides isWideScreen,
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (isWideScreen) {
                    NotiVasNavigationRail(
                        currentDestination = materialDestination,
                        onNavigate = { destination ->
                            materialNavigator.popUntil { it is MiuixRoute.Main }
                            navigateToMaterial(destination)
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(if (isWideScreen) Modifier.clipToBounds() else Modifier)
                ) {
                    NavDisplay(
                        backStack = materialBackStack,
                        modifier = Modifier.fillMaxSize(),
                        onBack = { handleMaterialBack() },
                        transition = CrossActivityTransition,
                        effects = materialAospEffects,
                    ) {
                        entry<MiuixRoute.Main> {
                            val isImeVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp
                            val isMaterialBottomBarVisible = !isWideScreen && !isImeVisible && !isMaterialDashboardSearchExpandingOrExpanded

                            Scaffold(
                                bottomBar = {
                                    if (!isWideScreen) {
                                        NotiVasNavigationBar(
                                            currentDestination = materialDestination,
                                            isVisible = isMaterialBottomBarVisible,
                                            onNavigate = { destination ->
                                                materialDestination = destination
                                            }
                                        )
                                    }
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
                                            listState = inicioListState,
                                            onNavigateToCopilot = {
                                                navigateToMaterial(MainDestination.Copilot)
                                            },
                                            onNavigateToHerramientas = {
                                                navigateToMaterial(MainDestination.Herramientas)
                                            },
                                            onNavigateToAjustes = {
                                                navigateToMaterial(MainDestination.Ajustes)
                                            },
                                            onNavigateToActivity = {
                                                materialNavigator.push(MiuixRoute.RecentActivity)
                                            },
                                            onNavigateToSchedule = {
                                                materialNavigator.push(MiuixRoute.Schedule)
                                            },
                                            onNavigateToAcademicPerformance = {
                                                materialNavigator.push(MiuixRoute.AcademicPerformance)
                                            },
                                            onOpenAssignmentDraft = { assignmentUi ->
                                                dashboardViewModel.openDraft(assignmentUi)
                                                materialNavigator.push(
                                                    MiuixRoute.AssignmentDraft(
                                                        assignmentUi.assignment.id,
                                                        assignmentUi.courseName
                                                    )
                                                )
                                            },
                                            onNavigateToCourseProgreso = { course ->
                                                notasViewModel.selectCourse(course)
                                                materialNavigator.push(MiuixRoute.CourseProgreso(course.id))
                                            },
                                            onNavigateToCourseDetail = { course ->
                                                materialNavigator.push(MiuixRoute.CourseDetail(course.id))
                                            },
                                            onNavigateToForoDetail = { topic ->
                                                forosViewModel.selectDiscussion(topic)
                                                materialNavigator.push(MiuixRoute.ForoDetail(topic.id))
                                            },
                                            isEntranceTriggered = isEntranceTriggered
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
                                                materialNavigator.push(MiuixRoute.CourseProgreso(selectedCourseId))
                                            },
                                            onOpenForos = {
                                                materialNavigator.push(MiuixRoute.ForosHub)
                                            },
                                            onOpenForoDetail = { topic ->
                                                forosViewModel.selectDiscussion(topic)
                                                materialNavigator.push(MiuixRoute.ForoDetail(topic.id))
                                            },
                                            onOpenArchive = {
                                                materialNavigator.push(MiuixRoute.TaskHistoryArchive)
                                            },
                                            onOpenAssignmentDetail = { assignment ->
                                                val courseName = notasViewModel.uiState.value.courses.find { it.id == assignment.courseId }?.name ?: "Curso"
                                                val assignmentUi = me.joxquin.notivas.ui.dashboard.AssignmentUiModel(
                                                    assignment = assignment,
                                                    courseName = courseName
                                                )
                                                dashboardViewModel.openDraft(assignmentUi)
                                                materialNavigator.push(MiuixRoute.AssignmentDraft(assignment.id, courseName))
                                            },
                                            onOpenTuneSettings = { showHerramientasTuneSheet = true },
                                            onOpenCatalogImport = { showCatalogImportDialog = true },
                                            onImportJsonClick = { filePicker.launchFilePicker() },
                                            onExportJsonClick = { filePicker.launchFileCreator("notivas_ponderacion.json") },
                                            onResetGroups = { notasViewModel.resetAllSimulationGroups() },
                                            onBack = { handleMaterialBack() },
                                            themeStyle = AppThemeStyle.MATERIAL,
                                            lazyListState = herramientasListState
                                        )
                                    }

                                    MainDestination.Copilot -> {
                                        me.joxquin.notivas.ui.copilot.CopilotScreen(
                                            viewModel = copilotViewModel,
                                            themeStyle = AppThemeStyle.MATERIAL,
                                            onNavigateToSettings = {
                                                navigateToMaterial(MainDestination.Ajustes)
                                            },
                                            onOpenAssignmentDraft = { assignmentId, courseName ->
                                                coroutineScope.launch {
                                                    val assignmentUi = dashboardViewModel.allAssignmentsUi.value.find { it.assignment.id == assignmentId }
                                                        ?: canvasRepository.getAssignment(assignmentId)?.let {
                                                            me.joxquin.notivas.ui.dashboard.AssignmentUiModel(it, courseName)
                                                        }
                                                    if (assignmentUi != null) {
                                                        dashboardViewModel.openDraft(assignmentUi)
                                                        materialNavigator.push(MiuixRoute.AssignmentDraft(assignmentId, courseName))
                                                    }
                                                }
                                            },
                                            onOpenForoDetail = { topicId ->
                                                materialNavigator.push(MiuixRoute.ForoDetail(topicId))
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

                entry<MiuixRoute.RecentActivity>(swipeDismiss = materialSwipeBackDirection) {
                    RecentActivityScreen(
                        viewModel = activityViewModel,
                        themeStyle = AppThemeStyle.MATERIAL,
                        lazyListState = activityListState,
                        onBack = { handleMaterialBack() },
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
                                    materialNavigator.push(MiuixRoute.AssignmentDraft(assignment.id, courseName))
                                }
                            }
                        },
                        onNavigateToForum = { topicId: Long ->
                            val topic = forosUiState.discussions.find { it.id == topicId }
                            if (topic != null) {
                                forosViewModel.selectDiscussion(topic)
                                materialNavigator.push(MiuixRoute.ForoDetail(topic.id))
                            }
                        },
                        onOpenUrl = { url: String ->
                            try {
                                uriHandler.openUri(url)
                            } catch (_: Exception) {}
                        }
                    )
                }

                entry<MiuixRoute.Schedule>(swipeDismiss = materialSwipeBackDirection) {
                    ScheduleScreen(
                        viewModel = dashboardViewModel,
                        themeStyle = AppThemeStyle.MATERIAL,
                        onBack = { handleMaterialBack() },
                        onAssignmentClick = { assignmentUi: AssignmentUiModel ->
                            dashboardViewModel.openDraft(assignmentUi)
                            materialNavigator.push(MiuixRoute.AssignmentDraft(assignmentUi.assignment.id, assignmentUi.courseName))
                        }
                    )
                }

                entry<MiuixRoute.AssignmentDraft>(swipeDismiss = materialSwipeBackDirection) { route: MiuixRoute.AssignmentDraft ->
                    val selectedAssignmentForDraft by dashboardViewModel.selectedAssignmentForDraft.collectAsState()
                    val currentDraft = selectedAssignmentForDraft ?: remember(route) {
                        dashboardViewModel.allAssignmentsUi.value.find { it.assignment.id == route.assignmentId }
                    }
                    if (currentDraft != null) {
                        AssignmentDraftScreen(
                            assignmentUi = currentDraft,
                            onBack = { handleMaterialBack() },
                            viewModel = dashboardViewModel,
                            themeStyle = AppThemeStyle.MATERIAL
                        )
                    }
                }

                entry<MiuixRoute.TaskHistoryArchive>(swipeDismiss = materialSwipeBackDirection) {
                    TaskHistoryArchiveScreen(
                        viewModel = archiveViewModel,
                        themeStyle = AppThemeStyle.MATERIAL,
                        onBack = { handleMaterialBack() },
                        onOpenAssignmentDetail = { assignment: Assignment ->
                            val courseName = notasViewModel.uiState.value.courses.find { it.id == assignment.courseId }?.name ?: "Curso"
                            val assignmentUi = AssignmentUiModel(
                                assignment = assignment,
                                courseName = courseName
                            )
                            dashboardViewModel.openDraft(assignmentUi)
                            materialNavigator.push(MiuixRoute.AssignmentDraft(assignment.id, courseName))
                        }
                    )
                }

                entry<MiuixRoute.ForoDetail>(swipeDismiss = materialSwipeBackDirection) { route: MiuixRoute.ForoDetail ->
                    LaunchedEffect(route.topicId) {
                        if (forosUiState.selectedDiscussion?.id != route.topicId) {
                            val existing = forosUiState.discussions.find { it.id == route.topicId }
                            if (existing != null) {
                                forosViewModel.selectDiscussion(existing)
                            }
                        }
                    }
                    ForoDetailScreen(
                        viewModel = forosViewModel,
                        themeStyle = AppThemeStyle.MATERIAL,
                        onBack = { handleMaterialBack() }
                    )
                }

                entry<MiuixRoute.CourseProgreso>(swipeDismiss = materialSwipeBackDirection) { route: MiuixRoute.CourseProgreso ->
                    LaunchedEffect(route.courseId) {
                        if (route.courseId != 0L && notasViewModel.uiState.value.selectedCourse?.id != route.courseId) {
                            val target = notasViewModel.uiState.value.courses.find { it.id == route.courseId }
                            if (target != null) {
                                notasViewModel.selectCourse(target)
                            }
                        }
                    }
                    NotasScreen(
                        viewModel = notasViewModel,
                        themeStyle = AppThemeStyle.MATERIAL,
                        onBack = { handleMaterialBack() }
                    )
                }

                entry<MiuixRoute.ForosHub>(swipeDismiss = materialSwipeBackDirection) {
                    ForosScreen(
                        viewModel = forosViewModel,
                        themeStyle = AppThemeStyle.MATERIAL,
                        onBack = { handleMaterialBack() },
                        onOpenTopic = { topic ->
                            forosViewModel.selectDiscussion(topic)
                            materialNavigator.push(MiuixRoute.ForoDetail(topic.id))
                        }
                    )
                }

                entry<MiuixRoute.CourseDetail>(swipeDismiss = materialSwipeBackDirection) { route: MiuixRoute.CourseDetail ->
                    val detailViewModel = remember(route.courseId) {
                        CourseDetailViewModel(
                            courseId = route.courseId,
                            repository = canvasRepository
                        )
                    }
                    CourseDetailScreen(
                        viewModel = detailViewModel,
                        themeStyle = AppThemeStyle.MATERIAL,
                        onBack = { handleMaterialBack() },
                        onNavigateToAssignmentDraft = { assignmentId, courseName ->
                            coroutineScope.launch {
                                val assignment = canvasRepository.getAssignment(assignmentId)
                                if (assignment != null) {
                                    val assignmentUi = AssignmentUiModel(assignment, courseName)
                                    dashboardViewModel.openDraft(assignmentUi)
                                    materialNavigator.push(MiuixRoute.AssignmentDraft(assignment.id, courseName))
                                }
                            }
                        },
                        onNavigateToForoDetail = { topicId ->
                            val topic = forosUiState.discussions.find { it.id == topicId }
                            if (topic != null) {
                                forosViewModel.selectDiscussion(topic)
                                materialNavigator.push(MiuixRoute.ForoDetail(topic.id))
                            } else {
                                coroutineScope.launch {
                                    val fetched = canvasRepository.getDiscussionTopic(route.courseId, topicId)
                                    if (fetched != null) {
                                        forosViewModel.selectDiscussion(fetched)
                                        materialNavigator.push(MiuixRoute.ForoDetail(fetched.id))
                                    }
                                }
                            }
                        },
                        onNavigateToProgreso = {
                            val course = notasViewModel.uiState.value.courses.find { it.id == route.courseId }
                            if (course != null) {
                                notasViewModel.selectCourse(course)
                            }
                            materialNavigator.push(MiuixRoute.CourseProgreso(route.courseId))
                        },
                        onNavigateToCopilot = { prompt ->
                            handleMaterialBack()
                            materialDestination = MainDestination.Copilot
                            copilotViewModel.sendMessage(prompt)
                        },
                        onNavigateToAnnouncements = {
                            materialNavigator.push(MiuixRoute.ForosHub)
                        },
                        onNavigateToAllModules = {
                            materialNavigator.push(MiuixRoute.CourseModules(route.courseId))
                        },
                        onOpenUrl = { url ->
                            try {
                                uriHandler.openUri(url)
                            } catch (_: Exception) {}
                        }
                    )
                }

                entry<MiuixRoute.CourseModules>(swipeDismiss = materialSwipeBackDirection) { route: MiuixRoute.CourseModules ->
                    val detailViewModel = remember(route.courseId) {
                        CourseDetailViewModel(
                            courseId = route.courseId,
                            repository = canvasRepository
                        )
                    }
                    CourseModulesScreen(
                        viewModel = detailViewModel,
                        themeStyle = AppThemeStyle.MATERIAL,
                        onBack = { handleMaterialBack() },
                        onNavigateToAssignmentDraft = { assignmentId, courseName ->
                            coroutineScope.launch {
                                val assignment = canvasRepository.getAssignment(assignmentId)
                                if (assignment != null) {
                                    val assignmentUi = AssignmentUiModel(assignment, courseName)
                                    dashboardViewModel.openDraft(assignmentUi)
                                    materialNavigator.push(MiuixRoute.AssignmentDraft(assignment.id, courseName))
                                }
                            }
                        },
                        onNavigateToForoDetail = { topicId ->
                            val topic = forosUiState.discussions.find { it.id == topicId }
                            if (topic != null) {
                                forosViewModel.selectDiscussion(topic)
                                materialNavigator.push(MiuixRoute.ForoDetail(topic.id))
                            } else {
                                coroutineScope.launch {
                                    val fetched = canvasRepository.getDiscussionTopic(route.courseId, topicId)
                                    if (fetched != null) {
                                        forosViewModel.selectDiscussion(fetched)
                                        materialNavigator.push(MiuixRoute.ForoDetail(fetched.id))
                                    }
                                }
                            }
                        },
                        onOpenUrl = { url ->
                            try {
                                uriHandler.openUri(url)
                            } catch (_: Exception) {}
                        }
                    )
                }

                entry<MiuixRoute.AcademicPerformance>(swipeDismiss = materialSwipeBackDirection) {
                    me.joxquin.notivas.ui.dashboard.performance.AcademicPerformanceScreen(
                        viewModel = dashboardViewModel,
                        themeStyle = AppThemeStyle.MATERIAL,
                        onBack = { handleMaterialBack() },
                        onNavigateToCourseProgreso = { courseId ->
                            coroutineScope.launch {
                                val course = canvasRepository.getCourse(courseId)
                                if (course != null) notasViewModel.selectCourse(course)
                                materialNavigator.push(MiuixRoute.CourseProgreso(courseId))
                            }
                        },
                        onNavigateToAssignmentDraft = { assignmentId, courseName ->
                            coroutineScope.launch {
                                val assignment = canvasRepository.getAssignment(assignmentId)
                                if (assignment != null) {
                                    val assignmentUi = AssignmentUiModel(assignment, courseName)
                                    dashboardViewModel.openDraft(assignmentUi)
                                    materialNavigator.push(MiuixRoute.AssignmentDraft(assignment.id, courseName))
                                }
                            }
                        }
                    )
                }
            }

            SnackbarHost(
                state = miuixSnackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (!isWideScreen) 88.dp else 24.dp)
            )
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
