package me.joxquin.notivas.ui.dashboard.components.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.CanvasDiscussionTopic
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.data.model.CourseMaterialSearchItem
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.search_ask_copilot_format
import me.joxquin.notivas.shared.resources.search_filter_assignments
import me.joxquin.notivas.shared.resources.search_filter_courses
import me.joxquin.notivas.shared.resources.search_filter_forums
import me.joxquin.notivas.shared.resources.search_filter_materials
import me.joxquin.notivas.shared.resources.search_no_results
import me.joxquin.notivas.ui.animation.staggeredEntrance
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.ui.dashboard.CourseStat
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Immutable
data class SearchResultColors(
    val onSurface: Color,
    val onVariant: Color,
    val primary: Color,
    val container: Color,
)

@Composable
fun rememberSearchResultColors(themeStyle: AppThemeStyle): SearchResultColors {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val containerColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow
    return remember(isMiuix, onSurfaceColor, onVariantColor, primaryColor, containerColor) {
        SearchResultColors(
            onSurface = onSurfaceColor,
            onVariant = onVariantColor,
            primary = primaryColor,
            container = containerColor,
        )
    }
}

/**
 * Emite los elementos de búsqueda virtualizados individualmente dentro de un LazyColumn.
 * Arquitectura basada en la implementación oficial de OverscrollLoadMorePage de compose-miuix-ui.
 */
