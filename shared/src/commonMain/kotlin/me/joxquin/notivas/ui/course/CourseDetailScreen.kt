package me.joxquin.notivas.ui.course

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.miuix.BackNavigationIcon
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.MiuixTopAppBarAction
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import me.joxquin.notivas.ui.components.miuix.shortenCourseSubtitle
import me.joxquin.notivas.ui.course.components.CourseHeroBannerCard
import me.joxquin.notivas.ui.course.components.CourseLearningRoadmapCard
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    viewModel: CourseDetailViewModel,
    themeStyle: AppThemeStyle,
    onBack: () -> Unit,
    onNavigateToAssignmentDraft: (Long, String) -> Unit = { _, _ -> },
    onNavigateToForoDetail: (Long) -> Unit = {},
    onNavigateToProgreso: () -> Unit = {},
    onNavigateToCopilot: (String) -> Unit = {},
    onNavigateToAnnouncements: () -> Unit = {},
    onNavigateToAllModules: () -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    if (themeStyle == AppThemeStyle.MIUIX) {
        val scrollBehavior = MiuixScrollBehavior()
        val listState = rememberLazyListState()
        val blurBackdrop = rememberMiuixBlurBackdrop()

        top.yukonga.miuix.kmp.basic.Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                MiuixBlurredBar(
                    backdrop = blurBackdrop,
                    scrollBehavior = scrollBehavior
                ) {
                    MiuixPageTopAppBar(
                        title = shortenCourseSubtitle(uiState.course?.name).ifBlank { stringResource(Res.string.course_detail_screen_title) },
                        subtitle = (uiState.course?.courseCode ?: "").uppercase(),
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            BackNavigationIcon(onClick = onBack)
                        },
                        actions = {
                            MiuixTopAppBarAction(
                                onClick = { viewModel.loadCourseDetail(isRefresh = true) },
                                backdrop = blurBackdrop
                            ) {
                                top.yukonga.miuix.kmp.basic.Icon(
                                    imageVector = MiuixIcons.Refresh,
                                    contentDescription = "Refrescar",
                                    tint = MiuixTheme.colorScheme.onBackground
                                )
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            val contentPadding = miuixPageContentPadding(
                innerPadding = innerPadding,
                extraTop = 8.dp,
                extraBottom = 24.dp
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .miuixBackdropModifier(blurBackdrop)
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MiuixTheme.colorScheme.primary)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = contentPadding,
                        modifier = Modifier
                            .fillMaxSize()
                            .miuixPageScrollModifiers(scrollBehavior),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // 1. Course Hero Banner Card
                        item {
                            CourseHeroBannerCard(
                                course = uiState.course,
                                departmentTag = uiState.departmentTag,
                                professorName = uiState.professorName,
                                promedioFormatted = uiState.promedioFormatted,
                                avancePercentage = uiState.avancePercentage,
                                completedModulesCount = uiState.completedModulesCount,
                                totalModulesCount = uiState.totalModulesCount,
                                themeStyle = AppThemeStyle.MIUIX
                            )
                        }

                        // 2. Ruta de Aprendizaje (Tarjeta única interactiva con semana activa incrustada)
                        item {
                            CourseLearningRoadmapCard(
                                activeModule = uiState.activeModule ?: uiState.modules.firstOrNull(),
                                totalModulesCount = uiState.totalModulesCount,
                                themeStyle = AppThemeStyle.MIUIX,
                                onClick = onNavigateToAllModules,
                                onItemClick = { item ->
                                    viewModel.openItem(
                                        item = item,
                                        uriHandler = uriHandler,
                                        onNavigateToAssignmentDraft = onNavigateToAssignmentDraft,
                                        onNavigateToForoDetail = onNavigateToForoDetail
                                    )
                                }
                            )
                        }
                    }

                    VerticalScrollBar(
                        adapter = rememberScrollBarAdapter(listState),
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        trackPadding = contentPadding
                    )
                }
            }
        }
    } else {
        // ─── ARQUITECTURA MATERIAL DESIGN 3 ──────────────────────────────────
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = shortenCourseSubtitle(uiState.course?.name).ifBlank { stringResource(Res.string.course_detail_screen_title) },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.loadCourseDetail(isRefresh = true) }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refrescar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        bottom = paddingValues.calculateBottomPadding() + 24.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // 1. Course Hero Banner Card
                    item {
                        CourseHeroBannerCard(
                            course = uiState.course,
                            departmentTag = uiState.departmentTag,
                            professorName = uiState.professorName,
                            promedioFormatted = uiState.promedioFormatted,
                            avancePercentage = uiState.avancePercentage,
                            completedModulesCount = uiState.completedModulesCount,
                            totalModulesCount = uiState.totalModulesCount,
                            themeStyle = AppThemeStyle.MATERIAL
                        )
                    }

                    // 2. Ruta de Aprendizaje (Tarjeta única interactiva con semana activa incrustada)
                    item {
                        CourseLearningRoadmapCard(
                            activeModule = uiState.activeModule ?: uiState.modules.firstOrNull(),
                            totalModulesCount = uiState.totalModulesCount,
                            themeStyle = AppThemeStyle.MATERIAL,
                            onClick = onNavigateToAllModules,
                            onItemClick = { item ->
                                viewModel.openItem(
                                    item = item,
                                    uriHandler = uriHandler,
                                    onNavigateToAssignmentDraft = onNavigateToAssignmentDraft,
                                    onNavigateToForoDetail = onNavigateToForoDetail
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
