package me.joxquin.notivas.ui.notas.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Course
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.DropdownArrowEndAction
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassDropdownPopup
import top.yukonga.miuix.kmp.glass.GlassPopupItem
import top.yukonga.miuix.kmp.glass.GlassPopupSizing
import top.yukonga.miuix.kmp.glass.glassPopupAnchorRow
import top.yukonga.miuix.kmp.glass.glassPopupAnchorValue
import top.yukonga.miuix.kmp.glass.rememberGlassPopupAnchor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils.Companion.PopupLayout
import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.notas_select_course
import me.joxquin.notivas.shared.resources.notas_course_default
import me.joxquin.notivas.shared.resources.*

@Composable
fun NotasHeaderSection(
    courses: List<Course>,
    selectedCourse: Course?,
    onCourseSelect: (Course) -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    backdrop: Backdrop? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrowRotation"
    )

    if (themeStyle == AppThemeStyle.MIUIX) {
        val windowInfo = LocalWindowInfo.current
        val density = LocalDensity.current
        val adaptiveMaxWidth = remember(windowInfo.containerSize.width, density) {
            with(density) {
                val widthDp = windowInfo.containerSize.width.toDp()
                (widthDp - 32.dp).coerceIn(288.dp, 400.dp)
            }
        }
        val anchor = rememberGlassPopupAnchor()
        var rowBounds by remember { mutableStateOf(Rect.Zero) }
        val mounted = remember { mutableStateOf(false) }
        val actualEnabled = courses.isNotEmpty()
        val hapticFeedback = LocalHapticFeedback.current
        val actionColor = if (actualEnabled) {
            MiuixTheme.colorScheme.onSurfaceVariantActions
        } else {
            MiuixTheme.colorScheme.disabledOnSecondaryVariant
        }

        LaunchedEffect(expanded) {
            if (expanded) {
                mounted.value = true
            } else if (mounted.value) {
                delay(350)
                mounted.value = false
            }
        }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        ) {
            BasicComponent(
                title = selectedCourse?.courseCode ?: stringResource(Res.string.notas_course_default),
                titleColor = BasicComponentDefaults.titleColor(color = MiuixTheme.colorScheme.primary),
                summary = selectedCourse?.name ?: stringResource(Res.string.notas_select_course),
                modifier = Modifier
                    .glassPopupAnchorRow(anchor)
                    .onGloballyPositioned {
                        rowBounds = it.boundsInWindow()
                    },
                onClick = {
                    expanded = !expanded
                    if (expanded) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    }
                },
                role = Role.DropdownList,
                holdDownState = mounted.value,
                enabled = actualEnabled,
                endActions = {
                    Row(
                        modifier = Modifier.glassPopupAnchorValue(anchor),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DropdownArrowEndAction(actionColor = actionColor)
                    }
                }
            )
        }

        if (mounted.value) {
            PopupLayout(
                visible = mounted,
                enableWindowDim = false,
                enableBackHandler = false,
                enterTransition = EnterTransition.None,
                exitTransition = ExitTransition.None,
                renderInRootScaffold = true,
            ) {
                val show = expanded && actualEnabled
                Box(modifier = Modifier.fillMaxSize()) {
                    GlassDropdownPopup(
                        show = show,
                        onDismissRequest = { expanded = false },
                        anchorBounds = rowBounds,
                        anchor = anchor,
                        backdrop = backdrop,
                        sizing = GlassPopupSizing(
                            minWidth = 260.dp,
                            maxWidth = adaptiveMaxWidth
                        ),
                    ) {
                        courses.forEach { course ->
                            val isSelected = course.id == selectedCourse?.id
                            GlassPopupItem(
                                text = course.name,
                                summary = course.courseCode,
                                selected = isSelected,
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                    expanded = false
                                    onCourseSelect(course)
                                }
                            )
                        }
                    }
                }
            }
        }
    } else {
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .animateContentSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { expanded = !expanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedCourse?.courseCode ?: "CURSO",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = selectedCourse?.name ?: "Selecciona un curso",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(Res.string.cd_expand),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer { rotationZ = arrowRotation }
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        courses.forEach { course ->
                            val isSelected = course.id == selectedCourse?.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCourseSelect(course)
                                        expanded = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = course.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        course.courseCode?.let { code ->
                                            Text(
                                                text = code,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = stringResource(Res.string.selected),
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
        }
    }
}
