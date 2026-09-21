package me.joxquin.notivas.ui.copilot.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.ui.copilot.MentionItem
import me.joxquin.notivas.ui.copilot.MentionResourceType
import me.joxquin.notivas.ui.copilot.MentionStep
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Componente flotante de autocompletado para menciones con '@'.
 * Permite seleccionar un curso y navegar recursivamente por sus recursos (Tareas, Módulos, Archivos, Foros).
 */
@Composable
fun CopilotMentionPopup(
    isVisible: Boolean,
    mentionStep: MentionStep,
    selectedCourse: Course?,
    courses: List<Course>,
    resources: List<MentionItem>,
    onSelectCourse: (Course) -> Unit,
    onSelectResource: (MentionItem) -> Unit,
    onBackToCourses: () -> Unit,
    onDismiss: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = modifier
    ) {
        if (themeStyle == AppThemeStyle.MIUIX) {
            // Diseño Miuix Squircle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .squircleSurface(
                        color = MiuixTheme.colorScheme.surfaceVariant,
                        cornerRadius = 20.dp
                    )
                    .squircleBorder(
                        width = 1.dp,
                        color = MiuixTheme.colorScheme.dividerLine,
                        cornerRadius = 20.dp
                    )
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header de navegación de mención
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (mentionStep == MentionStep.COURSE_RESOURCES) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .squircleSurface(
                                            color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                            cornerRadius = 9999.dp
                                        )
                                        .clickable(onClick = onBackToCourses),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Atrás",
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (mentionStep == MentionStep.COURSES) "Seleccionar Curso" else (selectedCourse?.name ?: "Recursos"),
                                style = MiuixTheme.textStyles.footnote1.copy(fontWeight = FontWeight.Bold),
                                color = MiuixTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .squircleSurface(
                                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                    cornerRadius = 9999.dp
                                )
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MiuixTheme.colorScheme.onSurfaceSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista de opciones
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                    ) {
                        if (mentionStep == MentionStep.COURSES) {
                            items(courses) { course ->
                                MiuixCourseItem(
                                    course = course,
                                    onClick = { onSelectCourse(course) }
                                )
                            }
                        } else {
                            items(resources) { item ->
                                MiuixResourceItem(
                                    item = item,
                                    onClick = { onSelectResource(item) }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Diseño Material 3 Expressive
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (mentionStep == MentionStep.COURSE_RESOURCES) {
                                IconButton(
                                    onClick = onBackToCourses,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Atrás",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (mentionStep == MentionStep.COURSES) "Mencionar Curso (@)" else (selectedCourse?.name ?: "Recursos"),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                    ) {
                        if (mentionStep == MentionStep.COURSES) {
                            items(courses) { course ->
                                MaterialCourseItem(
                                    course = course,
                                    onClick = { onSelectCourse(course) }
                                )
                            }
                        } else {
                            items(resources) { item ->
                                MaterialResourceItem(
                                    item = item,
                                    onClick = { onSelectResource(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixCourseItem(
    course: Course,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .squircleSurface(
                    color = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                    cornerRadius = 8.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = course.name,
                style = MiuixTheme.textStyles.body2.copy(fontWeight = FontWeight.Medium),
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!course.courseCode.isNullOrBlank()) {
                Text(
                    text = course.courseCode ?: "",
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MiuixResourceItem(
    item: MentionItem,
    onClick: () -> Unit
) {
    val icon = getResourceIcon(item.type)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .squircleSurface(
                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                    cornerRadius = 8.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MiuixTheme.textStyles.body2.copy(fontWeight = FontWeight.Medium),
                color = MiuixTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.subtitle,
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MaterialCourseItem(
    course: Course,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!course.courseCode.isNullOrBlank()) {
                Text(
                    text = course.courseCode ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MaterialResourceItem(
    item: MentionItem,
    onClick: () -> Unit
) {
    val icon = getResourceIcon(item.type)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

private fun getResourceIcon(type: MentionResourceType): ImageVector {
    return when (type) {
        MentionResourceType.ALL_COURSE -> Icons.Default.Class
        MentionResourceType.ASSIGNMENT -> Icons.AutoMirrored.Filled.Assignment
        MentionResourceType.MODULE -> Icons.Default.Folder
        MentionResourceType.FILE -> Icons.Default.Description
        MentionResourceType.DISCUSSION -> Icons.Default.Forum
    }
}
