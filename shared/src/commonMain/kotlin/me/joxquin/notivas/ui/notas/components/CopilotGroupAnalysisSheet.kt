package me.joxquin.notivas.ui.notas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.notas.CopilotAnalysisResult
import me.joxquin.notivas.ui.notas.NotasViewModel
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import kotlin.math.roundToInt

@Composable
fun CopilotGroupAnalysisSheet(
    course: Course,
    viewModel: NotasViewModel,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    onDismiss: () -> Unit,
    onGroupsApplied: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var customPrompt by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var analysisResult by remember { mutableStateOf<CopilotAnalysisResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun triggerAnalysis() {
        isLoading = true
        errorMessage = null
        viewModel.analyzeCourseWithCopilot(
            courseId = course.id,
            userPromptOverride = customPrompt.takeIf { it.isNotBlank() },
            onSuccess = { result ->
                analysisResult = result
                isLoading = false
            },
            onError = { err ->
                errorMessage = err
                isLoading = false
            }
        )
    }

    LaunchedEffect(course.id) {
        triggerAnalysis()
    }

    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val backgroundColor = if (isMiuix) {
        MiuixTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val dragHandleColor = if (isMiuix) {
        MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
    val primaryColor = if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val cardColor = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainer
    val onSurfaceColor = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant

    val fieldColors = if (isMiuix) {
        TextFieldDefaults.textFieldColors()
    } else {
        TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            borderColor = MaterialTheme.colorScheme.primary
        )
    }

    WindowBottomSheet(
        title = stringResource(Res.string.groups_create_copilot_title),
        show = true,
        onDismissRequest = onDismiss,
        backgroundColor = backgroundColor,
        dragHandleColor = dragHandleColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = course.name,
                fontSize = 13.sp,
                color = onSurfaceVariantColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            TextField(
                value = customPrompt,
                onValueChange = { customPrompt = it },
                label = stringResource(Res.string.groups_copilot_context_label),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    triggerAnalysis()
                }),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = primaryColor,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = stringResource(Res.string.groups_copilot_analyzing),
                        fontSize = 13.sp,
                        color = onSurfaceVariantColor
                    )
                }
            } else {
                val result = analysisResult
                val groups = result?.groups.orEmpty()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!result?.reasoning.isNullOrBlank()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.defaultColors(color = cardColor),
                                insideMargin = PaddingValues(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = stringResource(Res.string.groups_copilot_reasoning_title),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                    Text(
                                        text = result?.reasoning.orEmpty(),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = onSurfaceVariantColor
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.groups_copilot_preview_title),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceVariantColor
                            )
                            val totalWeight = groups.sumOf { it.weightPercentage.toDouble() }.roundToInt()
                            Text(
                                text = "Total: $totalWeight%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalWeight == 100) primaryColor else onSurfaceVariantColor
                            )
                        }
                    }

                    items(groups) { group ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.defaultColors(color = cardColor),
                            insideMargin = PaddingValues(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = group.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = onSurfaceColor
                                    )
                                    Text(
                                        text = "${group.weightPercentage.roundToInt()}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }
                                group.items.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(onSurfaceVariantColor)
                                        )
                                        Text(
                                            text = item.name,
                                            fontSize = 12.sp,
                                            color = onSurfaceVariantColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (item.canvasAssignmentId != null) {
                                            Text(
                                                text = "Canvas LMS",
                                                fontSize = 10.sp,
                                                color = primaryColor,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Acciones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        text = stringResource(Res.string.groups_copilot_retry_btn),
                        onClick = {
                            focusManager.clearFocus()
                            triggerAnalysis()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        text = stringResource(Res.string.groups_copilot_apply_btn),
                        onClick = {
                            viewModel.applyCopilotProposedGroups(course.id, groups)
                            onGroupsApplied()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.4f),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }
        }
    }
}
