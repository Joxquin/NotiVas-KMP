package me.joxquin.notivas.ui.notas.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupBottomSheet(
    currentTotalWeight: Float,
    onDismiss: () -> Unit,
    onSave: (name: String, weight: Float, targetAssessments: Int, dropLowest: Boolean, minToDrop: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }
    var assessmentsCountInput by remember { mutableStateOf("4") }
    var dropLowest by remember { mutableStateOf(false) }
    var minToDropInput by remember { mutableStateOf("3") }

    val parsedWeight = weightInput.toFloatOrNull() ?: 0f
    val remainingWeight = 100f - (currentTotalWeight + parsedWeight)
    val isExceeded = remainingWeight < -0.01f
    val isWeightValid = parsedWeight > 0f && !isExceeded
    val isFormValid = name.isNotBlank() && isWeightValid && (assessmentsCountInput.toIntOrNull() ?: 0) > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AddCircleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nuevo Grupo de Evaluación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del grupo (ej. Laboratorios)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Peso (%)") },
                    trailingIcon = {
                        Icon(imageVector = Icons.Outlined.Percent, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    isError = isExceeded
                )

                OutlinedTextField(
                    value = assessmentsCountInput,
                    onValueChange = { assessmentsCountInput = it.filter { char -> char.isDigit() } },
                    label = { Text("N° Notas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Feedback de peso interactivo en tiempo real
            if (isExceeded) {
                Text(
                    text = "Excede el 100% por ${abs(remainingWeight).toInt()}% (Suma actual: ${(currentTotalWeight + parsedWeight).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "Queda ${remainingWeight.toInt()}% libre para otros grupos",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remainingWeight == 0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Checkbox de Descarte de la peor nota
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = dropLowest,
                    onCheckedChange = { dropLowest = it }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Descartar la nota más baja",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Elimina automáticamente la menor nota del promedio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (dropLowest) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = minToDropInput,
                    onValueChange = { minToDropInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Mínimo de notas rendidas para aplicar descarte") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val assessmentsCount = assessmentsCountInput.toIntOrNull() ?: 1
                        val minToDrop = minToDropInput.toIntOrNull() ?: 3
                        onSave(name.trim(), parsedWeight, assessmentsCount, dropLowest, minToDrop)
                    },
                    enabled = isFormValid,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Guardar Grupo")
                }
            }
        }
    }
}
