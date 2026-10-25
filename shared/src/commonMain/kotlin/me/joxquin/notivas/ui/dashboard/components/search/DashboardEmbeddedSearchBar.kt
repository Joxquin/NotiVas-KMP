package me.joxquin.notivas.ui.dashboard.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import me.joxquin.notivas.ui.components.miuix.SearchStatus
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.cancel
import me.joxquin.notivas.shared.resources.dashboard_ask_ai
import me.joxquin.notivas.shared.resources.dashboard_search_hint
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.ui.dashboard.CourseStat
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.basic.SearchCleanup
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Cápsula ancla en el Feed que reporta su posición Y en la ventana global
 * para inicializar y sincronizar la animación fluida tipo SuperSearchBar.
 */
@Composable
fun DashboardSearchBarAnchor(
    query: String,
    onSearchClick: () -> Unit,
    onPositionChanged: (Dp) -> Unit,
    onAskAi: (String) -> Unit = {},
    themeStyle: AppThemeStyle,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val density = LocalDensity.current
    val placeholder = stringResource(Res.string.dashboard_search_hint)
    val containerColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val placeholderColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant

    if (isMiuix) {
        // En MIUIX: Cápsula idéntica a SearchBarFake canónico
        // Puesto que se aloja dentro de LazyColumn(contentPadding = 16.dp), la cápsula ancla
        // comienza exactamente a 16.dp de cada borde de la pantalla (ScreenWidth - 32.dp).
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
                .graphicsLayer {
                    alpha = if (isVisible) 1f else 0f
                }
                .onGloballyPositioned { coordinates ->
                    with(density) {
                        val y = coordinates.positionInWindow().y.toDp()
                        if (y > 0.dp) {
                            onPositionChanged(y)
                        }
                    }
                }
                .then(
                    if (isVisible) {
                        Modifier.clickable { onSearchClick() }
                    } else {
                        Modifier
                    }
                )
        ) {
            InputField(
                query = query,
                onQueryChange = {},
                label = placeholder,
                leadingIcon = {
                    top.yukonga.miuix.kmp.basic.Icon(
                        imageVector = MiuixIcons.Basic.Search,
                        contentDescription = "Buscar",
                        modifier = Modifier
                            .size(44.dp)
                            .padding(start = 16.dp, end = 8.dp),
                        tint = MiuixTheme.colorScheme.onSurfaceContainerHigh
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                enabled = false,
                onSearch = {},
                expanded = false,
                onExpandedChange = {}
            )
        }
    } else {
        // En Material 3 Expressive: dimensiones y padding idénticos a la cápsula dinámica (48.dp, esquinas 24.dp)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = containerColor,
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp)
                .graphicsLayer {
                    alpha = if (isVisible) 1f else 0f
                }
                .clip(RoundedCornerShape(24.dp))
                .onGloballyPositioned { coordinates ->
                    with(density) {
                        val y = coordinates.positionInWindow().y.toDp()
                        if (y > 0.dp) {
                            onPositionChanged(y)
                        }
                    }
                }
                .then(
                    if (isVisible) {
                        Modifier.clickable { onSearchClick() }
                    } else {
                        Modifier
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = placeholderColor,
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = query.ifBlank { placeholder },
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (query.isNotBlank()) textColor else placeholderColor
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Reserves [inset] above the content, read during layout so a changing value never recomposes the caller. */
internal fun Modifier.topInset(inset: () -> Dp): Modifier = layout { measurable, constraints ->
    val insetPx = inset().roundToPx()
    val placeable = measurable.measure(constraints.offset(vertical = -insetPx))
    layout(placeable.width, placeable.height + insetPx) {
        placeable.placeRelative(0, insetPx)
    }
}

/**
 * Overlay de búsqueda con animación Ease (interpolación vertical continua).
 * Sube desde la coordenada [initialOffsetY] hasta la parte superior de la pantalla,
 * desvaneciendo suavemente el fondo e introduciendo el campo de búsqueda y resultados.
 */
@Composable
fun DashboardAnimatedSearchOverlay(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    initialOffsetY: Dp,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchCommit: (String) -> Unit,
    recentSearches: List<String>,
    onRemoveRecentQuery: (String) -> Unit,
    onClearRecentHistory: () -> Unit,
    courses: List<Course>,
    courseStats: List<CourseStat>,
    assignments: List<AssignmentUiModel>,
    discussions: List<me.joxquin.notivas.data.model.CanvasDiscussionTopic> = emptyList(),
    onCourseClick: (Course) -> Unit,
    onAssignmentClick: (AssignmentUiModel) -> Unit,
    onForumClick: (me.joxquin.notivas.data.model.CanvasDiscussionTopic) -> Unit = {},
    onAskAi: (String) -> Unit,
    themeStyle: AppThemeStyle,
    onAnimStatusChange: (SearchAnimStatus) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var animStatus by remember { mutableStateOf(if (isExpanded) SearchAnimStatus.EXPANDED else SearchAnimStatus.COLLAPSED) }

    LaunchedEffect(animStatus) {
        onAnimStatusChange(animStatus)
    }

    LaunchedEffect(isExpanded) {
        if (isExpanded && animStatus.shouldCollapse()) {
            animStatus = SearchAnimStatus.EXPANDING
        } else if (!isExpanded && animStatus.shouldExpand()) {
            animStatus = SearchAnimStatus.COLLAPSING
        }
    }

    if (animStatus == SearchAnimStatus.COLLAPSED && !isExpanded) {
        return
    }

    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val focusRequester = remember { FocusRequester() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    val placeholder = stringResource(Res.string.dashboard_search_hint)
    val cancelText = stringResource(Res.string.cancel)
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val targetTopPadding = if (animStatus.shouldExpand()) {
        statusBarTop + 12.dp
    } else {
        max(initialOffsetY, statusBarTop + 12.dp)
    }

    val topPadding by animateDpAsState(
        targetValue = targetTopPadding,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "DashboardSearchTopPadding",
        finishedListener = {
            if (animStatus == SearchAnimStatus.EXPANDING) {
                animStatus = SearchAnimStatus.EXPANDED
            } else if (animStatus == SearchAnimStatus.COLLAPSING) {
                animStatus = SearchAnimStatus.COLLAPSED
                onExpandedChange(false)
            }
        }
    )

    val surfaceAlpha by animateFloatAsState(
        targetValue = if (animStatus.shouldExpand()) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "DashboardSearchSurfaceAlpha"
    )

    val surfaceColor = if (isMiuix) MiuixTheme.colorScheme.surface else MaterialTheme.colorScheme.surface

    fun collapseSearch() {
        focusManager.clearFocus()
        animStatus = SearchAnimStatus.COLLAPSING
        onQueryChange("")
        onExpandedChange(false)
    }

    me.joxquin.notivas.ui.components.BackHandler(enabled = animStatus.shouldExpand()) {
        collapseSearch()
    }

    LaunchedEffect(animStatus) {
        if (animStatus == SearchAnimStatus.EXPANDED) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(100f)
            .background(surfaceColor.copy(alpha = surfaceAlpha))
            .then(
                if (animStatus.shouldExpand()) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .topInset { topPadding }
        ) {
            // Header del Buscador Animado
            if (isMiuix) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        InputField(
                            query = query,
                            onQueryChange = onQueryChange,
                            label = placeholder,
                            leadingIcon = {
                                top.yukonga.miuix.kmp.basic.Icon(
                                    imageVector = MiuixIcons.Basic.Search,
                                    contentDescription = "Buscar",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .padding(start = 16.dp, end = 8.dp),
                                    tint = MiuixTheme.colorScheme.onSurfaceContainerHigh
                                )
                            },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    top.yukonga.miuix.kmp.basic.Icon(
                                        imageVector = MiuixIcons.Basic.SearchCleanup,
                                        contentDescription = "Limpiar",
                                        tint = MiuixTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .padding(start = 8.dp, end = 16.dp)
                                            .clickable(
                                                interactionSource = null,
                                                indication = null,
                                                onClick = { onQueryChange("") }
                                            )
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = if (animStatus.shouldExpand()) 6.dp else 16.dp
                                )
                                .padding(bottom = 6.dp)
                                .focusRequester(focusRequester),
                            color = MiuixTheme.colorScheme.surfaceContainerHigh,
                            onSearch = {
                                if (query.isNotBlank()) onSearchCommit(query)
                            },
                            expanded = animStatus.shouldExpand(),
                            onExpandedChange = {}
                        )
                    }

                    AnimatedVisibility(
                        visible = animStatus.shouldExpand(),
                        enter = expandHorizontally() + slideInHorizontally(initialOffsetX = { it }),
                        exit = shrinkHorizontally() + slideOutHorizontally(targetOffsetX = { it })
                    ) {
                        MiuixText(
                            text = cancelText,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(start = 4.dp, end = 16.dp, bottom = 6.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { collapseSearch() }
                                )
                        )
                    }
                }
            } else {
                // Header Material 3 Expressive
                val materialStartPadding by animateDpAsState(
                    targetValue = if (animStatus.shouldExpand()) 8.dp else 16.dp,
                    animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
                    label = "MaterialSearchStartPadding"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = materialStartPadding,
                            end = 16.dp,
                            bottom = 8.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(
                        visible = animStatus.shouldExpand(),
                        enter = fadeIn(animationSpec = tween(150)) + expandHorizontally(animationSpec = tween(250)),
                        exit = fadeOut(animationSpec = tween(150)) + shrinkHorizontally(animationSpec = tween(250))
                    ) {
                        IconButton(
                            onClick = { collapseSearch() },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )

                            BasicTextField(
                                value = query,
                                onValueChange = onQueryChange,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    if (query.isEmpty()) {
                                        Text(
                                            text = placeholder,
                                            style = TextStyle(
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            if (query.isNotEmpty()) {
                                IconButton(
                                    onClick = { onQueryChange("") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Resultados o Historial revelados con animación secuencial
            AnimatedVisibility(
                visible = animStatus.isExpand(),
                enter = fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(250)) { it / 6 },
                exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(animationSpec = tween(150)) { it / 6 },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
                ) {
                    if (query.isBlank()) {
                        item {
                            SearchHistorySection(
                                recentSearches = recentSearches,
                                onSelectQuery = { selectedTerm ->
                                    onQueryChange(selectedTerm)
                                    onSearchCommit(selectedTerm)
                                },
                                onRemoveQuery = onRemoveRecentQuery,
                                onClearHistory = onClearRecentHistory,
                                onAskAi = { onAskAi(query) },
                                themeStyle = themeStyle
                            )
                        }
                    } else {
                        item {
                            SearchLiveResultsSection(
                                query = query,
                                courses = courses,
                                courseStats = courseStats,
                                assignments = assignments,
                                discussions = discussions,
                                onCourseClick = { course ->
                                    onSearchCommit(course.name)
                                    animStatus = SearchAnimStatus.COLLAPSING
                                    onCourseClick(course)
                                },
                                onAssignmentClick = { uiModel ->
                                    onSearchCommit(uiModel.assignment.name)
                                    animStatus = SearchAnimStatus.COLLAPSING
                                    onAssignmentClick(uiModel)
                                },
                                onForumClick = { topic ->
                                    onSearchCommit(topic.title)
                                    animStatus = SearchAnimStatus.COLLAPSING
                                    onForumClick(topic)
                                },
                                onAskCopilot = { prompt ->
                                    onSearchCommit(prompt)
                                    animStatus = SearchAnimStatus.COLLAPSING
                                    onAskAi(prompt)
                                },
                                themeStyle = themeStyle
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente unificado para mantener compatibilidad si se invoca directamente.
 */
@Composable
fun DashboardEmbeddedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchCommit: (String) -> Unit,
    recentSearches: List<String>,
    onRemoveRecentQuery: (String) -> Unit,
    onClearRecentHistory: () -> Unit,
    courses: List<Course>,
    courseStats: List<CourseStat>,
    assignments: List<AssignmentUiModel>,
    onCourseClick: (Course) -> Unit,
    onAssignmentClick: (AssignmentUiModel) -> Unit,
    onAskAi: (String) -> Unit,
    themeStyle: AppThemeStyle,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isExpanded) {
        DashboardSearchBarAnchor(
            query = query,
            onSearchClick = { onExpandedChange(true) },
            onPositionChanged = {},
            onAskAi = onAskAi,
            themeStyle = themeStyle,
            modifier = modifier
        )
    } else {
        DashboardAnimatedSearchOverlay(
            isExpanded = true,
            onExpandedChange = onExpandedChange,
            initialOffsetY = 120.dp,
            query = query,
            onQueryChange = onQueryChange,
            onSearchCommit = onSearchCommit,
            recentSearches = recentSearches,
            onRemoveRecentQuery = onRemoveRecentQuery,
            onClearRecentHistory = onClearRecentHistory,
            courses = courses,
            courseStats = courseStats,
            assignments = assignments,
            onCourseClick = onCourseClick,
            onAssignmentClick = onAssignmentClick,
            onAskAi = onAskAi,
            themeStyle = themeStyle,
            modifier = modifier
        )
    }
}
