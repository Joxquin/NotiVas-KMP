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
import me.joxquin.notivas.shared.resources.copilot_provider_dialog_desc
import me.joxquin.notivas.shared.resources.copilot_provider_dialog_title
import me.joxquin.notivas.shared.resources.openrouter_model_dialog_desc
import me.joxquin.notivas.shared.resources.openrouter_model_dialog_title
import me.joxquin.notivas.shared.resources.save
import me.joxquin.notivas.ui.components.miuix.GlassDialogActions
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.RadioButton as MiuixRadioButton
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults as MiuixTextFieldDefaults
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassDialog
import top.yukonga.miuix.kmp.glass.GlassDialogButton
import top.yukonga.miuix.kmp.glass.GlassDialogButtonRole
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
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    backdrop: Backdrop? = null,
) {
    val providerOptions = listOf(
        CopilotProvider.OPENROUTER to ("OpenRouter" to "openrouter.ai (Multi-proveedor)"),
        CopilotProvider.GOOGLE_AI_STUDIO to ("Google AI Studio" to "aistudio.google.com (Gemini Oficial)")
    )

    if (themeStyle == AppThemeStyle.MIUIX) {
        GlassDialog(
            show = visible,
            backdrop = backdrop,
            title = stringResource(Res.string.copilot_provider_dialog_title),
            summary = stringResource(Res.string.copilot_provider_dialog_desc),
            onDismissRequest = onDismiss,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                providerOptions.forEach { (provider, info) ->
                    val (title, subtitle) = info
                    val isSelected = provider == currentProvider
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.defaultColors(
                            color = if (isSelected) MiuixTheme.colorScheme.surfaceContainerHighest else MiuixTheme.colorScheme.surfaceContainerHigh
                        ),
                        pressFeedbackType = PressFeedbackType.Sink,
                        onClick = {
                            if (visible) {
                                onProviderSelected(provider)
                                onDismiss()
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MiuixRadioButton(
                                selected = isSelected,
                                onClick = {
                                    if (visible) {
                                        onProviderSelected(provider)
                                        onDismiss()
                                    }
                                }
                            )
                            Column {
                                MiuixText(
                                    text = title,
                                    style = MiuixTheme.textStyles.body1.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                                )
                                MiuixText(
                                    text = subtitle,
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            }
                        }
                    }
                }
                GlassDialogActions {
                    GlassDialogButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss,
                    ) {
                        MiuixText(
                            text = stringResource(Res.string.close),
                            style = MiuixTheme.textStyles.button,
                        )
                    }
                }
            }
        }
    }

    if (themeStyle != AppThemeStyle.MIUIX && visible) {
        val containerColor = MaterialTheme.colorScheme.surfaceContainer

        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.copilot_provider_dialog_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.copilot_provider_dialog_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    providerOptions.forEach { (provider, info) ->
                        val (title, subtitle) = info
                        val isSelected = provider == currentProvider

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
            },
            confirmButton = {},
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.close))
                }
            },
            containerColor = containerColor
        )
    }
}

@Composable
fun CopilotModelPickerDialog(
    visible: Boolean,
    provider: CopilotProvider,
    currentModel: String,
    onModelSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    backdrop: Backdrop? = null,
) {
    val models = if (provider == CopilotProvider.OPENROUTER) OPENROUTER_MODELS else GOOGLE_AI_STUDIO_MODELS

    if (themeStyle == AppThemeStyle.MIUIX) {
        GlassDialog(
            show = visible,
            backdrop = backdrop,
            title = stringResource(Res.string.openrouter_model_dialog_title),
            summary = stringResource(Res.string.openrouter_model_dialog_desc),
            onDismissRequest = onDismiss,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                models.forEach { (modelId, label) ->
                    val isSelected = modelId == currentModel
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.defaultColors(
                            color = if (isSelected) MiuixTheme.colorScheme.surfaceContainerHighest else MiuixTheme.colorScheme.surfaceContainerHigh
                        ),
                        pressFeedbackType = PressFeedbackType.Sink,
                        onClick = {
                            if (visible) {
                                onModelSelected(modelId)
                                onDismiss()
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MiuixRadioButton(
                                selected = isSelected,
                                onClick = {
                                    if (visible) {
                                        onModelSelected(modelId)
                                        onDismiss()
                                    }
                                }
                            )
                            MiuixText(
                                text = label,
                                style = MiuixTheme.textStyles.body1.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                GlassDialogActions {
                    GlassDialogButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss,
                    ) {
                        MiuixText(
                            text = stringResource(Res.string.close),
                            style = MiuixTheme.textStyles.button,
                        )
                    }
                }
            }
        }
    }

    if (themeStyle != AppThemeStyle.MIUIX && visible) {
        val containerColor = MaterialTheme.colorScheme.surfaceContainer

        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.openrouter_model_dialog_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.openrouter_model_dialog_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    models.forEach { (modelId, label) ->
                        val isSelected = modelId == currentModel

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
            },
            confirmButton = {},
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.close))
                }
            },
            containerColor = containerColor
        )
    }
}

@Composable
fun CopilotCustomInstructionsDialog(
    visible: Boolean,
    initialInstructions: String,
    onSaveInstructions: (String) -> Unit,
    onDismiss: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    backdrop: Backdrop? = null,
) {
    var instructionsText by remember(initialInstructions, visible) {
        mutableStateOf(initialInstructions)
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        GlassDialog(
            show = visible,
            backdrop = backdrop,
            title = stringResource(Res.string.copilot_custom_instructions_title),
            summary = stringResource(Res.string.copilot_custom_instructions_desc),
            onDismissRequest = onDismiss,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MiuixTextField(
                    value = instructionsText,
                    onValueChange = { instructionsText = it },
                    label = stringResource(Res.string.copilot_custom_instructions_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    colors = MiuixTextFieldDefaults.textFieldColors()
                )
                GlassDialogActions {
                    GlassDialogButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss,
                    ) {
                        MiuixText(
                            text = stringResource(Res.string.cancel),
                            style = MiuixTheme.textStyles.button,
                        )
                    }
                    GlassDialogButton(
                        modifier = Modifier.weight(1f),
                        role = GlassDialogButtonRole.Primary,
                        onClick = {
                            if (visible) {
                                onSaveInstructions(instructionsText.trim())
                                onDismiss()
                            }
                        },
                    ) {
                        MiuixText(
                            text = stringResource(Res.string.save),
                            style = MiuixTheme.textStyles.button,
                        )
                    }
                }
            }
        }
    }

    if (themeStyle != AppThemeStyle.MIUIX && visible) {
        val containerColor = MaterialTheme.colorScheme.surfaceContainer

        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.copilot_custom_instructions_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(Res.string.copilot_custom_instructions_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

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
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onSaveInstructions(instructionsText.trim())
                        onDismiss()
                    }
                ) {
                    Text(stringResource(Res.string.save))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            containerColor = containerColor
        )
    }
}
