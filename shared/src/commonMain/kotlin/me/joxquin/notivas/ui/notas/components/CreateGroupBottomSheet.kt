package me.joxquin.notivas.ui.notas.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.group_create_assessments_label
import me.joxquin.notivas.shared.resources.group_create_drop_lowest_desc
import me.joxquin.notivas.shared.resources.group_create_drop_lowest_title
import me.joxquin.notivas.shared.resources.group_create_exceeded_weight_format
import me.joxquin.notivas.shared.resources.group_create_min_to_drop_label
import me.joxquin.notivas.shared.resources.group_create_name_label
import me.joxquin.notivas.shared.resources.group_create_remaining_weight_format
import me.joxquin.notivas.shared.resources.group_create_save_btn
import me.joxquin.notivas.shared.resources.group_create_sheet_title
import me.joxquin.notivas.shared.resources.group_create_weight_label
import me.joxquin.notivas.shared.resources.unreviewed_dialog_cancel
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import kotlin.math.abs

@Composable
fun CreateGroupBottomSheet(
    currentTotalWeight: Float,
    onDismiss: () -> Unit,
    onSave: (name: String, weight: Float, targetAssessments: Int, dropLowest: Boolean, minToDrop: Int) -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL
) {
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

    val fieldColors = if (isMiuix) {
        TextFieldDefaults.textFieldColors()
    } else {
        TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            borderColor = MaterialTheme.colorScheme.primary
        )
    }

    val weightFieldColors = if (isExceeded) {
        if (isMiuix) {
            TextFieldDefaults.textFieldColors(
                backgroundColor = MiuixTheme.colorScheme.errorContainer,
                labelColor = MiuixTheme.colorScheme.onErrorContainer,
                borderColor = MiuixTheme.colorScheme.error
            )
        } else {
            TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                labelColor = MaterialTheme.colorScheme.onErrorContainer,
                borderColor = MaterialTheme.colorScheme.error
            )
        }
    } else {
        fieldColors
    }

    WindowBottomSheet(
        title = stringResource(Res.string.group_create_sheet_title),
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
            // Campo 1: Nombre del Grupo
            TextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(Res.string.group_create_name_label),
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            // Campos 2 y 3: Peso (%) y N° de Notas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(
                    value = weightInput,
                    onValueChange = { weightInput = it.filter { char -> char.isDigit() || char == '.' } },
                    label = stringResource(Res.string.group_create_weight_label),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = weightFieldColors,
                    modifier = Modifier.weight(1f)
                )

                TextField(
                    value = assessmentsCountInput,
                    onValueChange = { assessmentsCountInput = it.filter { char -> char.isDigit() } },
                    label = stringResource(Res.string.group_create_assessments_label),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.weight(1f)
                )
            }

            // Feedback dinámico de peso restante
            if (isExceeded) {
                Text(
                    text = stringResource(
                        Res.string.group_create_exceeded_weight_format,
                        abs(remainingWeight).toInt(),
                        (currentTotalWeight + parsedWeight).toInt()
                    ),
                    fontSize = 12.sp,
                    color = if (isMiuix) MiuixTheme.colorScheme.error else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            } else {
                Text(
                    text = stringResource(
                        Res.string.group_create_remaining_weight_format,
                        remainingWeight.toInt()
                    ),
                    fontSize = 12.sp,
                    color = if (remainingWeight == 0f) {
                        if (isMiuix) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                    } else {
                        if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // SwitchPreference: Descartar la nota más baja
            MiuixCard(
                modifier = Modifier.fillMaxWidth(),
                colors = MiuixCardDefaults.defaultColors(
                    color = if (isMiuix) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainer
                ),
                insideMargin = PaddingValues(0.dp)
            ) {
                SwitchPreference(
                    title = stringResource(Res.string.group_create_drop_lowest_title),
                    summary = stringResource(Res.string.group_create_drop_lowest_desc),
                    checked = dropLowest,
                    onCheckedChange = { dropLowest = it }
                )
            }

            if (dropLowest) {
                TextField(
                    value = minToDropInput,
                    onValueChange = { minToDropInput = it.filter { char -> char.isDigit() } },
                    label = stringResource(Res.string.group_create_min_to_drop_label),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(4.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiuixTextButton(
                    text = stringResource(Res.string.unreviewed_dialog_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                MiuixTextButton(
                    text = stringResource(Res.string.group_create_save_btn),
                    onClick = {
                        val assessmentsCount = assessmentsCountInput.toIntOrNull() ?: 1
                        val minToDrop = minToDropInput.toIntOrNull() ?: 3
                        onSave(name.trim(), parsedWeight, assessmentsCount, dropLowest, minToDrop)
                    },
                    enabled = isFormValid,
                    modifier = Modifier.weight(1f),
                    colors = MiuixButtonDefaults.textButtonColorsPrimary()
                )
            }
        }
    }
}
