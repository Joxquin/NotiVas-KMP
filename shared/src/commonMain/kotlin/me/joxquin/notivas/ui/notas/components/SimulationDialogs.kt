package me.joxquin.notivas.ui.notas.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationItem
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.miuix.GlassDialogActions
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Switch as MiuixSwitch
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults as MiuixTextFieldDefaults
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassDialog
import top.yukonga.miuix.kmp.glass.GlassDialogButton
import top.yukonga.miuix.kmp.glass.GlassDialogButtonRole
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

// ─── 1. Dialog: Añadir Evaluación (Canvas vs Vacío) ─────────────────────────
@Composable
fun AddAssignmentDialog(
    groupName: String,
    availableAssignments: List<Assignment>,
    onDismiss: () -> Unit,
    onAddExisting: (Assignment) -> Unit,
    onAddPlaceholder: (name: String, initialScore: Float) -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    show: Boolean = true,
    backdrop: Backdrop? = null,
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    var placeholderName by remember { mutableStateOf("") }
    var placeholderScoreText by remember { mutableStateOf("15") }

    if (themeStyle == AppThemeStyle.MIUIX) {
        GlassDialog(
            show = show,
            backdrop = backdrop,
            title = stringResource(Res.string.simulation_add_assignment_title_format, groupName),
            summary = stringResource(Res.string.simulation_add_assignment_summary),
            onDismissRequest = onDismiss,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MiuixTheme.colorScheme.secondaryContainer, RoundedCornerShape(14.dp)),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { tabIndex = 0 }
                            .background(
                                if (tabIndex == 0) MiuixTheme.colorScheme.primary
                                else MiuixTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(14.dp),
                            )
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        MiuixText(
                            text = stringResource(
                                Res.string.simulation_tab_canvas_format,
                                availableAssignments.size,
                            ),
                            style = MiuixTheme.textStyles.button,
                            color = if (tabIndex == 0) MiuixTheme.colorScheme.onPrimary
                            else MiuixTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { tabIndex = 1 }
                            .background(
                                if (tabIndex == 1) MiuixTheme.colorScheme.primary
                                else MiuixTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(14.dp),
                            )
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        MiuixText(
                            text = stringResource(Res.string.simulation_empty_assignment_tab),
                            style = MiuixTheme.textStyles.button,
                            color = if (tabIndex == 1) MiuixTheme.colorScheme.onPrimary
                            else MiuixTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }

                if (tabIndex == 0) {
                    if (availableAssignments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            MiuixText(
                                text = stringResource(Res.string.simulation_no_canvas_assignments),
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(availableAssignments, key = { it.id }) { assignment ->
                                val rawScore = assignment.score ?: assignment.submission?.score
                                val maxPoints = assignment.pointsPossible ?: 20.0
                                val isGraded = rawScore != null

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onAddExisting(assignment) }
                                        .background(MiuixTheme.colorScheme.surfaceContainerHigh),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            MiuixText(
                                                text = assignment.name,
                                                style = MiuixTheme.textStyles.body2.copy(fontWeight = FontWeight.SemiBold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isGraded) MiuixTheme.colorScheme.secondaryContainer
                                                            else MiuixTheme.colorScheme.surfaceContainer,
                                                        ),
                                                ) {
                                                    MiuixText(
                                                        text = if (isGraded) {
                                                            stringResource(
                                                                Res.string.simulation_graded_score_format,
                                                                (rawScore ?: 0.0).toInt(),
                                                                maxPoints.toInt(),
                                                            )
                                                        } else {
                                                            stringResource(Res.string.simulation_ungraded)
                                                        },
                                                        style = MiuixTheme.textStyles.footnote2.copy(fontWeight = FontWeight.Bold),
                                                        color = if (isGraded) MiuixTheme.colorScheme.onSecondaryContainer
                                                        else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    )
                                                }
                                            }
                                        }
                                        MiuixIcon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = stringResource(Res.string.simulation_action_add),
                                            tint = MiuixTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MiuixText(
                            text = stringResource(Res.string.simulation_placeholder_hint),
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        MiuixTextField(
                            value = placeholderName,
                            onValueChange = { placeholderName = it },
                            label = stringResource(Res.string.simulation_placeholder_name_label),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        MiuixTextField(
                            value = placeholderScoreText,
                            onValueChange = { placeholderScoreText = it },
                            label = stringResource(Res.string.simulation_placeholder_score_label),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            GlassDialogActions {
                GlassDialogButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    MiuixText(
                        text = stringResource(Res.string.close),
                        style = MiuixTheme.textStyles.button,
                    )
                }
                if (tabIndex == 1) {
                    GlassDialogButton(
                        onClick = {
                            val score = placeholderScoreText.toFloatOrNull()?.coerceIn(0f, 20f) ?: 15f
                            if (placeholderName.isNotBlank()) {
                                onAddPlaceholder(placeholderName.trim(), score)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = placeholderName.isNotBlank(),
                        role = GlassDialogButtonRole.Primary,
                    ) {
                        MiuixText(
                            text = stringResource(Res.string.simulation_create_empty),
                            style = MiuixTheme.textStyles.button,
                        )
                    }
                }
            }
        }
    } else if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Column {
                    Text(
                        text = stringResource(Res.string.simulation_add_assignment_title_format, groupName),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(Res.string.simulation_add_assignment_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TabRow(
                        selectedTabIndex = tabIndex,
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Tab(
                            selected = tabIndex == 0,
                            onClick = { tabIndex = 0 },
                            text = {
                                Text(
                                    stringResource(Res.string.simulation_tab_canvas_format, availableAssignments.size),
                                    fontWeight = if (tabIndex == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = tabIndex == 1,
                            onClick = { tabIndex = 1 },
                            text = {
                                Text(
                                    stringResource(Res.string.simulation_empty_assignment_tab),
                                    fontWeight = if (tabIndex == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }

                    if (tabIndex == 0) {
                        if (availableAssignments.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(Res.string.simulation_no_canvas_assignments),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(availableAssignments, key = { it.id }) { assignment ->
                                    val rawScore = assignment.score ?: assignment.submission?.score
                                    val maxPoints = assignment.pointsPossible ?: 20.0
                                    val isGraded = rawScore != null

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                onAddExisting(assignment)
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = assignment.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    if (isGraded) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = MaterialTheme.colorScheme.primaryContainer
                                                        ) {
                                                            Text(
                                                                text = if (isGraded) {
                                                                    stringResource(
                                                                        Res.string.simulation_graded_score_format,
                                                                        (rawScore ?: 0.0).toInt(),
                                                                        maxPoints.toInt(),
                                                                    )
                                                                } else {
                                                                    stringResource(Res.string.simulation_ungraded)
                                                                },
                                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    } else {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = MaterialTheme.colorScheme.surfaceVariant
                                                        ) {
                                                            Text(
                                                                text = stringResource(Res.string.simulation_ungraded),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = stringResource(Res.string.simulation_action_add),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = stringResource(Res.string.simulation_placeholder_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = placeholderName,
                                onValueChange = { placeholderName = it },
                                label = { Text(stringResource(Res.string.simulation_placeholder_name_label)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = placeholderScoreText,
                                onValueChange = { placeholderScoreText = it },
                                label = { Text(stringResource(Res.string.simulation_placeholder_score_label)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (tabIndex == 1) {
                    Button(
                        onClick = {
                            val score = placeholderScoreText.toFloatOrNull()?.coerceIn(0f, 20f) ?: 15f
                            if (placeholderName.isNotBlank()) {
                                onAddPlaceholder(placeholderName.trim(), score)
                            }
                        },
                        enabled = placeholderName.isNotBlank()
                    ) {
                        Text(stringResource(Res.string.simulation_create_empty))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.close))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ─── 2. Dialog: Editar Nombre y Porcentaje de Grupo ─────────────────────────
@Composable
fun EditGroupDialog(
    group: SimulationGroup,
    onDismiss: () -> Unit,
    onConfirm: (updatedGroup: SimulationGroup) -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    show: Boolean = true,
    backdrop: Backdrop? = null,
) {
    var name by remember { mutableStateOf(group.name) }
    var weightText by remember { mutableStateOf(group.weightPercentage.roundToInt().toString()) }
    var targetAssessmentsText by remember { mutableStateOf(group.targetAssessments.toString()) }
    var dropLowest by remember { mutableStateOf(group.dropLowest) }
    var minToDropText by remember { mutableStateOf(group.minToDrop.toString()) }
    var isError by remember { mutableStateOf(false) }

    if (themeStyle == AppThemeStyle.MIUIX) {
        GlassDialog(
            show = show,
            backdrop = backdrop,
            title = stringResource(Res.string.simulation_edit_group_title),
            onDismissRequest = onDismiss,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MiuixTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(Res.string.group_create_name_label),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MiuixTextField(
                        value = weightText,
                        onValueChange = {
                            weightText = it
                            isError = false
                        },
                        label = stringResource(Res.string.group_create_weight_label),
                        trailingIcon = {
                            MiuixText(
                                text = stringResource(Res.string.simulation_percent_symbol),
                                modifier = Modifier.padding(end = 8.dp),
                            )
                        },
                        colors = MiuixTextFieldDefaults.textFieldColors(
                            borderColor = if (isError) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.primary,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )

                    MiuixTextField(
                        value = targetAssessmentsText,
                        onValueChange = { targetAssessmentsText = it },
                        label = stringResource(Res.string.group_create_assessments_label),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MiuixTheme.colorScheme.surfaceContainerHigh),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { dropLowest = !dropLowest }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MiuixSwitch(
                        checked = dropLowest,
                        onCheckedChange = null,
                    )
                    Column(modifier = Modifier.padding(start = 6.dp)) {
                        MiuixText(
                            text = stringResource(Res.string.group_create_drop_lowest_title),
                            style = MiuixTheme.textStyles.body2.copy(fontWeight = FontWeight.SemiBold),
                        )
                        MiuixText(
                            text = stringResource(Res.string.group_create_drop_lowest_desc),
                            style = MiuixTheme.textStyles.footnote2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }

                if (dropLowest) {
                    MiuixTextField(
                        value = minToDropText,
                        onValueChange = { minToDropText = it },
                        label = stringResource(Res.string.group_create_min_to_drop_label),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            GlassDialogActions {
                GlassDialogButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    MiuixText(
                        text = stringResource(Res.string.cancel),
                        style = MiuixTheme.textStyles.button,
                    )
                }
                GlassDialogButton(
                    onClick = {
                        val weight = weightText.toFloatOrNull()
                        val target = targetAssessmentsText.toIntOrNull() ?: 1
                        val minDrop = minToDropText.toIntOrNull() ?: 3
                        if (name.isNotBlank() && weight != null && weight > 0f) {
                            onConfirm(
                                group.copy(
                                    name = name.trim(),
                                    weightPercentage = weight,
                                    targetAssessments = target.coerceAtLeast(1),
                                    dropLowest = dropLowest,
                                    minToDrop = minDrop.coerceAtLeast(2)
                                )
                            )
                        } else {
                            isError = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    role = GlassDialogButtonRole.Primary,
                ) {
                    MiuixText(
                        text = stringResource(Res.string.save),
                        style = MiuixTheme.textStyles.button,
                    )
                }
            }
        }
    } else if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.simulation_edit_group_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(Res.string.group_create_name_label)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = {
                                weightText = it
                                isError = false
                            },
                            label = { Text(stringResource(Res.string.group_create_weight_label)) },
                            trailingIcon = {
                                Text(
                                    stringResource(Res.string.simulation_percent_symbol),
                                    modifier = Modifier.padding(end = 8.dp),
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = isError,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = targetAssessmentsText,
                            onValueChange = { targetAssessmentsText = it },
                            label = { Text(stringResource(Res.string.group_create_assessments_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { dropLowest = !dropLowest }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = dropLowest,
                            onCheckedChange = { dropLowest = it }
                        )
                        Column(modifier = Modifier.padding(start = 6.dp)) {
                            Text(
                                text = stringResource(Res.string.group_create_drop_lowest_title),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(Res.string.group_create_drop_lowest_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (dropLowest) {
                        OutlinedTextField(
                            value = minToDropText,
                            onValueChange = { minToDropText = it },
                            label = { Text(stringResource(Res.string.group_create_min_to_drop_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val weight = weightText.toFloatOrNull()
                        val target = targetAssessmentsText.toIntOrNull() ?: 1
                        val minDrop = minToDropText.toIntOrNull() ?: 3
                        if (name.isNotBlank() && weight != null && weight > 0f) {
                            onConfirm(
                                group.copy(
                                    name = name.trim(),
                                    weightPercentage = weight,
                                    targetAssessments = target.coerceAtLeast(1),
                                    dropLowest = dropLowest,
                                    minToDrop = minDrop.coerceAtLeast(2)
                                )
                            )
                        } else {
                            isError = true
                        }
                    }
                ) {
                    Text(stringResource(Res.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ─── 3. Dialog: Vincular Trabajo Vacío con Canvas ───────────────────────────
@Composable
fun LinkPlaceholderDialog(
    placeholderItem: SimulationItem,
    availableAssignments: List<Assignment>,
    onDismiss: () -> Unit,
    onLink: (assignment: Assignment) -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    show: Boolean = true,
    backdrop: Backdrop? = null,
) {
    if (themeStyle == AppThemeStyle.MIUIX) {
        GlassDialog(
            show = show,
            backdrop = backdrop,
            title = stringResource(Res.string.simulation_link_canvas_title),
            summary = stringResource(Res.string.simulation_link_canvas_summary_format, placeholderItem.name),
            onDismissRequest = onDismiss,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (availableAssignments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        MiuixText(
                            text = stringResource(Res.string.simulation_no_canvas_assignments_available),
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(availableAssignments, key = { it.id }) { assignment ->
                            val rawScore = assignment.score ?: assignment.submission?.score
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onLink(assignment) }
                                    .background(MiuixTheme.colorScheme.surfaceContainerHigh),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        MiuixText(
                                            text = assignment.name,
                                            style = MiuixTheme.textStyles.body2.copy(fontWeight = FontWeight.Medium),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        if (rawScore != null) {
                                            MiuixText(
                                                text = stringResource(Res.string.simulation_canvas_score_format, rawScore.toInt()),
                                                style = MiuixTheme.textStyles.footnote2.copy(fontWeight = FontWeight.Bold),
                                                color = MiuixTheme.colorScheme.primary,
                                            )
                                        }
                                    }
                                    MiuixIcon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            GlassDialogActions {
                GlassDialogButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    MiuixText(
                        text = stringResource(Res.string.cancel),
                        style = MiuixTheme.textStyles.button,
                    )
                }
            }
        }
    } else if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.simulation_link_canvas_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(Res.string.simulation_link_canvas_summary_format, placeholderItem.name),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (availableAssignments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.simulation_no_canvas_assignments_available),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(availableAssignments, key = { it.id }) { assignment ->
                                val rawScore = assignment.score ?: assignment.submission?.score
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            onLink(assignment)
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = assignment.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (rawScore != null) {
                                                Text(
                                                    text = stringResource(Res.string.simulation_canvas_score_format, rawScore.toInt()),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ─── 4. Dialog: Crear Grupo Rápido ──────────────────────────────────────────
@Composable
fun AddGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, weight: Float) -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    show: Boolean = true,
    backdrop: Backdrop? = null,
) {
    var name by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }

    if (themeStyle == AppThemeStyle.MIUIX) {
        GlassDialog(
            show = show,
            backdrop = backdrop,
            title = stringResource(Res.string.group_create_sheet_title),
            onDismissRequest = onDismiss,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MiuixTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(Res.string.group_create_name_label),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                MiuixTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = stringResource(Res.string.group_create_weight_label),
                    trailingIcon = {
                        MiuixText(
                            text = stringResource(Res.string.simulation_percent_symbol),
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            GlassDialogActions {
                GlassDialogButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    MiuixText(
                        text = stringResource(Res.string.cancel),
                        style = MiuixTheme.textStyles.button,
                    )
                }
                GlassDialogButton(
                    onClick = {
                        val weight = weightText.toFloatOrNull() ?: 0f
                        if (name.isNotBlank() && weight > 0f) {
                            onConfirm(name.trim(), weight)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = name.isNotBlank() && (weightText.toFloatOrNull() ?: 0f) > 0f,
                    role = GlassDialogButtonRole.Primary,
                ) {
                    MiuixText(
                        text = stringResource(Res.string.groups_add_group_btn),
                        style = MiuixTheme.textStyles.button,
                    )
                }
            }
        }
    } else if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(Res.string.group_create_sheet_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(Res.string.group_create_name_label)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text(stringResource(Res.string.group_create_weight_label)) },
                        trailingIcon = {
                            Text(
                                stringResource(Res.string.simulation_percent_symbol),
                                modifier = Modifier.padding(end = 8.dp),
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val weight = weightText.toFloatOrNull() ?: 0f
                        if (name.isNotBlank() && weight > 0f) {
                            onConfirm(name.trim(), weight)
                        }
                    },
                    enabled = name.isNotBlank() && (weightText.toFloatOrNull() ?: 0f) > 0f
                ) {
                    Text(stringResource(Res.string.groups_add_group_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
