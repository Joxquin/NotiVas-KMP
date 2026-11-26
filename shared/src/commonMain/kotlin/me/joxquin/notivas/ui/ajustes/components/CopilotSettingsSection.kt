package me.joxquin.notivas.ui.ajustes.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.repository.OpenRouterAccountBalance
import me.joxquin.notivas.ui.components.miuix.GlassDialogActions
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator as MiuixCircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.RadioButton as MiuixRadioButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.glass.GlassDialog
import top.yukonga.miuix.kmp.glass.GlassDialogButton
import top.yukonga.miuix.kmp.glass.GlassDialogButtonRole
import top.yukonga.miuix.kmp.glass.GlassTextField
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

import org.jetbrains.compose.resources.stringResource
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.settings_copilot_section_title
import me.joxquin.notivas.shared.resources.copilot_enable_title
import me.joxquin.notivas.shared.resources.copilot_enable_desc
import me.joxquin.notivas.shared.resources.openrouter_api_key_title
import me.joxquin.notivas.shared.resources.openrouter_api_key_desc_not_set
import me.joxquin.notivas.shared.resources.openrouter_api_key_dialog_title
import me.joxquin.notivas.shared.resources.openrouter_api_key_dialog_desc
import me.joxquin.notivas.shared.resources.openrouter_api_key_placeholder
import me.joxquin.notivas.shared.resources.openrouter_model_title
import me.joxquin.notivas.shared.resources.openrouter_model_dialog_title
import me.joxquin.notivas.shared.resources.openrouter_model_dialog_desc
import me.joxquin.notivas.shared.resources.openrouter_balance_title
import me.joxquin.notivas.shared.resources.tokens_used_format
import me.joxquin.notivas.shared.resources.openrouter_analyze_model_btn
import me.joxquin.notivas.shared.resources.openrouter_analyzing_model
import me.joxquin.notivas.shared.resources.openrouter_custom_model_hint
import me.joxquin.notivas.shared.resources.openrouter_custom_model_title
import me.joxquin.notivas.shared.resources.openrouter_model_context
import me.joxquin.notivas.shared.resources.openrouter_model_info_title
import me.joxquin.notivas.shared.resources.save
import me.joxquin.notivas.shared.resources.delete
import me.joxquin.notivas.shared.resources.cancel
import me.joxquin.notivas.shared.resources.close
import me.joxquin.notivas.shared.resources.change
import me.joxquin.notivas.shared.resources.sync

import me.joxquin.notivas.data.local.CopilotProvider
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassDropdownPreference
import me.joxquin.notivas.shared.resources.copilot_provider_title
import me.joxquin.notivas.shared.resources.copilot_provider_summary
import me.joxquin.notivas.shared.resources.copilot_provider_dialog_title
import me.joxquin.notivas.shared.resources.copilot_provider_dialog_desc
import me.joxquin.notivas.shared.resources.copilot_provider_openrouter_label
import me.joxquin.notivas.shared.resources.copilot_provider_google_ai_studio_label
import me.joxquin.notivas.shared.resources.copilot_model_analysis_gemini_compatible
import me.joxquin.notivas.shared.resources.copilot_model_analysis_custom_compatible
import me.joxquin.notivas.shared.resources.google_ai_studio_api_key_title
import me.joxquin.notivas.shared.resources.google_ai_studio_api_key_desc_not_set
import me.joxquin.notivas.shared.resources.google_ai_studio_api_key_dialog_title
import me.joxquin.notivas.shared.resources.google_ai_studio_api_key_dialog_desc
import me.joxquin.notivas.shared.resources.google_ai_studio_api_key_placeholder
import me.joxquin.notivas.shared.resources.google_ai_studio_model_title
import me.joxquin.notivas.shared.resources.google_ai_studio_model_dialog_title
import me.joxquin.notivas.shared.resources.google_ai_studio_model_dialog_desc
import me.joxquin.notivas.shared.resources.google_ai_studio_custom_model_hint
import me.joxquin.notivas.shared.resources.google_ai_studio_balance_title

