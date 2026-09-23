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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationItem
import kotlin.math.roundToInt

// ─── 1. Dialog: Añadir Evaluación (Canvas vs Vacío) ─────────────────────────
@Composable
fun AddAssignmentDialog(
    groupName: String,
    availableAssignments: List<Assignment>,
    onDismiss: () -> Unit,
    onAddExisting: (Assignment) -> Unit,
    onAddPlaceholder: (name: String, initialScore: Float) -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    var placeholderName by remember { mutableStateOf("") }
    var placeholderScoreText by remember { mutableStateOf("15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Añadir a $groupName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Selecciona una tarea de Canvas o crea una evaluación manual",
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
                                "De Canvas (${availableAssignments.size})",
                                fontWeight = if (tabIndex == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = tabIndex == 1,
                        onClick = { tabIndex = 1 },
                        text = {
                            Text(
                                "Trabajo Vacío",
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
                                text = "No hay tareas de Canvas sincronizadas para este curso.",
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
                                                            text = "Calificado: ${rawScore?.toInt()} / ${maxPoints.toInt()}",
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
                                                            text = "Por calificar",
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
                                            contentDescription = "Añadir",
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
                            text = "Crea una evaluación provisional (ej. \"Lab 04\" o \"Examen Parcial\"). Cuando el docente la publique en Canvas, podrás vincularla con un toque.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = placeholderName,
                            onValueChange = { placeholderName = it },
                            label = { Text("Nombre de la evaluación") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = placeholderScoreText,
                            onValueChange = { placeholderScoreText = it },
                            label = { Text("Nota esperada (0-20)") },
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
                    Text("Crear Vacío")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// ─── 2. Dialog: Editar Nombre y Porcentaje de Grupo ─────────────────────────
@Composable
fun EditGroupDialog(
    group: SimulationGroup,
    onDismiss: () -> Unit,
    onConfirm: (updatedGroup: SimulationGroup) -> Unit
) {
    var name by remember { mutableStateOf(group.name) }
    var weightText by remember { mutableStateOf(group.weightPercentage.roundToInt().toString()) }
    var targetAssessmentsText by remember { mutableStateOf(group.targetAssessments.toString()) }
    var dropLowest by remember { mutableStateOf(group.dropLowest) }
    var minToDropText by remember { mutableStateOf(group.minToDrop.toString()) }
    var isError by remember { mutableStateOf(false) }

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
                    text = "Editar Grupo de Evaluación",
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
                    label = { Text("Nombre del grupo") },
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
                        label = { Text("Peso (%)") },
                        trailingIcon = { Text("%", modifier = Modifier.padding(end = 8.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = isError,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = targetAssessmentsText,
                        onValueChange = { targetAssessmentsText = it },
                        label = { Text("Total notas") },
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
                            text = "Descartar la nota más baja",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Elimina automáticamente la peor calificación del promedio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (dropLowest) {
                    OutlinedTextField(
                        value = minToDropText,
                        onValueChange = { minToDropText = it },
                        label = { Text("Mínimo de notas para activar descarte") },
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
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// ─── 3. Dialog: Vincular Trabajo Vacío con Canvas ───────────────────────────
@Composable
fun LinkPlaceholderDialog(
    placeholderItem: SimulationItem,
    availableAssignments: List<Assignment>,
    onDismiss: () -> Unit,
    onLink: (assignment: Assignment) -> Unit
) {
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
                    text = "Vincular con Canvas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Selecciona la tarea publicada en Canvas para vincularla a \"${placeholderItem.name}\":",
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
                            text = "No se encontraron tareas disponibles en Canvas.",
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
                                                text = "Nota Canvas: ${rawScore.toInt()}",
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
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// ─── 4. Dialog: Crear Grupo Rápido ──────────────────────────────────────────
@Composable
fun AddGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, weight: Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nuevo Grupo de Evaluación",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej. Laboratorios)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Porcentaje de peso (ej. 40)") },
                    trailingIcon = { Text("%", modifier = Modifier.padding(end = 8.dp)) },
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
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
