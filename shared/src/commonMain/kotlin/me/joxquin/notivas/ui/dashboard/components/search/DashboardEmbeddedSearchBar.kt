package me.joxquin.notivas.ui.dashboard.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val focusRequester = remember { FocusRequester() }

    val placeholder = stringResource(Res.string.dashboard_search_hint)
    val askAiText = stringResource(Res.string.dashboard_ask_ai)
    val cancelText = stringResource(Res.string.cancel)

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val surfaceColor = if (isMiuix) MiuixTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
    val surfaceAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "DashboardSearchSurfaceAlpha"
    )

    if (!isExpanded) {
        // ─── Estado Colapsado: Cápsula en el Feed ───────────────────────────
        val containerColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHigh
        val textColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
        val placeholderColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
        val aiButtonBgColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
        val aiButtonTextColor = if (isMiuix) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer

        Surface(
            shape = RoundedCornerShape(9999.dp),
            color = containerColor,
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(9999.dp))
                .clickable { onExpandedChange(true) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (query.isNotBlank()) textColor else placeholderColor
                    ),
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = aiButtonBgColor,
                    modifier = Modifier.clickable { onAskAi(query) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = aiButtonTextColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = askAiText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = aiButtonTextColor
                        )
                    }
                }
            }
        }
    } else {
        // ─── Estado Expandido: Vista Incrustada a Pantalla Completa ─────────
        me.joxquin.notivas.ui.components.BackHandler(enabled = isExpanded) {
            onQueryChange("")
            onExpandedChange(false)
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f)
                .background(surfaceColor.copy(alpha = surfaceAlpha))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header de Búsqueda
                if (isMiuix) {
                    // Header MIUIX estilo SuperSearchBar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = statusBarTop + 8.dp, start = 12.dp, end = 12.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            InputField(
                                query = query,
                                onQueryChange = onQueryChange,
                                label = placeholder,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        modifier = Modifier.padding(start = 12.dp, end = 4.dp).size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (query.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpiar",
                                            tint = MiuixTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .padding(end = 12.dp)
                                                .size(20.dp)
                                                .clickable { onQueryChange("") }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                onSearch = {
                                    if (query.isNotBlank()) onSearchCommit(query)
                                },
                                expanded = true,
                                onExpandedChange = {}
                            )
                        }

                        AnimatedVisibility(
                            visible = true,
                            enter = expandHorizontally() + slideInHorizontally(initialOffsetX = { it }),
                            exit = shrinkHorizontally() + slideOutHorizontally(targetOffsetX = { it })
                        ) {
                            MiuixText(
                                text = cancelText,
                                fontWeight = FontWeight.SemiBold,
                                color = MiuixTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 4.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            onQueryChange("")
                                            onExpandedChange(false)
                                        }
                                    )
                            )
                        }
                    }
                } else {
                    // Header Material 3 Expressive SearchBar
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = statusBarTop + 8.dp, start = 8.dp, end = 16.dp, bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    onQueryChange("")
                                    onExpandedChange(false)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Atrás",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(28.dp),
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
                                    BasicTextField(
                                        value = query,
                                        onValueChange = onQueryChange,
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(focusRequester),
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        decorationBox = { innerTextField ->
                                            if (query.isEmpty()) {
                                                Text(
                                                    text = placeholder,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                }

                // Contenido de Resultados o Historial con animación fluida
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) +
                            androidx.compose.animation.slideInVertically(
                                animationSpec = androidx.compose.animation.core.tween(260)
                            ) { it / 8 },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
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
                                    onCourseClick = { course ->
                                        onSearchCommit(course.name)
                                        onExpandedChange(false)
                                        onCourseClick(course)
                                    },
                                    onAssignmentClick = { uiModel ->
                                        onSearchCommit(uiModel.assignment.name)
                                        onExpandedChange(false)
                                        onAssignmentClick(uiModel)
                                    },
                                    onAskCopilot = { prompt ->
                                        onSearchCommit(prompt)
                                        onExpandedChange(false)
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
}
