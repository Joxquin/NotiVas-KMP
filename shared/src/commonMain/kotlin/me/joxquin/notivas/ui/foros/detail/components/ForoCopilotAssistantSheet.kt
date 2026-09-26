package me.joxquin.notivas.ui.foros.detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.CanvasDiscussionTopic
import me.joxquin.notivas.ui.foros.CopilotForoStrategy
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForoCopilotAssistantSheet(
    topic: CanvasDiscussionTopic,
    strategy: CopilotForoStrategy?,
    isGenerating: Boolean,
    errorMessage: String?,
    activeModelName: String,
    onDismiss: () -> Unit,
    onGenerate: (toneModifier: String?) -> Unit,
    onInsertDraft: (String) -> Unit,
    themeStyle: AppThemeStyle
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }
    var selectedTone by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(topic.id) {
        if (strategy == null && !isGenerating && errorMessage == null) {
            onGenerate(null)
        }
    }

    val primaryColor = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val onSurface = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainer
    val chipBg = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
    val successColor = Color(0xFF4CAF50)

    val cleanModelDisplayName = remember(activeModelName) {
        when {
            activeModelName.contains("gemini-2.5-flash", ignoreCase = true) -> "Gemini 2.5 Flash"
            activeModelName.contains("gemini-2.5-pro", ignoreCase = true) -> "Gemini 2.5 Pro"
            activeModelName.contains("claude-3-5-sonnet", ignoreCase = true) -> "Claude 3.5 Sonnet"
            activeModelName.contains("deepseek-r1", ignoreCase = true) -> "DeepSeek-R1"
            activeModelName.contains("gpt-4o", ignoreCase = true) -> "GPT-4o"
            else -> activeModelName.substringAfterLast("/")
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surface else MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(top = 12.dp)
        ) {
            // Header: Botón volver + Modelo Activo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = chipBg,
                    modifier = Modifier.clickable {
                        coroutineScope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Volver al Foro",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = onSurface
                        )
                    }
                }

                // Chip de Modelo de IA Activo
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = primaryColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                        )
                        Text(
                            text = cleanModelDisplayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = primaryColor
                        )
                    }
                }
            }

            // Título Principal
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                Text(
                    text = "COPILOT ACADÉMICO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    ),
                    color = primaryColor
                )
                Text(
                    text = "Estrategia para ${topic.title}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = onSurface,
                    maxLines = 1
                )
            }

            // Contenido dinámico (Cargando / Error / Contenido)
            if (isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(
                            color = primaryColor,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Analizando consigna con $cleanModelDisplayName...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Extrayendo objetivos de aprendizaje y estructura de respuesta",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No se pudo generar la estrategia",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = onSurface
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = primaryColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onGenerate(selectedTone) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Reintentar",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            } else if (strategy != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Tarjeta de Diagnóstico y Cumplimiento de Rúbrica
                    item(key = "copilot_diagnostic") {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = cardBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(9999.dp),
                                            color = successColor.copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = null,
                                                    tint = successColor,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = "Objetivo Detectado",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = successColor
                                                )
                                            }
                                        }

                                        Text(
                                            text = strategy.learningObjective,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                lineHeight = 18.sp
                                            ),
                                            color = onSurface
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = primaryColor.copy(alpha = 0.15f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.FactCheck,
                                                contentDescription = null,
                                                tint = primaryColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                // Mini medidores
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MetricPill(
                                        title = "Rigor Técnico",
                                        value = strategy.technicalRigor,
                                        themeStyle = themeStyle,
                                        modifier = Modifier.weight(1f)
                                    )
                                    MetricPill(
                                        title = "Contraejemplo",
                                        value = strategy.counterExample,
                                        themeStyle = themeStyle,
                                        modifier = Modifier.weight(1f)
                                    )
                                    MetricPill(
                                        title = "Interacción",
                                        value = strategy.activeInteraction,
                                        themeStyle = themeStyle,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Estructura Argumental Recomendada (3 Fases)
                    item(key = "copilot_structure") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ESTRUCTURA ARGUMENTAL RECOMENDADA",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = onSurfaceVariant
                                )
                                Text(
                                    text = "3 Fases",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = primaryColor
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = cardBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    StructurePhaseItem(
                                        phaseNumber = "1",
                                        title = strategy.phase1Title,
                                        description = strategy.phase1Desc,
                                        themeStyle = themeStyle
                                    )
                                    StructurePhaseItem(
                                        phaseNumber = "2",
                                        title = strategy.phase2Title,
                                        description = strategy.phase2Desc,
                                        themeStyle = themeStyle,
                                        isAlternate = true
                                    )
                                    StructurePhaseItem(
                                        phaseNumber = "3",
                                        title = strategy.phase3Title,
                                        description = strategy.phase3Desc,
                                        themeStyle = themeStyle
                                    )
                                }
                            }
                        }
                    }

                    // 3. Afinador de Tono & Perspectiva (Chips interactivos)
                    item(key = "copilot_tone_tuner") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AFINADOR DE TONO & PERSPECTIVA",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ToneChip(
                                    icon = Icons.Default.Psychology,
                                    text = "Más formal y técnico",
                                    isSelected = selectedTone == "Haz el texto más riguroso, formal y de nivel universitario avanzado.",
                                    onClick = {
                                        selectedTone = "Haz el texto más riguroso, formal y de nivel universitario avanzado."
                                        onGenerate(selectedTone)
                                    },
                                    themeStyle = themeStyle
                                )
                                ToneChip(
                                    icon = Icons.Default.Compress,
                                    text = "Más conciso (-30%)",
                                    isSelected = selectedTone == "Resume la respuesta haciéndola 30% más concisa y directa al grano.",
                                    onClick = {
                                        selectedTone = "Resume la respuesta haciéndola 30% más concisa y directa al grano."
                                        onGenerate(selectedTone)
                                    },
                                    themeStyle = themeStyle
                                )
                                ToneChip(
                                    icon = Icons.Default.MenuBook,
                                    text = "Añadir citas y teoría",
                                    isSelected = selectedTone == "Incluye fundamentación teórica profunda y referencias metodológicas.",
                                    onClick = {
                                        selectedTone = "Incluye fundamentación teórica profunda y referencias metodológicas."
                                        onGenerate(selectedTone)
                                    },
                                    themeStyle = themeStyle
                                )
                                ToneChip(
                                    icon = Icons.Default.CrisisAlert,
                                    text = "Enfatizar contraejemplo",
                                    isSelected = selectedTone == "Profundiza en contraejemplos detallados y casos de borde.",
                                    onClick = {
                                        selectedTone = "Profundiza en contraejemplos detallados y casos de borde."
                                        onGenerate(selectedTone)
                                    },
                                    themeStyle = themeStyle
                                )
                            }
                        }
                    }

                    // 4. Tarjeta de Respuesta Generada
                    item(key = "copilot_draft_preview") {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = cardBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(9999.dp),
                                            color = primaryColor.copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = primaryColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "Borrador sugerido",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = primaryColor
                                                )
                                            }
                                        }

                                        Text(
                                            text = "${strategy.wordCount} palabras",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(9999.dp),
                                        color = chipBg
                                    ) {
                                        Text(
                                            text = "Tono Académico",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                            color = primaryColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = chipBg,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = strategy.suggestedDraft,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            lineHeight = 20.sp
                                        ),
                                        color = onSurface,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 5. Botones de Acción (Copiar + Editar + Insertar)
                    item(key = "copilot_actions") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = chipBg,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            isCopied = true
                                            coroutineScope.launch {
                                                delay(2000)
                                                isCopied = false
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            tint = if (isCopied) successColor else onSurface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = if (isCopied) "¡Copiado!" else "Copiar texto",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isCopied) successColor else onSurface
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = chipBg,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            onInsertDraft(strategy.suggestedDraft)
                                            coroutineScope.launch {
                                                sheetState.hide()
                                                onDismiss()
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = onSurface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Editar borrador",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = onSurface
                                        )
                                    }
                                }
                            }

                            // Botón Principal de Inserción
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = primaryColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        onInsertDraft(strategy.suggestedDraft)
                                        coroutineScope.launch {
                                            sheetState.hide()
                                            onDismiss()
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Insertar en mi respuesta",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    title: String,
    value: String,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val chipBg = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
    val onSurfaceVariant = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val successColor = Color(0xFF4CAF50)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = chipBg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = successColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StructurePhaseItem(
    phaseNumber: String,
    title: String,
    description: String,
    themeStyle: AppThemeStyle,
    isAlternate: Boolean = false
) {
    val primaryColor = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val onSurface = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val bg = if (isAlternate) {
        if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = primaryColor.copy(alpha = 0.18f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = phaseNumber,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = primaryColor
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                color = onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ToneChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    themeStyle: AppThemeStyle
) {
    val primaryColor = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val onPrimary = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
    val onSurface = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val chipBg = if (isSelected) primaryColor else if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow

    Surface(
        shape = RoundedCornerShape(9999.dp),
        color = chipBg,
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) onPrimary else primaryColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "✦ $text",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) onPrimary else onSurface
            )
        }
    }
}
