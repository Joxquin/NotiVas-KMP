package me.joxquin.notivas.ui.copilot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun CopilotHeaderBar(
    hasMessages: Boolean,
    tokens: Int,
    balance: Double?,
    onOpenHistory: () -> Unit,
    onStartNewChat: () -> Unit,
    onClearChat: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    modifier: Modifier = Modifier
) {
    val tokenLabel = if (tokens > 0) {
        "$tokens tokens"
    } else if (balance != null) {
        val rounded = (balance * 1000).toLong() / 1000.0
        "$$rounded"
    } else null

    if (themeStyle == AppThemeStyle.MIUIX) {
        // Estilo Stitch Miuix: TopBar con píldoras reactivas al color de tema
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Historial de Sesiones
                Box(
                    modifier = Modifier
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.surfaceVariant,
                            cornerRadius = 9999.dp
                        )
                        .squircleBorder(
                            width = 1.dp,
                            color = MiuixTheme.colorScheme.dividerLine,
                            cornerRadius = 9999.dp
                        )
                        .clickable(onClick = onOpenHistory)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Historial",
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Historial",
                            style = MiuixTheme.textStyles.footnote1.copy(fontWeight = FontWeight.Medium),
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }
                }

                // Nueva consulta
                Box(
                    modifier = Modifier
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.surfaceVariant,
                            cornerRadius = 9999.dp
                        )
                        .squircleBorder(
                            width = 1.dp,
                            color = MiuixTheme.colorScheme.dividerLine,
                            cornerRadius = 9999.dp
                        )
                        .clickable(onClick = onStartNewChat)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddComment,
                            contentDescription = "Nuevo Chat",
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Nueva",
                            style = MiuixTheme.textStyles.footnote1.copy(fontWeight = FontWeight.Medium),
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tokens o Balance de la sesión actual
                if (tokenLabel != null) {
                    Box(
                        modifier = Modifier
                            .squircleSurface(
                                color = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                                cornerRadius = 9999.dp
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tokenLabel,
                            style = MiuixTheme.textStyles.footnote2.copy(fontWeight = FontWeight.Bold),
                            color = MiuixTheme.colorScheme.primary
                        )
                    }
                }

                // Limpiar conversación (si hay mensajes)
                if (hasMessages) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .squircleSurface(
                                color = MiuixTheme.colorScheme.surfaceVariant,
                                cornerRadius = 9999.dp
                            )
                            .squircleBorder(
                                width = 1.dp,
                                color = MiuixTheme.colorScheme.dividerLine,
                                cornerRadius = 9999.dp
                            )
                            .clickable(onClick = onClearChat),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = "Limpiar chat",
                            tint = MiuixTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    } else {
        // Modo Material Design 3
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onOpenHistory,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Historial",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                FilledTonalButton(
                    onClick = onStartNewChat,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AddComment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Nuevo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (tokenLabel != null) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(9999.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tokenLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (hasMessages) {
                    IconButton(onClick = onClearChat) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = "Limpiar chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
