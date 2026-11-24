// Copyright 2026, compose-miuix-ui contributors / NotiVas contributors
// SPDX-License-Identifier: Apache-2.0

package me.joxquin.notivas.ui.dashboard.components.search

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import me.joxquin.notivas.data.model.CourseMaterialSearchItem
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.CanvasDiscussionTopic
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.cancel
import me.joxquin.notivas.shared.resources.dashboard_search_hint
import me.joxquin.notivas.ui.components.BackHandler
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.ui.dashboard.CourseStat
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.material.GlassDefaults
import top.yukonga.miuix.kmp.material.GlassShadows
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Close
import top.yukonga.miuix.kmp.icon.glass.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import me.joxquin.notivas.ui.components.miuix.SearchModeEasing
import me.joxquin.notivas.ui.components.miuix.rememberGlassSearchBarAlpha
import me.joxquin.notivas.ui.components.miuix.glassSearchBarTransition
import me.joxquin.notivas.ui.components.miuix.rememberGlassSearchTitleAlpha

/**
 * Overlay de búsqueda nativo de HyperOS (GlassSearchOverlay), adaptado a NotiVas con
 * historial reciente, resultados de Canvas en vivo y acceso directo a Copilot IA.
 */
@Composable
fun GlassSearchOverlay(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchCommit: (String) -> Unit,
    recentSearches: List<String>,
    onRemoveRecentQuery: (String) -> Unit,
    onClearRecentHistory: () -> Unit,
    courses: List<Course>,
    courseStats: List<CourseStat>,
    assignments: List<AssignmentUiModel>,
    discussions: List<CanvasDiscussionTopic> = emptyList(),
    materials: List<CourseMaterialSearchItem> = emptyList(),
    onCourseClick: (Course) -> Unit,
    onAssignmentClick: (AssignmentUiModel) -> Unit,
    onForumClick: (CanvasDiscussionTopic) -> Unit = {},
    onMaterialClick: ((CourseMaterialSearchItem) -> Unit)? = null,
    onAskAi: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var debouncedQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            debouncedQuery = ""
            isSearching = false
        } else {
            isSearching = true
            delay(250)
            debouncedQuery = trimmed
            isSearching = false
        }
    }

    val lazyListState = rememberLazyListState()
    var visibleLimit by remember(debouncedQuery) { mutableIntStateOf(15) }
    var loadingMore by remember { mutableStateOf(false) }

    val totalMatchingCount = remember(debouncedQuery, courses, assignments, discussions, materials) {
        val q = debouncedQuery.trim()
        if (q.isEmpty()) 0 else {
            courses.count { it.name.contains(q, true) || it.courseCode?.contains(q, true) == true } +
            assignments.count { it.assignment.name.contains(q, true) || it.courseName.contains(q, true) } +
            discussions.count { it.title.contains(q, true) || it.courseName?.contains(q, true) == true } +
            materials.count { it.item.title.contains(q, true) || it.courseName.contains(q, true) }
        }
    }

    val hasMore = visibleLimit < totalMatchingCount

    LaunchedEffect(lazyListState, hasMore) {
        if (!hasMore) return@LaunchedEffect
        snapshotFlow {
            val layoutInfo = lazyListState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= 0 && lastVisible >= layoutInfo.totalItemsCount - 3
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && !loadingMore && hasMore) {
                    loadingMore = true
                    delay(150)
                    visibleLimit += 15
                    loadingMore = false
                }
            }
    }

    val handleDismiss = {
        onQueryChange("")
        onDismissRequest()
    }

    val searchColors = rememberSearchResultColors(AppThemeStyle.MIUIX)

    val transition = updateTransition(expanded, label = "glassSearch")
    val progress = transition.animateFloat(
        transitionSpec = { tween(400, easing = SearchModeEasing) },
        label = "glassSearchGeometry",
    ) { if (it) 1f else 0f }

    val cancelAlpha = transition.animateFloat(
        transitionSpec = { tween(150, delayMillis = if (targetState) 100 else 0, easing = LinearEasing) },
        label = "glassSearchCancelMaterial",
    ) { if (it) 1f else 0f }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val direction = LocalLayoutDirection.current
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)
    val active = expanded || transition.currentState || transition.isRunning

    LaunchedEffect(expanded) {
        if (expanded) {
            focusRequester.requestFocus()
            keyboard?.show()
        } else if (active) {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }

    if (!active) return

    BackHandler(
        enabled = expanded,
        onBack = onDismissRequest,
    )

    val colors = MiuixTheme.colorScheme
    val hintText = stringResource(Res.string.dashboard_search_hint)
    val cancelContentDesc = stringResource(Res.string.cancel)

    Box(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent {
                if (it.key == Key.Escape && it.type == KeyEventType.KeyUp) {
                    onDismissRequest()
                    true
                } else {
                    false
                }
            },
    ) {
        // Scrim de fondo que atenúa la pantalla detrás del overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress.value }
                .background(colors.surface)
                .clickable(interactionSource = null, indication = null) {
                    keyboard?.hide()
                    focusManager.clearFocus()
                },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress.value }
                .windowInsetsPadding(WindowInsets.statusBars)
                .imePadding(),
        ) {
            // Cabecera de búsqueda con animación elástica
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .graphicsLayer { translationY = -statusBarHeight * (1f - progress.value) },
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    enabled = expanded,
                    singleLine = true,
                    textStyle = MiuixTheme.textStyles.body1.copy(color = colors.onSurface),
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (query.isNotBlank()) {
                                onSearchCommit(query.trim())
                            }
                            keyboard?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .layout { measurable, constraints ->
                            val endSpace = (54.dp.toPx() * progress.value).toInt()
                            val width = (constraints.maxWidth - endSpace).coerceAtLeast(0)
                            val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                            layout(constraints.maxWidth, placeable.height) {
                                placeable.placeRelative(0, 0)
                            }
                        }
                        .height(44.dp)
                        .focusRequester(focusRequester),
                    decorationBox = { field ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.surfaceContainerHigh, CircleShape)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Glass.Search,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = colors.onSurfaceVariantSummary,
                            )
                            Box(Modifier.weight(1f)) {
                                if (query.isEmpty()) {
                                    Text(
                                        text = hintText,
                                        color = colors.onSurfaceVariantSummary,
                                        style = MiuixTheme.textStyles.body1,
                                    )
                                }
                                field()
                            }
                            if (query.isNotEmpty()) {
                                if (isSearching) {
                                    Box(
                                        modifier = Modifier.size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        InfiniteProgressIndicator(
                                            color = colors.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            onQueryChange("")
                                            focusRequester.requestFocus()
                                        },
                                        modifier = Modifier.size(24.dp),
                                    ) {
                                        Icon(
                                            imageVector = MiuixIcons.Glass.Close,
                                            contentDescription = "Limpiar",
                                            modifier = Modifier.size(18.dp),
                                            tint = colors.onSurfaceVariantSummary,
                                        )
                                    }
                                }
                            }
                        }
                    },
                )

                // Botón Cancelar con animación de salida angular y blur
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .graphicsLayer {
                            val fraction = progress.value
                            translationX = 66.dp.toPx() * (1f - fraction) * if (direction == LayoutDirection.Ltr) 1f else -1f
                            scaleX = fraction
                            scaleY = fraction
                            alpha = cancelAlpha.value
                            val blur = 50f * (1f - cancelAlpha.value)
                            renderEffect = if (blur > 0.5f) BlurEffect(blur, blur, TileMode.Decal) else null
                        },
                ) {
                    IconButton(
                        onClick = handleDismiss,
                        enabled = expanded,
                        modifier = Modifier
                            .size(44.dp)
                            .background(colors.surfaceContainerHigh, CircleShape),
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Glass.Close,
                            contentDescription = cancelContentDesc,
                            modifier = Modifier.size(24.dp),
                            tint = colors.onSurface,
                        )
                    }
                }
            }

            // Cuerpo de resultados / historial con desvanecimiento coordinado
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = progress.value
                    },
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    if (query.isBlank()) {
                        item(key = "search_history") {
                            SearchHistorySection(
                                recentSearches = recentSearches,
                                onSelectQuery = { selectedQuery ->
                                    onQueryChange(selectedQuery)
                                    onSearchCommit(selectedQuery)
                                    keyboard?.hide()
                                    focusManager.clearFocus()
                                },
                                onRemoveQuery = onRemoveRecentQuery,
                                onClearHistory = onClearRecentHistory,
                                onAskAi = { onAskAi(query) },
                                themeStyle = AppThemeStyle.MIUIX,
                            )
                        }
                    } else {
                        searchLiveResultItems(
                            query = debouncedQuery,
                            courses = courses,
                            courseStats = courseStats,
                            assignments = assignments,
                            discussions = discussions,
                            materials = materials,
                            isSearching = isSearching || (query.isNotBlank() && debouncedQuery.isEmpty()),
                            visibleLimit = visibleLimit,
                            hasMore = hasMore,
                            loadingMore = loadingMore,
                            colors = searchColors,
                            onCourseClick = { course ->
                                onSearchCommit(query)
                                handleDismiss()
                                onCourseClick(course)
                            },
                            onAssignmentClick = { assignment ->
                                onSearchCommit(query)
                                handleDismiss()
                                onAssignmentClick(assignment)
                            },
                            onForumClick = { forum ->
                                onSearchCommit(query)
                                handleDismiss()
                                onForumClick(forum)
                            },
                            onMaterialClick = { material ->
                                onSearchCommit(query)
                                handleDismiss()
                                onMaterialClick?.invoke(material)
                            },
                            onAskCopilot = { onAskAi(query) },
                            themeStyle = AppThemeStyle.MIUIX,
                        )
                    }
                }
            }
        }
    }
}