fun LazyListScope.searchLiveResultItems(
    query: String,
    courses: List<Course>,
    courseStats: List<CourseStat>,
    assignments: List<AssignmentUiModel>,
    discussions: List<CanvasDiscussionTopic> = emptyList(),
    materials: List<CourseMaterialSearchItem> = emptyList(),
    isSearching: Boolean = false,
    visibleLimit: Int = 15,
    hasMore: Boolean = false,
    loadingMore: Boolean = false,
    colors: SearchResultColors,
    onCourseClick: (Course) -> Unit,
    onAssignmentClick: (AssignmentUiModel) -> Unit,
    onForumClick: (CanvasDiscussionTopic) -> Unit = {},
    onMaterialClick: ((CourseMaterialSearchItem) -> Unit)? = null,
    onAskCopilot: (String) -> Unit,
    themeStyle: AppThemeStyle,
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val onSurfaceColor = colors.onSurface
    val onVariantColor = colors.onVariant
    val primaryColor = colors.primary
    val containerColor = colors.container

    val trimmedQuery = query.trim()

    if (isSearching) {
        item(key = "search_loading_spinner") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                InfiniteProgressIndicator(color = primaryColor)
            }
        }
        return
    }

    if (trimmedQuery.isEmpty()) return

    val matchingCourses = courses.filter {
        it.name.contains(trimmedQuery, ignoreCase = true) ||
                (it.courseCode?.contains(trimmedQuery, ignoreCase = true) == true)
    }

    val matchingAssignments = assignments.filter {
        it.assignment.name.contains(trimmedQuery, ignoreCase = true) ||
                it.courseName.contains(trimmedQuery, ignoreCase = true)
    }

    val matchingDiscussions = discussions.filter {
        it.title.contains(trimmedQuery, ignoreCase = true) ||
                (it.courseName?.contains(trimmedQuery, ignoreCase = true) == true) ||
                (it.userName?.contains(trimmedQuery, ignoreCase = true) == true)
    }

    val matchingMaterials = materials.filter {
        it.item.title.contains(trimmedQuery, ignoreCase = true) ||
                it.courseName.contains(trimmedQuery, ignoreCase = true) ||
                it.moduleName.contains(trimmedQuery, ignoreCase = true)
    }

    val hasResults = matchingCourses.isNotEmpty() || matchingAssignments.isNotEmpty() ||
            matchingDiscussions.isNotEmpty() || matchingMaterials.isNotEmpty()

    if (!hasResults) {
        item(key = "search_empty_state") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = onVariantColor,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = stringResource(Res.string.search_no_results, query),
                    style = MaterialTheme.typography.bodyMedium,
                    color = onVariantColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (isMiuix) {
                    TextButton(
                        text = stringResource(Res.string.search_ask_copilot_format, query),
                        onClick = { onAskCopilot(query) },
                        colors = MiuixButtonDefaults.textButtonColorsPrimary()
                    )
                } else {
                    Button(
                        onClick = { onAskCopilot(query) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(Res.string.search_ask_copilot_format, query),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
        return
    }

    // ─── 1. Cursos Encontrados ──────────────────────────────────────────────
    val visibleCourses = matchingCourses.take(visibleLimit)
    if (visibleCourses.isNotEmpty()) {
        item(key = "section_header_courses") {
            if (isMiuix) {
                SmallTitle(
                    text = "${stringResource(Res.string.search_filter_courses).uppercase()} (${matchingCourses.size})",
                    modifier = Modifier.padding(start = 2.dp, top = 8.dp, bottom = 4.dp)
                )
            } else {
                Text(
                    text = "${stringResource(Res.string.search_filter_courses)} (${matchingCourses.size})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = onSurfaceColor,
                    modifier = Modifier.padding(start = 2.dp, top = 8.dp, bottom = 4.dp)
                )
            }
        }

        items(visibleCourses, key = { "search_course_${it.id}" }) { course ->
            val stat = courseStats.find { it.course.id == course.id }
            val avg = stat?.averageScore ?: 17.5
            if (isMiuix) {
                MiuixCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = MiuixCardDefaults.defaultColors(color = containerColor),
                    insideMargin = PaddingValues(12.dp),
                    pressFeedbackType = PressFeedbackType.Sink,
                    onClick = { onCourseClick(course) }
                ) {
                    CourseResultRow(
                        course = course,
                        avg = avg,
                        pendingCount = stat?.pendingCount ?: 0,
                        isMiuix = true,
                        onSurfaceColor = onSurfaceColor,
                        onVariantColor = onVariantColor,
                        primaryColor = primaryColor
                    )
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onCourseClick(course) }
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        CourseResultRow(
                            course = course,
                            avg = avg,
                            pendingCount = stat?.pendingCount ?: 0,
                            isMiuix = false,
                            onSurfaceColor = onSurfaceColor,
                            onVariantColor = onVariantColor,
                            primaryColor = primaryColor
                        )
                    }
                }
            }
        }
    }

    // ─── 2. Tareas Encontradas ──────────────────────────────────────────────
    val visibleAssignments = matchingAssignments.take(visibleLimit)
    if (visibleAssignments.isNotEmpty()) {
        item(key = "section_header_assignments") {
            if (isMiuix) {
                SmallTitle(
                    text = "${stringResource(Res.string.search_filter_assignments).uppercase()} (${matchingAssignments.size})",
                    modifier = Modifier.padding(start = 2.dp, top = 12.dp, bottom = 4.dp)
                )
            } else {
                Text(
                    text = "${stringResource(Res.string.search_filter_assignments)} (${matchingAssignments.size})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = onSurfaceColor,
                    modifier = Modifier.padding(start = 2.dp, top = 12.dp, bottom = 4.dp)
                )
            }
        }

        items(visibleAssignments, key = { "search_asgn_${it.assignment.id}" }) { uiModel ->
            val isCompleted = uiModel.assignment.isCompleted || uiModel.assignment.status == "completed"
            if (isMiuix) {
                MiuixCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = MiuixCardDefaults.defaultColors(color = containerColor),
                    insideMargin = PaddingValues(12.dp),
                    pressFeedbackType = PressFeedbackType.Sink,
                    onClick = { onAssignmentClick(uiModel) }
                ) {
                    AssignmentResultRow(
                        uiModel = uiModel,
                        isCompleted = isCompleted,
                        isMiuix = true,
                        onSurfaceColor = onSurfaceColor,
                        onVariantColor = onVariantColor,
                        primaryColor = primaryColor
                    )
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onAssignmentClick(uiModel) }
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        AssignmentResultRow(
                            uiModel = uiModel,
                            isCompleted = isCompleted,
                            isMiuix = false,
                            onSurfaceColor = onSurfaceColor,
                            onVariantColor = onVariantColor,
                            primaryColor = primaryColor
                        )
                    }
                }
            }
        }
    }

    // ─── 3. Foros Encontrados ──────────────────────────────────────────────
    val visibleDiscussions = matchingDiscussions.take(visibleLimit)
    if (visibleDiscussions.isNotEmpty()) {
        item(key = "section_header_forums") {
            if (isMiuix) {
                SmallTitle(
                    text = "${stringResource(Res.string.search_filter_forums).uppercase()} (${matchingDiscussions.size})",
                    modifier = Modifier.padding(start = 2.dp, top = 12.dp, bottom = 4.dp)
                )
            } else {
                Text(
                    text = "${stringResource(Res.string.search_filter_forums)} (${matchingDiscussions.size})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = onSurfaceColor,
                    modifier = Modifier.padding(start = 2.dp, top = 12.dp, bottom = 4.dp)
                )
            }
        }

        items(visibleDiscussions, key = { "search_forum_${it.id}" }) { topic ->
            if (isMiuix) {
                MiuixCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = MiuixCardDefaults.defaultColors(color = containerColor),
                    insideMargin = PaddingValues(12.dp),
                    pressFeedbackType = PressFeedbackType.Sink,
                    onClick = { onForumClick(topic) }
                ) {
                    ForumResultRow(
                        topic = topic,
                        isMiuix = true,
                        onSurfaceColor = onSurfaceColor,
                        onVariantColor = onVariantColor,
                        primaryColor = primaryColor
                    )
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onForumClick(topic) }
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        ForumResultRow(
                            topic = topic,
                            isMiuix = false,
                            onSurfaceColor = onSurfaceColor,
                            onVariantColor = onVariantColor,
                            primaryColor = primaryColor
                        )
                    }
                }
            }
        }
    }

    // ─── 4. Materiales y Módulos de Clases ──────────────────────────────────
    val visibleMaterials = matchingMaterials.take(visibleLimit)
    if (visibleMaterials.isNotEmpty()) {
        item(key = "section_header_materials") {
            if (isMiuix) {
                SmallTitle(
                    text = "${stringResource(Res.string.search_filter_materials).uppercase()} (${matchingMaterials.size})",
                    modifier = Modifier.padding(start = 2.dp, top = 12.dp, bottom = 4.dp)
                )
            } else {
                Text(
                    text = "${stringResource(Res.string.search_filter_materials)} (${matchingMaterials.size})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = onSurfaceColor,
                    modifier = Modifier.padding(start = 2.dp, top = 12.dp, bottom = 4.dp)
                )
            }
        }

        items(visibleMaterials, key = { "search_mat_${it.item.id}_${it.courseId}" }) { material ->
            val uriHandler = LocalUriHandler.current
            val onRowClick = {
                if (onMaterialClick != null) {
                    onMaterialClick(material)
                } else {
                    val targetUrl = material.item.htmlUrl ?: material.item.externalUrl ?: material.item.url
                    if (!targetUrl.isNullOrBlank()) {
                        try {
                            uriHandler.openUri(targetUrl)
                        } catch (_: Exception) {}
                    }
                }
            }

            if (isMiuix) {
                MiuixCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = MiuixCardDefaults.defaultColors(color = containerColor),
                    insideMargin = PaddingValues(12.dp),
                    pressFeedbackType = PressFeedbackType.Sink,
                    onClick = onRowClick
                ) {
                    MaterialResultRow(
                        material = material,
                        isMiuix = true,
                        onSurfaceColor = onSurfaceColor,
                        onVariantColor = onVariantColor,
                        primaryColor = primaryColor
                    )
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(onClick = onRowClick)
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        MaterialResultRow(
                            material = material,
                            isMiuix = false,
                            onSurfaceColor = onSurfaceColor,
                            onVariantColor = onVariantColor,
                            primaryColor = primaryColor
                        )
                    }
                }
            }
        }
    }

    // ─── 5. Pie de Paginación Dinámica (Overscroll Load More) ────────────────
    if (hasMore) {
        item(key = "footer_load_more") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (loadingMore) {
                    InfiniteProgressIndicator(color = primaryColor)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (loadingMore) "Cargando más resultados…" else "Desliza para ver más resultados",
                    style = MaterialTheme.typography.bodySmall,
                    color = onVariantColor
                )
            }
        }
    }

    // ─── 6. Botón inferior para consultar con Copilot IA ────────────────────
    item(key = "search_copilot_cta") {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isMiuix) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onAskCopilot(query) }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(Res.string.search_ask_copilot_format, query),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = onSurfaceColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Componente Composable de conveniencia para invocar la búsqueda con LazyColumn integrada.
 */
