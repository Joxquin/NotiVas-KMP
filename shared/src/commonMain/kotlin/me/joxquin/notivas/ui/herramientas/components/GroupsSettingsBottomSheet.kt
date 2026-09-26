package me.joxquin.notivas.ui.herramientas.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsSettingsBottomSheet(
    onDismiss: () -> Unit,
    onOpenCatalogImport: () -> Unit,
    onImportJson: (json: String) -> Unit,
    onExportJson: () -> Unit,
    onResetGroups: () -> Unit,
    exportedJson: String? = null,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showJsonInputDialog by remember { mutableStateOf(false) }
    var inputJsonText by remember { mutableStateOf("") }
    var jsonError by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    var copiedToClipboard by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header del Sheet
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = null,
                            tint = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Ajustes de Ponderación",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Importar, exportar y gestionar grupos globales",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(
                color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.dividerLine else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // Sección 1: IMPORTAR GRUPOS
            Text(
                text = "IMPORTAR GRUPOS DE EVALUACIÓN",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
            )

            SettingActionTile(
                icon = Icons.Default.LibraryBooks,
                title = "Desde Repositorio / Catálogo",
                subtitle = "Plantillas oficiales preconfiguradas (ej. Tecsup)",
                themeStyle = themeStyle,
                onClick = {
                    onDismiss()
                    onOpenCatalogImport()
                }
            )

            SettingActionTile(
                icon = Icons.Default.Upload,
                title = "Desde Archivo / Código JSON",
                subtitle = "Importa una configuración global de pesos y reglas",
                themeStyle = themeStyle,
                onClick = {
                    showJsonInputDialog = true
                }
            )

            // Sección 2: EXPORTAR & COPIAS
            Spacer(Modifier.height(4.dp))
            Text(
                text = "EXPORTACIÓN & RESPALDO",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
            )

            SettingActionTile(
                icon = Icons.Default.Download,
                title = "Exportar Grupos de Todos los Cursos",
                subtitle = "Genera un archivo JSON con los grupos actuales",
                themeStyle = themeStyle,
                onClick = {
                    onExportJson()
                }
            )

            // Vista previa del JSON Exportado si existe
            if (exportedJson != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (themeStyle == AppThemeStyle.MIUIX) MiuixTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "JSON Generado",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(exportedJson))
                                    copiedToClipboard = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (copiedToClipboard) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = "Copiar",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = exportedJson.take(180) + if (exportedJson.length > 180) "\n..." else "",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 4
                        )
                    }
                }
            }

            // Sección 3: RESTABLECER
            Spacer(Modifier.height(4.dp))
            SettingActionTile(
                icon = Icons.Default.Restore,
                title = "Restablecer Todos los Grupos",
                subtitle = "Elimina los grupos de simulación de todos los cursos",
                isDestructive = true,
                themeStyle = themeStyle,
                onClick = {
                    onResetGroups()
                    onDismiss()
                }
            )
        }
    }

    // Diálogo para pegar JSON de importación
    if (showJsonInputDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showJsonInputDialog = false },
            title = {
                Text(
                    text = "Importar Configuración JSON",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Pega el código JSON exportado de grupos para aplicarlo globalmente a tus cursos:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = inputJsonText,
                        onValueChange = {
                            inputJsonText = it
                            jsonError = null
                        },
                        placeholder = { Text("{ \"version\": 1, \"courses\": [...] }") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp),
                        shape = RoundedCornerShape(12.dp),
                        isError = jsonError != null
                    )
                    if (jsonError != null) {
                        Text(
                            text = jsonError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputJsonText.isNotBlank()) {
                            onImportJson(inputJsonText.trim())
                            showJsonInputDialog = false
                            onDismiss()
                        }
                    },
                    enabled = inputJsonText.isNotBlank()
                ) {
                    Text("Importar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJsonInputDialog = false }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SettingActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    themeStyle: AppThemeStyle,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    if (themeStyle == AppThemeStyle.MIUIX) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .squircleSurface(
                    color = MiuixTheme.colorScheme.surfaceVariant,
                    cornerRadius = 16.dp
                )
                .squircleBorder(
                    width = 0.5.dp,
                    color = MiuixTheme.colorScheme.dividerLine,
                    cornerRadius = 16.dp
                )
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDestructive) Color(0xFFFFEBEE) else MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) Color(0xFFD32F2F) else MiuixTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isDestructive) Color(0xFFD32F2F) else MiuixTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }
    } else {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDestructive) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
