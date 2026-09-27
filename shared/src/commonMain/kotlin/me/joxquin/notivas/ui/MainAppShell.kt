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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Tune
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
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Filter
import top.yukonga.miuix.kmp.icon.extended.Refresh
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

    val updateRepository = remember { UpdateRepository() }
    var activeUpdateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        val info = updateRepository.checkForUpdates()
        if (info.isUpdateAvailable) {
            activeUpdateInfo = info
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
    var showHerramientasTuneSheet by remember { mutableStateOf(false) }
    var showCatalogImportDialog by remember { mutableStateOf(false) }

    val forosUiState by forosViewModel.uiState.collectAsState()

    if (themeStyle == AppThemeStyle.MIUIX) {
        // En Miuix: Interceptar botón Atrás físico para volver al Detalle del Foro -> Hub de Foros -> Hub de Herramientas o a Inicio
        BackHandler(enabled = showingNotasDetail || showingForosDetail || currentDestination != MainDestination.Inicio) {
            if (currentDestination == MainDestination.Herramientas) {
                if (showingForosDetail && forosUiState.selectedDiscussion != null) {
                    forosViewModel.clearSelectedDiscussion()
                } else if (showingNotasDetail || showingForosDetail) {
                    showingNotasDetail = false
                    showingForosDetail = false
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
            // Capa 1: Contenido de la Pantalla Activa con HorizontalPager (Transiciones fluidas)
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
                                showingNotasDetail = showingNotasDetail,
                                showingForosDetail = showingForosDetail,
                                onOpenProgreso = {
                                    showingForosDetail = false
                                    showingNotasDetail = true
                                },
                                onOpenForos = {
                                    showingNotasDetail = false
                                    showingForosDetail = true
                                },
                                onOpenTuneSettings = { showHerramientasTuneSheet = true },
                                onBack = {
                                    showingNotasDetail = false
                                    showingForosDetail = false
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

            val copilotUiState by copilotViewModel.uiState.collectAsState()

            // Capa 2: Barra Superior Canónica Unificada (Top Overlay)
            MiuixTopAppBar(
                title = when {
                    currentDestination == MainDestination.Herramientas && showingNotasDetail -> "Progreso"
                    currentDestination == MainDestination.Herramientas && showingForosDetail -> {
                        if (forosUiState.selectedDiscussion != null) "Detalle del Foro" else "Foros Académicos"
                    }
                    currentDestination == MainDestination.Inicio -> "NotiVas"
                    currentDestination == MainDestination.Herramientas -> "Herramientas"
                    currentDestination == MainDestination.Copilot -> "Copilot IA"
                    currentDestination == MainDestination.Ajustes -> "Ajustes & Perfil"
                    else -> "NotiVas"
                },
                isScrolled = when (currentDestination) {
                    MainDestination.Inicio -> isInicioScrolled
                    MainDestination.Herramientas -> isHerramientasScrolled
                    MainDestination.Ajustes -> isAjustesScrolled
                    MainDestination.Copilot -> true
                },
                scrollOffset = when (currentDestination) {
                    MainDestination.Inicio -> inicioScrollOffset
                    MainDestination.Herramientas -> herramientasScrollOffset
                    MainDestination.Ajustes -> ajustesScrollOffset
                    MainDestination.Copilot -> if (isCopilotScrolled) copilotScrollOffset else 64f
                },
                backdrop = contentBackdrop,
                titleAlwaysVisible = currentDestination == MainDestination.Herramientas && (showingNotasDetail || showingForosDetail),
                navigationIcon = when {
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
                    when (currentDestination) {
                        MainDestination.Copilot -> {
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

                        MainDestination.Ajustes -> {
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

                        MainDestination.Herramientas -> {
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
                            } else if (!showingNotasDetail) {
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
                bottomContent = if (currentDestination == MainDestination.Herramientas && showingForosDetail && forosUiState.selectedDiscussion == null) {
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
                } else null,
                onNavigationClick = if (currentDestination == MainDestination.Inicio) {
                    { /* Notificaciones */ }
                } else null,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Capa 3: FloatingNavigationBar Independiente ("Liquid Glass")
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
                backdrop = contentBackdrop,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    } else {
        // ─── ARQUITECTURA MATERIAL DESIGN 3 (SCAFFOLD) ────────────────────────
        var materialDestination by remember { mutableStateOf(MainDestination.Inicio) }
        fun navigateToMaterial(destination: MainDestination) {
            materialDestination = destination
        }

        // Interceptar gesto/botón Atrás del sistema para Material 3:
        BackHandler(enabled = themeStyle == AppThemeStyle.MATERIAL && (showingNotasDetail || showingForosDetail || materialDestination != MainDestination.Inicio)) {
            if (materialDestination == MainDestination.Herramientas) {
                if (showingForosDetail && forosUiState.selectedDiscussion != null) {
                    forosViewModel.clearSelectedDiscussion()
                } else if (showingNotasDetail || showingForosDetail) {
                    showingNotasDetail = false
                    showingForosDetail = false
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
                    isVisible = !isImeVisible,
                    onNavigate = { destination ->
                        showingNotasDetail = false
                        showingForosDetail = false
                        materialDestination = destination
                    }
                )
            }
        ) { innerPadding ->
            val targetBottom = if (!isImeVisible) innerPadding.calculateBottomPadding() else 0.dp
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
                                showingNotasDetail = showingNotasDetail,
                                showingForosDetail = showingForosDetail,
                                onOpenProgreso = {
                                    showingForosDetail = false
                                    showingNotasDetail = true
                                },
                                onOpenForos = {
                                    showingNotasDetail = false
                                    showingForosDetail = true
                                },
                                onOpenTuneSettings = { showHerramientasTuneSheet = true },
                                onBack = {
                                    showingNotasDetail = false
                                    showingForosDetail = false
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
