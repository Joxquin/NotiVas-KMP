// Copyright 2026, compose-miuix-ui contributors / NotiVas contributors
// SPDX-License-Identifier: Apache-2.0

package me.joxquin.notivas.ui.dashboard.components.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.CanvasDiscussionTopic
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.CourseMaterialSearchItem
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.dashboard_search_hint
import me.joxquin.notivas.ui.components.miuix.LocalAppPageInsets
import me.joxquin.notivas.ui.components.miuix.TopLayer
import me.joxquin.notivas.ui.components.miuix.appPageHorizontalInsets
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.ui.dashboard.CourseStat
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.glass.GlassSearchOverlay as MiuixGlassSearchOverlay
import top.yukonga.miuix.kmp.glass.GlassSearchOverlayDefaults
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Close
import top.yukonga.miuix.kmp.icon.glass.Search

/**
 * Overlay de búsqueda nativo de HyperOS (GlassSearchOverlay oficial de miuix-glass),
 * elevado mediante TopLayer para renderizarse por encima de HorizontalPager, NavDisplay y chromes persistentes.
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
    val pageInsets = LocalAppPageInsets.current
    val hintText = stringResource(Res.string.dashboard_search_hint)
    val searchColors = rememberSearchResultColors(AppThemeStyle.MIUIX)

    var selectedHistoryQuery by remember { mutableStateOf("") }
    LaunchedEffect(expanded) {
        if (!expanded) {
            selectedHistoryQuery = ""
        }
    }

    TopLayer {
        val lazyListState = rememberLazyListState()

        MiuixGlassSearchOverlay(
            expanded = expanded,
            onDismissRequest = {
                selectedHistoryQuery = ""
                onQueryChange("")
                onDismissRequest()
            },
            scrollMaterialThreshold = GlassSearchOverlayDefaults.VerticalPadding + 6.dp,
            modifier = modifier,
            horizontalInsetsModifier = Modifier.appPageHorizontalInsets(),
            label = hintText,
            scrollOffset = {
                if (lazyListState.firstVisibleItemIndex > 0) 1000f
                else lazyListState.firstVisibleItemScrollOffset.toFloat()
            },
            topInsets = pageInsets.safeInsets.only(WindowInsetsSides.Top),
            searchTopPadding = pageInsets.searchTop,
            searchIcon = MiuixIcons.Glass.Search,
            closeIcon = MiuixIcons.Glass.Close,
        ) { overlayQuery ->
            val effectiveQuery = overlayQuery.ifEmpty { selectedHistoryQuery }
            LaunchedEffect(overlayQuery) {
                if (overlayQuery.isNotEmpty() && selectedHistoryQuery.isNotEmpty()) {
                    selectedHistoryQuery = ""
                }
                onQueryChange(overlayQuery)
            }

            var debouncedQuery by remember { mutableStateOf("") }
            var isSearching by remember { mutableStateOf(false) }

            LaunchedEffect(effectiveQuery) {
                val trimmed = effectiveQuery.trim()
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

            val focusManager = LocalFocusManager.current
            val keyboard = LocalSoftwareKeyboardController.current
            val topPadding = pageInsets.safeInsets.only(WindowInsetsSides.Top)
                .asPaddingValues()
                .calculateTopPadding() +
                pageInsets.searchTop() +
                GlassSearchOverlayDefaults.BarHeight +
                GlassSearchOverlayDefaults.VerticalPadding * 2 +
                8.dp
            val bottomPadding = pageInsets.safeInsets.only(WindowInsetsSides.Bottom)
                .asPaddingValues()
                .calculateBottomPadding() + 24.dp

            val handleDismiss = {
                selectedHistoryQuery = ""
                onQueryChange("")
                onDismissRequest()
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = topPadding,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = bottomPadding,
                ),
            ) {
                if (effectiveQuery.isBlank()) {
                    item(key = "search_history") {
                        SearchHistorySection(
                            recentSearches = recentSearches,
                            onSelectQuery = { selected ->
                                selectedHistoryQuery = selected
                                onQueryChange(selected)
                                onSearchCommit(selected)
                                keyboard?.hide()
                                focusManager.clearFocus()
                            },
                            onRemoveQuery = onRemoveRecentQuery,
                            onClearHistory = onClearRecentHistory,
                            onAskAi = { onAskAi(effectiveQuery) },
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
                        isSearching = isSearching || (effectiveQuery.isNotBlank() && debouncedQuery.isEmpty()),
                        visibleLimit = visibleLimit,
                        hasMore = hasMore,
                        loadingMore = loadingMore,
                        colors = searchColors,
                        onCourseClick = { course ->
                            onSearchCommit(effectiveQuery)
                            handleDismiss()
                            onCourseClick(course)
                        },
                        onAssignmentClick = { assignment ->
                            onSearchCommit(effectiveQuery)
                            handleDismiss()
                            onAssignmentClick(assignment)
                        },
                        onForumClick = { forum ->
                            onSearchCommit(effectiveQuery)
                            handleDismiss()
                            onForumClick(forum)
                        },
                        onMaterialClick = { material ->
                            onSearchCommit(effectiveQuery)
                            handleDismiss()
                            onMaterialClick?.invoke(material)
                        },
                        onAskCopilot = { onAskAi(effectiveQuery) },
                        themeStyle = AppThemeStyle.MIUIX,
                    )
                }
            }
        }
    }
}
