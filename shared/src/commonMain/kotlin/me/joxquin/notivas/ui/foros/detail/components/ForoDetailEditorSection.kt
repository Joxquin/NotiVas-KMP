package me.joxquin.notivas.ui.foros.detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.CanvasDiscussionTopic
import androidx.compose.material.icons.filled.Lock
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ForoDetailEditorSection(
    topic: CanvasDiscussionTopic,
    draftText: String,
    onDraftChange: (String) -> Unit,
    onSaveDraft: () -> Unit,
    isDraftSaved: Boolean,
    isExpired: Boolean = false,
    onOpenCopilotAssistant: () -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val wordCount = remember(draftText) {
        val trimmed = draftText.trim()
        if (trimmed.isEmpty()) 0 else trimmed.split(Regex("\\s+")).size
    }
    val charCount = draftText.length
    val isWordCountMet = wordCount >= 150

    val primaryColor = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    val onSurface = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainer
    val editorBg = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Título de la sección
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tu Respuesta Académica",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = onSurface
            )

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isDraftSaved) Color(0xFF4CAF50).copy(alpha = 0.2f) else editorBg
            ) {
                Text(
                    text = if (isDraftSaved) "¡Borrador guardado!" else "Borrador local",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = if (isDraftSaved) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isDraftSaved) Color(0xFF4CAF50) else onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // 1. Banner Inteligente Copilot IA
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = primaryColor.copy(alpha = 0.18f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¿Necesitas estructurar tu argumento?",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = onSurface
                            )
                            Text(
                                text = "Copilot analiza la consigna de Canvas LMS y la rúbrica para asistirte.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                                color = onSurfaceVariant
                            )
                        }
                    }

                    // Botón Protagónico Preguntar a Copilot
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = primaryColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .clickable {
                                onOpenCopilotAssistant()
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Estructurar respuesta con Copilot",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Editor de Texto con Formato
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
                // Barra de herramientas de formato rápido
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FormatIconButton(icon = Icons.Default.FormatBold, onClick = { onDraftChange("$draftText **negrita**") }, themeStyle = themeStyle)
                        FormatIconButton(icon = Icons.Default.FormatItalic, onClick = { onDraftChange("$draftText *cursiva*") }, themeStyle = themeStyle)
                        FormatIconButton(icon = Icons.Default.Code, onClick = { onDraftChange("$draftText ```codigo```") }, themeStyle = themeStyle)
                        FormatIconButton(icon = Icons.Default.Functions, onClick = { onDraftChange("$draftText O(n log n)") }, themeStyle = themeStyle)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spellcheck,
                            contentDescription = null,
                            tint = onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "ES-LA",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = onSurfaceVariant
                        )
                    }
                }

                // Textarea
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = editorBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = draftText,
                        onValueChange = onDraftChange,
                        textStyle = TextStyle(
                            color = onSurface,
                            fontSize = 14.sp,
                            lineHeight = 21.sp
                        ),
                        cursorBrush = SolidColor(primaryColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(12.dp),
                        decorationBox = { innerTextField ->
                            if (draftText.isEmpty()) {
                                Text(
                                    text = "Escribe tu intervención académica aquí o genera una estructura con Copilot...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
                                    color = onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                // Contador de Palabras y Validación de Rúbrica
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isWordCountMet) Color(0xFF4CAF50) else onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$wordCount palabras ($charCount caracteres)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isWordCountMet) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isWordCountMet) Color(0xFF4CAF50) else onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Mínimo sugerido: 150",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = onSurfaceVariant
                    )
                }
            }
        }

        // 3. Botones de Acción al Pie (Guardar Borrador / Publicar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = editorBg,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onSaveDraft)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDraftSaved) Icons.Default.Check else Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = if (isDraftSaved) "¡Guardado!" else "Guardar Borrador",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = onSurface
                    )
                }
            }

            val publishBtnBg = if (isExpired) {
                if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                primaryColor
            }
            val publishBtnTextColor = if (isExpired) {
                onSurfaceVariant
            } else {
                if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = publishBtnBg,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = !isExpired) {
                        onSaveDraft()
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpired) "Foro Cerrado" else "Publicar en Canvas",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = publishBtnTextColor
                    )
                    Spacer(Modifier.size(6.dp))
                    Icon(
                        imageVector = if (isExpired) Icons.Default.Lock else Icons.Default.Send,
                        contentDescription = null,
                        tint = publishBtnTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    themeStyle: AppThemeStyle
) {
    val bg = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHigh
    val onSurface = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = onSurface,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