@Composable
fun SearchLiveResultsSection(
    query: String,
    courses: List<Course>,
    courseStats: List<CourseStat>,
    assignments: List<AssignmentUiModel>,
    discussions: List<CanvasDiscussionTopic> = emptyList(),
    materials: List<CourseMaterialSearchItem> = emptyList(),
    isSearching: Boolean = false,
    onCourseClick: (Course) -> Unit,
    onAssignmentClick: (AssignmentUiModel) -> Unit,
    onForumClick: (CanvasDiscussionTopic) -> Unit = {},
    onMaterialClick: ((CourseMaterialSearchItem) -> Unit)? = null,
    onAskCopilot: (String) -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    var visibleLimit by remember(query) { mutableIntStateOf(15) }
    var loadingMore by remember { mutableStateOf(false) }

    val totalMatchingCount = remember(query, courses, assignments, discussions, materials) {
        val q = query.trim()
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

    val searchColors = rememberSearchResultColors(themeStyle)

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        searchLiveResultItems(
            query = query,
            courses = courses,
            courseStats = courseStats,
            assignments = assignments,
            discussions = discussions,
            materials = materials,
            isSearching = isSearching,
            visibleLimit = visibleLimit,
            hasMore = hasMore,
            loadingMore = loadingMore,
            colors = searchColors,
            onCourseClick = onCourseClick,
            onAssignmentClick = onAssignmentClick,
            onForumClick = onForumClick,
            onMaterialClick = onMaterialClick,
            onAskCopilot = onAskCopilot,
            themeStyle = themeStyle
        )
    }
}

