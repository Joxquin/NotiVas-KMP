package me.joxquin.notivas.ui.copilot.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.local.CopilotProvider
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.cancel
import me.joxquin.notivas.shared.resources.close
import me.joxquin.notivas.shared.resources.copilot_custom_instructions_desc
import me.joxquin.notivas.shared.resources.copilot_custom_instructions_placeholder
import me.joxquin.notivas.shared.resources.copilot_custom_instructions_title
import me.joxquin.notivas.shared.resources.copilot_model_label
import me.joxquin.notivas.shared.resources.copilot_provider_dialog_desc
import me.joxquin.notivas.shared.resources.copilot_provider_dialog_title
import me.joxquin.notivas.shared.resources.openrouter_model_dialog_desc
import me.joxquin.notivas.shared.resources.openrouter_model_dialog_title
import me.joxquin.notivas.shared.resources.save
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults as MiuixTextFieldDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

val OPENROUTER_MODELS = listOf(
    "google/gemini-2.5-flash" to "Gemini 2.5 Flash",
    "google/gemini-2.0-flash-exp:free" to "Gemini 2.0 Flash (Free)",
    "meta-llama/llama-3.3-70b-instruct:free" to "Llama 3.3 70B (Free)",
    "anthropic/claude-3.5-haiku" to "Claude 3.5 Haiku",
    "openai/gpt-4o-mini" to "GPT-4o Mini"
)

val GOOGLE_AI_STUDIO_MODELS = listOf(
    "gemini-3.5-flash-lite" to "Gemini 3.5 Flash-Lite",
    "gemini-3.5-flash" to "Gemini 3.5 Flash",
    "gemini-2.5-flash" to "Gemini 2.5 Flash",
    "gemini-3.1-pro-preview" to "Gemini 3.1 Pro"
)

fun resolveModelLabel(provider: CopilotProvider, modelId: String): String {
    val models = if (provider == CopilotProvider.OPENROUTER) OPENROUTER_MODELS else GOOGLE_AI_STUDIO_MODELS
    return models.find { it.first == modelId }?.second ?: modelId
}

@Composable
fun CopilotProviderPickerDialog(
    visible: Boolean,
    currentProvider: CopilotProvider,
    onProviderSelected: (CopilotProvider) -> Unit,
    onDismiss: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL
) {
    if (!visible) return

    val providerOptions = listOf(
        CopilotProvider.OPENROUTER to ("OpenRouter" to "openrouter.ai (Multi-proveedor)"),
        CopilotProvider.GOOGLE_AI_STUDIO to ("Google AI Studio" to "aistudio.google.com (Gemini Oficial)")
    )

    val containerColor = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(Res.string.copilot_provider_dialog_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.copilot_provider_dialog_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                providerOptions.forEach { (provider, info) ->
                    val (title, subtitle) = info
                    val isSelected = provider == currentProvider

                    if (themeStyle == AppThemeStyle.MIUIX) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.defaultColors(
                                color = if (isSelected) MiuixTheme.colorScheme.surfaceContainerHighest else MiuixTheme.colorScheme.surfaceContainerHigh
                            ),
                            pressFeedbackType = PressFeedbackType.Sink,
                            onClick = {
                                onProviderSelected(provider)
                                onDismiss()
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        onProviderSelected(provider)
                                        onDismiss()
                                    }
                                )
                                Column {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onProviderSelected(provider)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        onProviderSelected(provider)
                                        onDismiss()
                                    }
                                )
                                Column {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
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
            if (themeStyle == AppThemeStyle.MIUIX) {
                MiuixTextButton(
                    text = stringResource(Res.string.close),
                    onClick = onDismiss
                )
            } else {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.close))
                }
            }
        },
        containerColor = containerColor
    )
}

@Composable
fun CopilotModelPickerDialog(
    visible: Boolean,
    provider: CopilotProvider,
    currentModel: String,
    onModelSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL
) {
    if (!visible) return

    val models = if (provider == CopilotProvider.OPENROUTER) OPENROUTER_MODELS else GOOGLE_AI_STUDIO_MODELS
    val containerColor = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(Res.string.openrouter_model_dialog_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.openrouter_model_dialog_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                models.forEach { (modelId, label) ->
                    val isSelected = modelId == currentModel

                    if (themeStyle == AppThemeStyle.MIUIX) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.defaultColors(
                                color = if (isSelected) MiuixTheme.colorScheme.surfaceContainerHighest else MiuixTheme.colorScheme.surfaceContainerHigh
                            ),
                            pressFeedbackType = PressFeedbackType.Sink,
                            onClick = {
                                onModelSelected(modelId)
                                onDismiss()
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        onModelSelected(modelId)
                                        onDismiss()
                                    }
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onModelSelected(modelId)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        onModelSelected(modelId)
                                        onDismiss()
                                    }
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            if (themeStyle == AppThemeStyle.MIUIX) {
                MiuixTextButton(
                    text = stringResource(Res.string.close),
                    onClick = onDismiss
                )
            } else {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.close))
                }
            }
        },
        containerColor = containerColor
    )
}

@Composable
fun CopilotCustomInstructionsDialog(
    visible: Boolean,
    initialInstructions: String,
    onSaveInstructions: (String) -> Unit,
    onDismiss: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL
) {
    if (!visible) return

    var instructionsText by remember(initialInstructions, visible) {
        mutableStateOf(initialInstructions)
    }

    val containerColor = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(Res.string.copilot_custom_instructions_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(Res.string.copilot_custom_instructions_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (themeStyle == AppThemeStyle.MIUIX) {
                    MiuixTextField(
                        value = instructionsText,
                        onValueChange = { instructionsText = it },
                        label = stringResource(Res.string.copilot_custom_instructions_title),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        colors = MiuixTextFieldDefaults.textFieldColors()
                    )
                } else {
                    OutlinedTextField(
                        value = instructionsText,
                        onValueChange = { instructionsText = it },
                        label = { Text(stringResource(Res.string.copilot_custom_instructions_title)) },
                        placeholder = { Text(stringResource(Res.string.copilot_custom_instructions_placeholder)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (themeStyle == AppThemeStyle.MIUIX) {
                MiuixTextButton(
                    text = stringResource(Res.string.save),
                    onClick = {
                        onSaveInstructions(instructionsText.trim())
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
            } else {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onSaveInstructions(instructionsText.trim())
                        onDismiss()
                    }
                ) {
                    Text(stringResource(Res.string.save))
                }
            }
        },
        dismissButton = {
            if (themeStyle == AppThemeStyle.MIUIX) {
                MiuixTextButton(
                    text = stringResource(Res.string.cancel),
                    onClick = onDismiss
                )
            } else {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        },
        containerColor = containerColor
    )
}
