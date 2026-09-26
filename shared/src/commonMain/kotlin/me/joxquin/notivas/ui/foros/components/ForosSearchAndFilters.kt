package me.joxquin.notivas.ui.foros.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.ui.foros.DiscussionFilterTab
import me.joxquin.notivas.ui.foros.DiscussionSortOption
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ForosSearchAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedTab: DiscussionFilterTab,
    onTabSelected: (DiscussionFilterTab) -> Unit,
    totalCount: Int,
    withPointsCount: Int,
    withoutPointsCount: Int,
    expiredCount: Int,
    selectedCourse: Course?,
    onOpenCourseSelector: () -> Unit,
    sortOption: DiscussionSortOption,
    onOpenSortSelector: () -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val searchBg = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainer
    val onSurface = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val outlineColor = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.outline
    val primaryColor = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Buscador redondeado
        Surface(
            shape = RoundedCornerShape(9999.dp),
            color = searchBg,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
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
                    tint = outlineColor,
                    modifier = Modifier.size(18.dp)
                )

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = onSurface,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(primaryColor),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Buscar foros, temas o profesores...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = outlineColor
                            )
                        }
                        innerTextField()
                    }
                )

                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Limpiar búsqueda",
                        tint = outlineColor,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable { onSearchQueryChange("") }
                    )
                }
            }
        }

        // 2. Filter Chips con scroll horizontal
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chip: Todos
            ForoFilterChip(
                label = "Todos",
                count = totalCount.toString(),
                isSelected = selectedTab == DiscussionFilterTab.TODOS,
                onClick = { onTabSelected(DiscussionFilterTab.TODOS) },
                themeStyle = themeStyle
            )

            // Chip: Con Puntos
            ForoFilterChip(
                label = "Con Puntos",
                count = withPointsCount.toString(),
                isSelected = selectedTab == DiscussionFilterTab.CON_PUNTOS,
                onClick = { onTabSelected(DiscussionFilterTab.CON_PUNTOS) },
                themeStyle = themeStyle
            )

            // Chip: Sin Puntos
            ForoFilterChip(
                label = "Sin Puntos",
                count = withoutPointsCount.toString(),
                isSelected = selectedTab == DiscussionFilterTab.SIN_PUNTOS,
                onClick = { onTabSelected(DiscussionFilterTab.SIN_PUNTOS) },
                themeStyle = themeStyle
            )

            // Chip: Vencidos
            ForoFilterChip(
                label = "Vencidos",
                count = if (expiredCount > 0) expiredCount.toString() else null,
                hasErrorDot = expiredCount > 0,
                isSelected = selectedTab == DiscussionFilterTab.VENCIDOS,
                onClick = { onTabSelected(DiscussionFilterTab.VENCIDOS) },
                themeStyle = themeStyle
            )

            // Chip: Selector de Curso
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = if (selectedCourse != null) primaryColor.copy(alpha = 0.2f) else searchBg,
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .clickable(onClick = onOpenCourseSelector)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = if (selectedCourse != null) primaryColor else outlineColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = selectedCourse?.name ?: "Todos los cursos",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = if (selectedCourse != null) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (selectedCourse != null) primaryColor else onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(if (selectedCourse != null) 120.dp else 110.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (selectedCourse != null) primaryColor else outlineColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 3. Fila de ordenamiento
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Ordenar por:",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = outlineColor
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = searchBg,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenSortSelector)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = sortOption.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForoFilterChip(
    label: String,
    count: String? = null,
    hasErrorDot: Boolean = false,
    isSelected: Boolean,
    onClick: () -> Unit,
    themeStyle: AppThemeStyle
) {
    val primaryColor = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val chipBg = if (isSelected) {
        primaryColor.copy(alpha = 0.2f)
    } else {
        if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainer
    }
    val textColor = if (isSelected) primaryColor else {
        if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(9999.dp),
        color = chipBg,
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(9999.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = textColor
            )

            if (count != null) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) primaryColor else {
                        if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = count,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isSelected) {
                                if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
                            } else {
                                if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            } else if (hasErrorDot) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                )
            }
        }
    }
}