@Composable
private fun CourseResultRow(
    course: Course,
    avg: Double,
    pendingCount: Int,
    isMiuix: Boolean,
    onSurfaceColor: androidx.compose.ui.graphics.Color,
    onVariantColor: androidx.compose.ui.graphics.Color,
    primaryColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .then(
                    if (isMiuix) Modifier.squircleSurface(
                        color = MiuixTheme.colorScheme.surfaceContainerHigh,
                        cornerRadius = 10.dp
                    ) else Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.School,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = onSurfaceColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = course.courseCode ?: "CS-${course.id % 1000}",
                    style = MaterialTheme.typography.labelSmall,
                    color = onVariantColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (pendingCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(onVariantColor.copy(alpha = 0.5f))
                    )
                    Text(
                        text = "$pendingCount pendientes",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMiuix) MiuixTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = (avg * 10).toInt().toDouble().div(10).toString(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = primaryColor
            )
            Text(
                text = "/20",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = onVariantColor
            )
        }
    }
}

@Composable
private fun AssignmentResultRow(
    uiModel: AssignmentUiModel,
    isCompleted: Boolean,
    isMiuix: Boolean,
    onSurfaceColor: androidx.compose.ui.graphics.Color,
    onVariantColor: androidx.compose.ui.graphics.Color,
    primaryColor: androidx.compose.ui.graphics.Color
) {
    val statusColor = if (isCompleted) {
        if (isMiuix) MiuixTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    } else {
        primaryColor
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(statusColor)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = uiModel.assignment.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = onSurfaceColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = uiModel.courseName,
                style = MaterialTheme.typography.bodySmall,
                color = onVariantColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = onVariantColor,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun ForumResultRow(
    topic: CanvasDiscussionTopic,
    isMiuix: Boolean,
    onSurfaceColor: androidx.compose.ui.graphics.Color,
    onVariantColor: androidx.compose.ui.graphics.Color,
    primaryColor: androidx.compose.ui.graphics.Color
) {
    val points = topic.assignment?.pointsPossible
    val hasPoints = points != null && points > 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = primaryColor.copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = topic.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = onSurfaceColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = topic.courseName ?: "Canvas",
                    style = MaterialTheme.typography.bodySmall,
                    color = onVariantColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (hasPoints) {
                    Text(
                        text = "• ${points?.toInt()} pts",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = primaryColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = onVariantColor,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun MaterialResultRow(
    material: CourseMaterialSearchItem,
    isMiuix: Boolean,
    onSurfaceColor: androidx.compose.ui.graphics.Color,
    onVariantColor: androidx.compose.ui.graphics.Color,
    primaryColor: androidx.compose.ui.graphics.Color
) {
    val icon = when (material.item.type.lowercase()) {
        "file" -> Icons.Outlined.Description
        "externalurl", "url" -> Icons.Default.Link
        "discussion" -> Icons.Default.Forum
        "assignment" -> Icons.AutoMirrored.Outlined.Assignment
        else -> Icons.Outlined.Folder
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .then(
                    if (isMiuix) Modifier.squircleSurface(
                        color = MiuixTheme.colorScheme.surfaceContainerHigh,
                        cornerRadius = 10.dp
                    ) else Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = material.item.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = onSurfaceColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${material.courseName} • ${material.moduleName}",
                style = MaterialTheme.typography.bodySmall,
                color = onVariantColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = onVariantColor,
            modifier = Modifier.size(14.dp)
        )
    }
}
