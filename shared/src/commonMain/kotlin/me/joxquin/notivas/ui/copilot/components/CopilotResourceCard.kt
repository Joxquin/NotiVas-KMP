package me.joxquin.notivas.ui.copilot.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.util.CanvasDeepLinkHandler
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class ResourceCardType {
    ASSIGNMENT,
    FORUM,
    MATERIAL,
    LINK
}

/**
 * Tarjeta interactiva enriquecida para tareas, foros, materiales y enlaces
 * intercalados dentro de las respuestas de Copilot IA.
 */
@Composable
fun CopilotResourceCard(
    card: CopilotContentBlock.ResourceCardBlock,
    themeStyle: AppThemeStyle,
    onOpenAssignmentDraft: ((Long, String) -> Unit)? = null,
    onOpenForoDetail: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (primaryColor, typeIcon, typeLabel, actionText) = when (card.type) {
        ResourceCardType.ASSIGNMENT -> ResourceCardVisuals(
            color = Color(0xFF3B82F6), // Azul vibrante
            icon = Icons.Default.Assignment,
            typeLabel = "Tarea / Entrega",
            actionText = "Ver consigna"
        )
        ResourceCardType.FORUM -> ResourceCardVisuals(
            color = Color(0xFF8B5CF6), // Violeta
            icon = Icons.Default.Forum,
            typeLabel = "Foro Académico",
            actionText = "Abrir debate"
        )
        ResourceCardType.MATERIAL -> ResourceCardVisuals(
            color = Color(0xFF10B981), // Esmeralda
            icon = Icons.Default.FolderOpen,
            typeLabel = "Material de Clase",
            actionText = "Ver material"
        )
        ResourceCardType.LINK -> ResourceCardVisuals(
            color = Color(0xFF06B6D4), // Cyan
            icon = Icons.Default.OpenInBrowser,
            typeLabel = "Recurso Web",
            actionText = "Abrir enlace"
        )
    }

    val onPrimaryClick: () -> Unit = {
        when (card.type) {
            ResourceCardType.ASSIGNMENT -> {
                if (card.assignmentId != null) {
                    onOpenAssignmentDraft?.invoke(card.assignmentId, card.courseName ?: "")
                } else if (!card.url.isNullOrBlank()) {
                    CanvasDeepLinkHandler.openCanvasUrl(card.url)
                }
            }
            ResourceCardType.FORUM -> {
                if (card.forumTopicId != null) {
                    onOpenForoDetail?.invoke(card.forumTopicId)
                } else if (!card.url.isNullOrBlank()) {
                    CanvasDeepLinkHandler.openCanvasUrl(card.url)
                }
            }
            ResourceCardType.MATERIAL,
            ResourceCardType.LINK -> {
                if (!card.url.isNullOrBlank()) {
                    CanvasDeepLinkHandler.openCanvasUrl(card.url)
                }
            }
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .squircleSurface(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                    cornerRadius = 16.dp
                )
                .squircleBorder(
                    width = 1.dp,
                    color = primaryColor.copy(alpha = 0.22f),
                    cornerRadius = 16.dp
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPrimaryClick
                )
                .padding(12.dp)
        ) {
            ResourceCardContent(
                card = card,
                primaryColor = primaryColor,
                typeIcon = typeIcon,
                typeLabel = typeLabel,
                actionText = actionText,
                themeStyle = themeStyle,
                onPrimaryClick = onPrimaryClick
            )
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(onClick = onPrimaryClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f))
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                ResourceCardContent(
                    card = card,
                    primaryColor = primaryColor,
                    typeIcon = typeIcon,
                    typeLabel = typeLabel,
                    actionText = actionText,
                    themeStyle = themeStyle,
                    onPrimaryClick = onPrimaryClick
                )
            }
        }
    }
}

private data class ResourceCardVisuals(
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val typeLabel: String,
    val actionText: String
)

@Composable
private fun ResourceCardContent(
    card: CopilotContentBlock.ResourceCardBlock,
    primaryColor: Color,
    typeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    typeLabel: String,
    actionText: String,
    themeStyle: AppThemeStyle,
    onPrimaryClick: () -> Unit
) {
    val onSurfaceColor = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val onSurfaceVariantColor = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.onSurfaceSecondary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Fila Superior: Badge del curso o tipo de recurso + Icono secundario de Canvas / Web
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Pill con el nombre del curso o tipo
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = card.courseName ?: typeLabel,
                        style = if (themeStyle == AppThemeStyle.MIUIX) {
                            MiuixTheme.textStyles.footnote.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        color = primaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (card.pointsPossible != null && card.pointsPossible > 0) {
                    Text(
                        text = "•  ${card.pointsPossible.toInt()} pts",
                        style = if (themeStyle == AppThemeStyle.MIUIX) {
                            MiuixTheme.textStyles.footnote.copy(fontSize = 11.sp)
                        } else {
                            MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                        },
                        color = onSurfaceVariantColor
                    )
                }
            }

            // Botón secundario para abrir en la app de Canvas Student o Web si hay URL
            if (!card.url.isNullOrBlank()) {
                IconButton(
                    onClick = { CanvasDeepLinkHandler.openCanvasUrl(card.url) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = "Abrir en Canvas o Navegador Web",
                        tint = onSurfaceVariantColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fila Principal: Icono de tipo + Título / Fecha + Botón de Acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contenedor Squircle / Redondeado para el icono
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(primaryColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Textos del centro
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = card.title,
                    style = if (themeStyle == AppThemeStyle.MIUIX) {
                        MiuixTheme.textStyles.body1.copy(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        MaterialTheme.typography.titleSmall.copy(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    color = onSurfaceColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!card.dueDate.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = onSurfaceVariantColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = card.dueDate,
                            style = if (themeStyle == AppThemeStyle.MIUIX) {
                                MiuixTheme.textStyles.footnote.copy(fontSize = 12.sp)
                            } else {
                                MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                            },
                            color = onSurfaceVariantColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Chip / Botón de acción hacia la consigna o debate
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(primaryColor.copy(alpha = 0.12f))
                    .clickable(onClick = onPrimaryClick)
                    .padding(horizontal = 9.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = actionText,
                        style = if (themeStyle == AppThemeStyle.MIUIX) {
                            MiuixTheme.textStyles.footnote.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        color = primaryColor
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