@Composable
fun CopilotSettingsSection(
    enabled: Boolean,
    provider: CopilotProvider = CopilotProvider.OPENROUTER,
    apiKey: String?,
    selectedModel: String,
    googleAiStudioApiKey: String? = null,
    googleAiStudioModel: String = "gemini-3.5-flash-lite",
    totalTokens: Long,
    balance: OpenRouterAccountBalance?,
    isLoadingBalance: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onProviderChange: (CopilotProvider) -> Unit = {},
    onApiKeyChange: (String?) -> Unit,
    onModelChange: (String) -> Unit,
    onGoogleAiStudioApiKeyChange: (String?) -> Unit = {},
    onGoogleAiStudioModelChange: (String) -> Unit = {},
    onRefreshBalance: () -> Unit,
    onAnalyzeModel: (suspend (String) -> me.joxquin.notivas.data.model.CustomModelAnalysisResult)? = null,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    backdrop: Backdrop? = null,
    modifier: Modifier = Modifier
) {
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }
    var showGoogleAiStudioApiKeyDialog by remember { mutableStateOf(false) }
    var showGoogleAiStudioModelDialog by remember { mutableStateOf(false) }
    var showProviderDialog by remember { mutableStateOf(false) }
    var modelInfoResult by remember { mutableStateOf<me.joxquin.notivas.data.model.CustomModelAnalysisResult?>(null) }
    // Resolver los textos fuera de los callbacks no composables de los diálogos.
    val geminiCompatibleMessage = stringResource(Res.string.copilot_model_analysis_gemini_compatible)
    val customCompatibleMessage = stringResource(Res.string.copilot_model_analysis_custom_compatible)

    val openRouterModels = remember {
        listOf(
            "google/gemini-2.5-flash" to "Gemini 2.5 Flash",
            "google/gemini-2.0-flash-exp:free" to "Gemini 2.0 Flash (Free)",
            "meta-llama/llama-3.3-70b-instruct:free" to "Llama 3.3 70B (Free)",
            "anthropic/claude-3.5-haiku" to "Claude 3.5 Haiku",
            "openai/gpt-4o-mini" to "GPT-4o Mini"
        )
    }

    val googleAiStudioModels = remember {
        listOf(
            "gemini-3.5-flash-lite" to "Gemini 3.5 Flash-Lite (Gratis / Rápido)",
            "gemini-3.5-flash" to "Gemini 3.5 Flash (Recomendado)",
            "gemini-2.5-flash" to "Gemini 2.5 Flash",
            "gemini-3.1-pro-preview" to "Gemini 3.1 Pro"
        )
    }

    val currentOpenRouterModelLabel = remember(selectedModel) {
        openRouterModels.find { it.first == selectedModel }?.second ?: selectedModel
    }

    val currentGoogleAiStudioModelLabel = remember(googleAiStudioModel) {
        googleAiStudioModels.find { it.first == googleAiStudioModel }?.second ?: googleAiStudioModel
    }

    val notConfiguredLabel = stringResource(Res.string.openrouter_api_key_desc_not_set)
    val notConfiguredGoogleLabel = stringResource(Res.string.google_ai_studio_api_key_desc_not_set)

    if (themeStyle == AppThemeStyle.MIUIX) {
        GlassProviderPickerDialog(
            show = showProviderDialog,
            backdrop = backdrop,
            currentProvider = provider,
            options = listOf(
                CopilotProvider.OPENROUTER to stringResource(Res.string.copilot_provider_openrouter_label),
                CopilotProvider.GOOGLE_AI_STUDIO to stringResource(Res.string.copilot_provider_google_ai_studio_label),
            ),
            onProviderSelected = { option ->
                onProviderChange(option)
                showProviderDialog = false
            },
            onDismissRequest = { showProviderDialog = false },
        )

        GlassApiKeyDialog(
            show = showGoogleAiStudioApiKeyDialog,
            backdrop = backdrop,
            title = stringResource(Res.string.google_ai_studio_api_key_dialog_title),
            summary = stringResource(Res.string.google_ai_studio_api_key_dialog_desc),
            fieldLabel = stringResource(Res.string.google_ai_studio_api_key_title),
            currentKey = googleAiStudioApiKey,
            showDelete = !googleAiStudioApiKey.isNullOrBlank(),
            onSave = { value ->
                onGoogleAiStudioApiKeyChange(value.trim().ifBlank { null })
                showGoogleAiStudioApiKeyDialog = false
            },
            onDelete = {
                onGoogleAiStudioApiKeyChange(null)
                showGoogleAiStudioApiKeyDialog = false
            },
            onDismissRequest = { showGoogleAiStudioApiKeyDialog = false },
        )

        GlassModelPickerDialog(
            show = showGoogleAiStudioModelDialog,
            backdrop = backdrop,
            title = stringResource(Res.string.google_ai_studio_model_dialog_title),
            summary = stringResource(Res.string.google_ai_studio_model_dialog_desc),
            customFieldLabel = stringResource(Res.string.google_ai_studio_custom_model_hint),
            models = googleAiStudioModels,
            currentModel = googleAiStudioModel,
            onSave = { finalModel, isCustom ->
                if (finalModel.isNotBlank()) {
                    onGoogleAiStudioModelChange(finalModel)
                    if (isCustom) {
                        val clean = finalModel.lowercase()
                        val isKnown = clean.startsWith("gemini-")
                        modelInfoResult = me.joxquin.notivas.data.model.CustomModelAnalysisResult(
                            modelId = finalModel,
                            name = finalModel,
                            contextLength = if (clean.contains("1.5") || clean.contains("2.0") || clean.contains("2.5") || clean.contains("3.0") || clean.contains("3.1") || clean.contains("3.5")) 1000000 else 32768,
                            isMultimodal = isKnown,
                            supportsFunctionCalling = true,
                            status = if (isKnown) me.joxquin.notivas.data.model.ModelCompatibilityStatus.OPTIMAL else me.joxquin.notivas.data.model.ModelCompatibilityStatus.COMPATIBLE,
                            message = if (isKnown) geminiCompatibleMessage else customCompatibleMessage
                        )
                    }
                }
                showGoogleAiStudioModelDialog = false
            },
            onDismissRequest = { showGoogleAiStudioModelDialog = false },
        )

        GlassApiKeyDialog(
            show = showApiKeyDialog,
            backdrop = backdrop,
            title = stringResource(Res.string.openrouter_api_key_dialog_title),
            summary = stringResource(Res.string.openrouter_api_key_dialog_desc),
            fieldLabel = stringResource(Res.string.openrouter_api_key_title),
            currentKey = apiKey,
            showDelete = !apiKey.isNullOrBlank(),
            onSave = { value ->
                onApiKeyChange(value.trim().ifBlank { null })
                showApiKeyDialog = false
            },
            onDelete = {
                onApiKeyChange(null)
                showApiKeyDialog = false
            },
            onDismissRequest = { showApiKeyDialog = false },
        )

        GlassModelInfoDialog(
            show = modelInfoResult != null,
            info = modelInfoResult,
            backdrop = backdrop,
            onDismissRequest = { modelInfoResult = null },
        )

        GlassModelPickerDialog(
            show = showModelDialog,
            backdrop = backdrop,
            title = stringResource(Res.string.openrouter_model_dialog_title),
            summary = stringResource(Res.string.openrouter_model_dialog_desc),
            customFieldLabel = stringResource(Res.string.openrouter_custom_model_hint),
            models = openRouterModels,
            currentModel = selectedModel,
            onSave = { finalModel, isCustom ->
                if (finalModel.isNotBlank()) {
                    onModelChange(finalModel)
                    if (isCustom && onAnalyzeModel != null) {
                        val result = onAnalyzeModel(finalModel)
                        modelInfoResult = result
                    }
                }
                showModelDialog = false
            },
            onDismissRequest = { showModelDialog = false },
        )
    }

    val openRouterKeyMasked = if (apiKey.isNullOrBlank()) {
        notConfiguredLabel
    } else {
        val visibleChars = 4
        if (apiKey.length > visibleChars * 2) {
            "${apiKey.take(visibleChars)}••••••••${apiKey.takeLast(visibleChars)}"
        } else {
            "••••••••••••"
        }
    }

    val googleAiStudioKeyMasked = if (googleAiStudioApiKey.isNullOrBlank()) {
        notConfiguredGoogleLabel
    } else {
        val visibleChars = 4
        if (googleAiStudioApiKey.length > visibleChars * 2) {
            "${googleAiStudioApiKey.take(visibleChars)}••••••••${googleAiStudioApiKey.takeLast(visibleChars)}"
        } else {
            "••••••••••••"
        }
    }

    // Dialog: Proveedor de IA (Material 3)
    if (themeStyle != AppThemeStyle.MIUIX && showProviderDialog) {
        val providerOptions = listOf(
            CopilotProvider.OPENROUTER to "OpenRouter",
            CopilotProvider.GOOGLE_AI_STUDIO to "Google AI Studio"
        )
        AlertDialog(
            onDismissRequest = { showProviderDialog = false },
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
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
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
                    providerOptions.forEach { (option, label) ->
                        val isSelected = option == provider
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onProviderChange(option)
                                    showProviderDialog = false
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
                                        onProviderChange(option)
                                        showProviderDialog = false
                                    }
                                )
                                Column {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    Text(
                                        text = if (option == CopilotProvider.OPENROUTER) "openrouter.ai" else "aistudio.google.com (Gemini)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showProviderDialog = false }) {
                    Text(stringResource(Res.string.close))
                }
            }
        )
    }

    // Dialog: Configurar API Key Google AI Studio
    if (themeStyle != AppThemeStyle.MIUIX && showGoogleAiStudioApiKeyDialog) {
        var inputKey by remember { mutableStateOf(googleAiStudioApiKey ?: "") }
        var isPasswordVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showGoogleAiStudioApiKeyDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.google_ai_studio_api_key_dialog_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(Res.string.google_ai_studio_api_key_dialog_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.google_ai_studio_api_key_title)) },
                        placeholder = { Text(stringResource(Res.string.google_ai_studio_api_key_placeholder)) },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = inputKey.trim()
                        onGoogleAiStudioApiKeyChange(trimmed.ifBlank { null })
                        showGoogleAiStudioApiKeyDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(Res.string.save))
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!googleAiStudioApiKey.isNullOrBlank()) {
                        TextButton(
                            onClick = {
                                onGoogleAiStudioApiKeyChange(null)
                                showGoogleAiStudioApiKeyDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(Res.string.delete))
                        }
                    }
                    TextButton(onClick = { showGoogleAiStudioApiKeyDialog = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        )
    }

    // Dialog: Seleccionar Modelo Google AI Studio
    if (themeStyle != AppThemeStyle.MIUIX && showGoogleAiStudioModelDialog) {
        var selectedOption by remember { mutableStateOf(googleAiStudioModel) }
        var isCustom by remember {
            mutableStateOf(googleAiStudioModels.none { it.first == googleAiStudioModel })
        }
        var customInput by remember {
            mutableStateOf(if (isCustom) googleAiStudioModel else "")
        }

        AlertDialog(
            onDismissRequest = { showGoogleAiStudioModelDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.google_ai_studio_model_dialog_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.google_ai_studio_model_dialog_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    googleAiStudioModels.forEach { (modelId, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    isCustom = false
                                    selectedOption = modelId
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !isCustom && selectedOption == modelId,
                                onClick = {
                                    isCustom = false
                                    selectedOption = modelId
                                }
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    // Opcion personalizada
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isCustom = true }
                            .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isCustom,
                            onClick = { isCustom = true }
                        )
                        Text(
                            text = stringResource(Res.string.openrouter_custom_model_title),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    if (isCustom) {
                        OutlinedTextField(
                            value = customInput,
                            onValueChange = { customInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(Res.string.google_ai_studio_custom_model_hint)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalModel = if (isCustom) customInput.trim() else selectedOption
                        if (finalModel.isNotBlank()) {
                            onGoogleAiStudioModelChange(finalModel)
                            if (isCustom) {
                                val clean = finalModel.lowercase()
                                val isKnown = clean.startsWith("gemini-")
                                modelInfoResult = me.joxquin.notivas.data.model.CustomModelAnalysisResult(
                                    modelId = finalModel,
                                    name = finalModel,
                                    contextLength = if (clean.contains("1.5") || clean.contains("2.0") || clean.contains("2.5") || clean.contains("3.0") || clean.contains("3.1") || clean.contains("3.5")) 1000000 else 32768,
                                    isMultimodal = isKnown,
                                    supportsFunctionCalling = true,
                                    status = if (isKnown) me.joxquin.notivas.data.model.ModelCompatibilityStatus.OPTIMAL else me.joxquin.notivas.data.model.ModelCompatibilityStatus.COMPATIBLE,
                                    message = if (isKnown) geminiCompatibleMessage else customCompatibleMessage
                                )
                            }
                        }
                        showGoogleAiStudioModelDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(Res.string.save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showGoogleAiStudioModelDialog = false }
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    // Dialog: Configurar API Key
    if (themeStyle != AppThemeStyle.MIUIX && showApiKeyDialog) {
        var inputKey by remember { mutableStateOf(apiKey ?: "") }
        var isPasswordVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.openrouter_api_key_dialog_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(Res.string.openrouter_api_key_dialog_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.openrouter_api_key_title)) },
                        placeholder = { Text(stringResource(Res.string.openrouter_api_key_placeholder)) },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = inputKey.trim()
                        onApiKeyChange(trimmed.ifBlank { null })
                        showApiKeyDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(Res.string.save))
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!apiKey.isNullOrBlank()) {
                        TextButton(
                            onClick = {
                                onApiKeyChange(null)
                                showApiKeyDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(Res.string.delete))
                        }
                    }
                    TextButton(onClick = { showApiKeyDialog = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        )
    }

    // Dialog flotante de informacion del modelo
    if (themeStyle != AppThemeStyle.MIUIX && modelInfoResult != null) {
        val info = modelInfoResult!!
        val infoContainerColor = when (info.status) {
            me.joxquin.notivas.data.model.ModelCompatibilityStatus.OPTIMAL ->
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            me.joxquin.notivas.data.model.ModelCompatibilityStatus.COMPATIBLE ->
                MaterialTheme.colorScheme.surfaceContainerHigh
            me.joxquin.notivas.data.model.ModelCompatibilityStatus.INCOMPATIBLE,
            me.joxquin.notivas.data.model.ModelCompatibilityStatus.NOT_FOUND ->
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            me.joxquin.notivas.data.model.ModelCompatibilityStatus.ERROR ->
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        }

        AlertDialog(
            onDismissRequest = { modelInfoResult = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.openrouter_model_info_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = infoContainerColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = info.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (info.contextLength != null && info.contextLength > 0) {
                            Text(
                                text = stringResource(Res.string.openrouter_model_context, info.contextLength),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = info.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { modelInfoResult = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(Res.string.close))
                }
            }
        )
    }

    // Dialog: Seleccionar Modelo LLM
    if (themeStyle != AppThemeStyle.MIUIX && showModelDialog) {
        val coroutineScope = rememberCoroutineScope()
        var selectedOption by remember { mutableStateOf(selectedModel) }
        var isCustom by remember {
            mutableStateOf(openRouterModels.none { it.first == selectedModel })
        }
        var customInput by remember {
            mutableStateOf(if (isCustom) selectedModel else "")
        }
        var isSavingAndAnalyzing by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                if (!isSavingAndAnalyzing) showModelDialog = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.openrouter_model_dialog_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.openrouter_model_dialog_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    openRouterModels.forEach { (modelId, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isSavingAndAnalyzing) {
                                    isCustom = false
                                    selectedOption = modelId
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !isCustom && selectedOption == modelId,
                                onClick = {
                                    isCustom = false
                                    selectedOption = modelId
                                },
                                enabled = !isSavingAndAnalyzing
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    // Opcion personalizada
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = !isSavingAndAnalyzing) { isCustom = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isCustom,
                            onClick = { isCustom = true },
                            enabled = !isSavingAndAnalyzing
                        )
                        Text(
                            text = stringResource(Res.string.openrouter_custom_model_title),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    if (isCustom) {
                        OutlinedTextField(
                            value = customInput,
                            onValueChange = { customInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(Res.string.openrouter_custom_model_hint)) },
                            singleLine = true,
                            enabled = !isSavingAndAnalyzing,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalModel = if (isCustom) customInput.trim() else selectedOption
                        if (finalModel.isNotBlank()) {
                            onModelChange(finalModel)
                            if (isCustom && onAnalyzeModel != null) {
                                coroutineScope.launch {
                                    isSavingAndAnalyzing = true
                                    try {
                                        val result = onAnalyzeModel(finalModel)
                                        showModelDialog = false
                                        modelInfoResult = result
                                    } finally {
                                        isSavingAndAnalyzing = false
                                    }
                                }
                            } else {
                                showModelDialog = false
                            }
                        } else {
                            showModelDialog = false
                        }
                    },
                    enabled = !isSavingAndAnalyzing,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSavingAndAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(Res.string.openrouter_analyzing_model))
                    } else {
                        Text(stringResource(Res.string.save))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showModelDialog = false },
                    enabled = !isSavingAndAnalyzing
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        val providerItems = remember { listOf("OpenRouter", "Google AI Studio") }
        Column(modifier = modifier.fillMaxWidth()) {
            SmallTitle(text = stringResource(Res.string.settings_copilot_section_title))
            MiuixCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                SwitchPreference(
                    title = stringResource(Res.string.copilot_enable_title),
                    summary = stringResource(Res.string.copilot_enable_desc),
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )

                GlassDropdownPreference(
                    title = stringResource(Res.string.copilot_provider_title),
                    summary = if (provider == CopilotProvider.OPENROUTER) "OpenRouter" else "Google AI Studio",
                    items = providerItems,
                    selectedIndex = if (provider == CopilotProvider.OPENROUTER) 0 else 1,
                    onSelectedIndexChange = { index ->
                        val selected = if (index == 0) CopilotProvider.OPENROUTER else CopilotProvider.GOOGLE_AI_STUDIO
                        onProviderChange(selected)
                    },
                    backdrop = backdrop
                )

                // Opciones para OpenRouter
                if (provider == CopilotProvider.OPENROUTER) {
                    ArrowPreference(
                        title = stringResource(Res.string.openrouter_api_key_title),
                        summary = openRouterKeyMasked,
                        onClick = { showApiKeyDialog = true }
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.openrouter_model_title),
                        summary = currentOpenRouterModelLabel,
                        onClick = { showModelDialog = true }
                    )

                    if (!apiKey.isNullOrBlank()) {
                        val balanceStr = when {
                            balance == null && isLoadingBalance -> "..."
                            balance == null -> "—"
                            balance.isFreeTier == true -> "Free Tier"
                            balance.remainingCredits != null -> "$${balance.remainingCredits}"
                            else -> "Active"
                        }
                        ArrowPreference(
                            title = "${stringResource(Res.string.openrouter_balance_title)}: $balanceStr",
                            summary = stringResource(Res.string.tokens_used_format, totalTokens),
                            onClick = onRefreshBalance,
                            endActions = {
                                if (isLoadingBalance) {
                                    top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator(
                                        color = MiuixTheme.colorScheme.primary,
                                        size = 16.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = stringResource(Res.string.sync),
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )
                    }
                } else {
                    // Opciones para Google AI Studio
                    ArrowPreference(
                        title = stringResource(Res.string.google_ai_studio_api_key_title),
                        summary = googleAiStudioKeyMasked,
                        onClick = { showGoogleAiStudioApiKeyDialog = true }
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.google_ai_studio_model_title),
                        summary = currentGoogleAiStudioModelLabel,
                        onClick = { showGoogleAiStudioModelDialog = true }
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.google_ai_studio_balance_title),
                        summary = stringResource(Res.string.tokens_used_format, totalTokens),
                        onClick = onRefreshBalance
                    )
                }
            }
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = stringResource(Res.string.settings_copilot_section_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Switch maestro
                MaterialNotificationToggleRow(
                    icon = Icons.Default.SmartToy,
                    title = stringResource(Res.string.copilot_enable_title),
                    subtitle = stringResource(Res.string.copilot_enable_desc),
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                // Selector de Proveedor Row (Material 3)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showProviderDialog = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.copilot_provider_title),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (provider == CopilotProvider.OPENROUTER) "OpenRouter" else "Google AI Studio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.change),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                if (provider == CopilotProvider.OPENROUTER) {
                    // API Key Row OpenRouter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showApiKeyDialog = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(Res.string.openrouter_api_key_title),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = openRouterKeyMasked,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (apiKey.isNullOrBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.change),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                    // Modelo LLM Row OpenRouter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showModelDialog = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(Res.string.openrouter_model_title),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentOpenRouterModelLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.change),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Balance de créditos OpenRouter
                    if (!apiKey.isNullOrBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(Res.string.openrouter_balance_title),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    IconButton(
                                        onClick = onRefreshBalance,
                                        modifier = Modifier.size(28.dp),
                                        enabled = !isLoadingBalance
                                    ) {
                                        if (isLoadingBalance) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = stringResource(Res.string.sync),
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = stringResource(Res.string.openrouter_balance_title),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            val balanceStr = when {
                                                balance == null && isLoadingBalance -> "..."
                                                balance == null -> "—"
                                                balance.isFreeTier == true -> "Free Tier"
                                                balance.remainingCredits != null -> "$${balance.remainingCredits}"
                                                else -> "Active"
                                            }
                                            Text(
                                                text = balanceStr,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = if (balance?.isFreeTier == true) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = stringResource(Res.string.tokens_used_format, totalTokens),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "$totalTokens",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // API Key Row Google AI Studio
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showGoogleAiStudioApiKeyDialog = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(Res.string.google_ai_studio_api_key_title),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = googleAiStudioKeyMasked,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (googleAiStudioApiKey.isNullOrBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.change),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                    // Modelo LLM Row Google AI Studio
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showGoogleAiStudioModelDialog = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(Res.string.google_ai_studio_model_title),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentGoogleAiStudioModelLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.change),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                    // Tokens Locales Usados
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.google_ai_studio_balance_title),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = stringResource(Res.string.tokens_used_format, totalTokens),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$totalTokens",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
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

@Composable
private fun GlassProviderPickerDialog(
    show: Boolean,
    backdrop: Backdrop?,
    currentProvider: CopilotProvider,
    options: List<Pair<CopilotProvider, String>>,
    onProviderSelected: (CopilotProvider) -> Unit,
    onDismissRequest: () -> Unit,
) {
    GlassDialog(
        show = show,
        backdrop = backdrop,
        onDismissRequest = onDismissRequest,
        title = stringResource(Res.string.copilot_provider_dialog_title),
        summary = stringResource(Res.string.copilot_provider_dialog_desc),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { (option, label) ->
                val selected = option == currentProvider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) {
                                MiuixTheme.colorScheme.primaryContainer
                            } else {
                                MiuixTheme.colorScheme.surfaceContainerHigh
                            }
                        )
                        .clickable { onProviderSelected(option) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MiuixRadioButton(
                        selected = selected,
                        onClick = { onProviderSelected(option) },
                    )
                    MiuixText(
                        text = label,
                        style = MiuixTheme.textStyles.main,
                        color = if (selected) {
                            MiuixTheme.colorScheme.onPrimaryContainer
                        } else {
                            MiuixTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }
        GlassDialogActions {
            GlassDialogButton(
                onClick = onDismissRequest,
                modifier = Modifier.weight(1f),
            ) {
                MiuixText(
                    text = stringResource(Res.string.close),
                    style = MiuixTheme.textStyles.button,
                )
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
private fun GlassApiKeyDialog(
    show: Boolean,
    backdrop: Backdrop?,
    title: String,
    summary: String,
    fieldLabel: String,
    currentKey: String?,
    showDelete: Boolean,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    var inputKey by remember(show) { mutableStateOf(currentKey ?: "") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    GlassDialog(
        show = show,
        backdrop = backdrop,
        onDismissRequest = onDismissRequest,
        title = title,
        summary = summary,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GlassTextField(
                value = inputKey,
                onValueChange = { inputKey = it },
                modifier = Modifier.fillMaxWidth(),
                label = fieldLabel,
                useLabelAsPlaceholder = true,
                singleLine = true,
                visualTransformation = if (isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    MiuixIconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        MiuixIcon(
                            imageVector = if (isPasswordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = null,
                        )
                    }
                },
            )
        }
        GlassDialogActions {
            if (showDelete) {
                GlassDialogButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    role = GlassDialogButtonRole.Destructive,
                ) {
                    MiuixText(
                        text = stringResource(Res.string.delete),
                        style = MiuixTheme.textStyles.button,
                    )
                }
            }
            GlassDialogButton(
                onClick = onDismissRequest,
                modifier = Modifier.weight(1f),
            ) {
                MiuixText(
                    text = stringResource(Res.string.cancel),
                    style = MiuixTheme.textStyles.button,
                )
            }
            GlassDialogButton(
                onClick = { onSave(inputKey) },
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
}

@Suppress("DEPRECATION")
@Composable
private fun GlassModelPickerDialog(
    show: Boolean,
    backdrop: Backdrop?,
    title: String,
    summary: String,
    customFieldLabel: String,
    models: List<Pair<String, String>>,
    currentModel: String,
    onSave: suspend (modelId: String, isCustom: Boolean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var selectedOption by remember(show) { mutableStateOf(currentModel) }
    var isCustom by remember(show) { mutableStateOf(models.none { it.first == currentModel }) }
    var customInput by remember(show) { mutableStateOf(if (isCustom) currentModel else "") }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val dismiss = {
        if (!isSaving) onDismissRequest()
    }

    GlassDialog(
        show = show,
        backdrop = backdrop,
        onDismissRequest = dismiss,
        title = title,
        summary = summary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            models.forEach { (modelId, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !isSaving) {
                            isCustom = false
                            selectedOption = modelId
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MiuixRadioButton(
                        selected = !isCustom && selectedOption == modelId,
                        onClick = {
                            isCustom = false
                            selectedOption = modelId
                        },
                        enabled = !isSaving,
                    )
                    MiuixText(
                        text = label,
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !isSaving) { isCustom = true }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MiuixRadioButton(
                    selected = isCustom,
                    onClick = { isCustom = true },
                    enabled = !isSaving,
                )
                MiuixText(
                    text = stringResource(Res.string.openrouter_custom_model_title),
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            if (isCustom) {
                GlassTextField(
                    value = customInput,
                    onValueChange = { customInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = customFieldLabel,
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !isSaving,
                )
            }
        }
        GlassDialogActions {
            GlassDialogButton(
                onClick = dismiss,
                modifier = Modifier.weight(1f),
                enabled = !isSaving,
            ) {
                MiuixText(
                    text = stringResource(Res.string.cancel),
                    style = MiuixTheme.textStyles.button,
                )
            }
            GlassDialogButton(
                onClick = {
                    val finalModel = if (isCustom) customInput.trim() else selectedOption
                    scope.launch {
                        isSaving = true
                        try {
                            onSave(finalModel, isCustom)
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isSaving,
                role = GlassDialogButtonRole.Primary,
            ) {
                if (isSaving) {
                    MiuixCircularProgressIndicator(modifier = Modifier.size(16.dp), size = 16.dp)
                    Spacer(Modifier.size(8.dp))
                }
                MiuixText(
                    text = if (isSaving) {
                        stringResource(Res.string.openrouter_analyzing_model)
                    } else {
                        stringResource(Res.string.save)
                    },
                    style = MiuixTheme.textStyles.button,
                )
            }
        }
    }
}

@Composable
private fun GlassModelInfoDialog(
    show: Boolean,
    info: me.joxquin.notivas.data.model.CustomModelAnalysisResult?,
    backdrop: Backdrop?,
    onDismissRequest: () -> Unit,
) {
    GlassDialog(
        show = show,
        backdrop = backdrop,
        onDismissRequest = onDismissRequest,
        title = stringResource(Res.string.openrouter_model_info_title),
        summary = info?.name,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (info != null) {
                val containerColor = when (info.status) {
                    me.joxquin.notivas.data.model.ModelCompatibilityStatus.OPTIMAL ->
                        MiuixTheme.colorScheme.primaryContainer
                    me.joxquin.notivas.data.model.ModelCompatibilityStatus.COMPATIBLE ->
                        MiuixTheme.colorScheme.surfaceContainerHigh
                    me.joxquin.notivas.data.model.ModelCompatibilityStatus.INCOMPATIBLE,
                    me.joxquin.notivas.data.model.ModelCompatibilityStatus.NOT_FOUND ->
                        MiuixTheme.colorScheme.errorContainer
                    me.joxquin.notivas.data.model.ModelCompatibilityStatus.ERROR ->
                        MiuixTheme.colorScheme.errorContainer
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(containerColor)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    MiuixText(
                        text = info.name,
                        style = MiuixTheme.textStyles.subtitle,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                    if (info.contextLength != null && info.contextLength > 0) {
                        MiuixText(
                            text = stringResource(Res.string.openrouter_model_context, info.contextLength),
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    MiuixText(
                        text = info.message,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        GlassDialogActions {
            GlassDialogButton(
                onClick = onDismissRequest,
                modifier = Modifier.weight(1f),
                role = GlassDialogButtonRole.Primary,
            ) {
                MiuixText(
                    text = stringResource(Res.string.close),
                    style = MiuixTheme.textStyles.button,
                )
            }
        }
    }
}

