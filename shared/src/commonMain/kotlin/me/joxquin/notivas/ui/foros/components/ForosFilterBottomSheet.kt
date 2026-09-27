package me.joxquin.notivas.ui.foros.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.ui.foros.DiscussionFilterTab
import me.joxquin.notivas.ui.foros.DiscussionSortOption
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close as MiuixClose
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForosFilterBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    selectedTab: DiscussionFilterTab,
    onTabSelected: (DiscussionFilterTab) -> Unit,
    totalCount: Int,
    withPointsCount: Int,
    withoutPointsCount: Int,
    expiredCount: Int,
    courses: List<Course>,
    selectedCourseId: Long?,
    onCourseSelected: (Long?) -> Unit,
    sortOption: DiscussionSortOption,
    onSortOptionSelected: (DiscussionSortOption) -> Unit,
    themeStyle: AppThemeStyle
) {
    if (themeStyle == AppThemeStyle.MIUIX) {
        WindowBottomSheet(
            title = "Filtros y Ordenamiento",
            show = show,
            onDismissRequest = onDismissRequest,
            startAction = {
                val dismissState = LocalDismissState.current
                top.yukonga.miuix.kmp.basic.IconButton(
                    onClick = { dismissState?.invoke() }
                ) {
                    top.yukonga.miuix.kmp.basic.Icon(
                        imageVector = MiuixIcons.MiuixClose,
                        contentDescription = "Cerrar",
                        tint = MiuixTheme.colorScheme.onBackground
                    )
                }
            },
            endAction = {
                val dismissState = LocalDismissState.current
                top.yukonga.miuix.kmp.basic.IconButton(
                    onClick = { dismissState?.invoke() }
                ) {
                    top.yukonga.miuix.kmp.basic.Icon(
                        imageVector = MiuixIcons.Ok,
                        contentDescription = "Listo",
                        tint = MiuixTheme.colorScheme.primary
                    )
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .scrollEndHaptic()
                    .overScrollVertical()
            ) {
                // Sección 1: Estado del Foro
                item {
                    SmallTitle(
                        text = "Categoría y Estado",
                        insideMargin = PaddingValues(16.dp, 8.dp)
                    )
                    Card(
                        modifier = Modifier.padding(bottom = 12.dp),
                        colors = CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        DiscussionFilterTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            val label = when (tab) {
                                DiscussionFilterTab.TODOS -> "Todos los foros"
                                DiscussionFilterTab.CON_PUNTOS -> "Foros con calificación (Puntos)"
                                DiscussionFilterTab.SIN_PUNTOS -> "Foros sin calificación"
                                DiscussionFilterTab.VENCIDOS -> "Foros vencidos"
                            }
                            val count = when (tab) {
                                DiscussionFilterTab.TODOS -> totalCount
                                DiscussionFilterTab.CON_PUNTOS -> withPointsCount
                                DiscussionFilterTab.SIN_PUNTOS -> withoutPointsCount
                                DiscussionFilterTab.VENCIDOS -> expiredCount
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTabSelected(tab) }
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = count.toString(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = if (isSelected) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurfaceVariantSummary
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        top.yukonga.miuix.kmp.basic.Icon(
                                            imageVector = MiuixIcons.Ok,
                                            contentDescription = null,
                                            tint = MiuixTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Sección 2: Curso
                item {
                    val courseOptions = listOf("Todos los cursos") + courses.map { it.name }
                    val currentCourseIndex = if (selectedCourseId == null) 0 else {
                        val idx = courses.indexOfFirst { it.id == selectedCourseId }
                        if (idx >= 0) idx + 1 else 0
                    }

                    SmallTitle(
                        text = "Filtro por Asignatura",
                        insideMargin = PaddingValues(16.dp, 8.dp)
                    )
                    Card(
                        modifier = Modifier.padding(bottom = 12.dp),
                        colors = CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        WindowDropdownPreference(
                            title = "Curso Seleccionado",
                            items = courseOptions,
                            selectedIndex = currentCourseIndex,
                            onSelectedIndexChange = { index ->
                                if (index == 0) {
                                    onCourseSelected(null)
                                } else {
                                    val course = courses.getOrNull(index - 1)
                                    onCourseSelected(course?.id)
                                }
                            }
                        )
                    }
                }

                // Sección 3: Criterio de Ordenamiento
                item {
                    val sortOptions = DiscussionSortOption.entries.map { it.displayName }
                    val currentSortIndex = DiscussionSortOption.entries.indexOf(sortOption).coerceAtLeast(0)

                    SmallTitle(
                        text = "Orden de Visualización",
                        insideMargin = PaddingValues(16.dp, 8.dp)
                    )
                    Card(
                        modifier = Modifier.padding(bottom = 12.dp),
                        colors = CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        WindowDropdownPreference(
                            title = "Criterio",
                            items = sortOptions,
                            selectedIndex = currentSortIndex,
                            onSelectedIndexChange = { index ->
                                val selected = DiscussionSortOption.entries.getOrNull(index) ?: DiscussionSortOption.DUE_DATE_ASC
                                onSortOptionSelected(selected)
                            }
                        )
                    }

                    Spacer(
                        Modifier.padding(
                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                WindowInsets.captionBar.asPaddingValues().calculateBottomPadding() + 16.dp
                        )
                    )
                }
            }
        }
    } else {
        // Material Design 3 Modal Bottom Sheet
        if (show) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = onDismissRequest,
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                dragHandle = null
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Filtros y Ordenamiento",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Personaliza la lista de foros",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(onClick = onDismissRequest) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }

                    // Categoría / Estado
                    item {
                        Text(
                            text = "Categoría del Foro",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            DiscussionFilterTab.entries.forEach { tab ->
                                val isSelected = selectedTab == tab
                                val label = when (tab) {
                                    DiscussionFilterTab.TODOS -> "Todos los foros"
                                    DiscussionFilterTab.CON_PUNTOS -> "Foros con puntos"
                                    DiscussionFilterTab.SIN_PUNTOS -> "Foros sin puntos"
                                    DiscussionFilterTab.VENCIDOS -> "Foros vencidos"
                                }
                                val count = when (tab) {
                                    DiscussionFilterTab.TODOS -> totalCount
                                    DiscussionFilterTab.CON_PUNTOS -> withPointsCount
                                    DiscussionFilterTab.SIN_PUNTOS -> withoutPointsCount
                                    DiscussionFilterTab.VENCIDOS -> expiredCount
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onTabSelected(tab) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = count.toString(),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Curso
                    item {
                        Text(
                            text = "Filtrar por Curso",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val allCoursesOption = Course(id = -1L, name = "Todos los cursos", courseCode = "")
                            val courseList = listOf(allCoursesOption) + courses

                            courseList.forEach { course ->
                                val isSelected = if (course.id == -1L) selectedCourseId == null else selectedCourseId == course.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (course.id == -1L) {
                                                onCourseSelected(null)
                                            } else {
                                                onCourseSelected(course.id)
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = course.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Ordenamiento
                    item {
                        Text(
                            text = "Criterio de Ordenamiento",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            DiscussionSortOption.entries.forEach { option ->
                                val isSelected = sortOption == option
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSortOptionSelected(option) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = option.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
