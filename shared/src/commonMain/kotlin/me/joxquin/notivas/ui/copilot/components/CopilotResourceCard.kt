package me.joxquin.notivas.ui.copilot.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
 * Tarjeta interactiva integrada en las respuestas de Copilot IA.
 * Respeta 100% la paleta dinámica del sistema (Monet / Material You en Android y MIUIX en HyperOS),
 * adaptándose perfectamente a temas oscuros y claros sin colores fijos ni discordantes.
 */
@Composable
fun CopilotResourceCard(
    card: CopilotContentBlock.ResourceCardBlock,
    themeStyle: AppThemeStyle,
    onOpenAssignmentDraft: ((Long, String) -> Unit)? = null,
    onOpenForoDetail: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // ─── RESOLUCIÓN DINÁMICA DE COLORES DE LA APP (MONET / MIUIX) ───
    val primaryColor: Color = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary
    }

    val onPrimaryColor: Color = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    val containerColor: Color = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    val surfaceHighColor: Color = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }

    val onSurfaceColor: Color = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val onSurfaceSecondaryColor: Color = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.onSurfaceSecondary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val iconBoxBg: Color = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val iconTint: Color = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val borderColor: Color = if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.outline.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    }

    val (typeIcon: ImageVector, actionText: String) = when (card.type) {
        ResourceCardType.ASSIGNMENT -> Pair(Icons.Default.AssignmentTurnedIn, "Ver consigna")
        ResourceCardType.FORUM -> Pair(Icons.Default.Forum, "Abrir debate")
        ResourceCardType.MATERIAL -> Pair(Icons.Default.FolderOpen, "Ver material")
        ResourceCardType.LINK -> Pair(Icons.Default.OpenInBrowser, "Abrir enlace")
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
                    color = containerColor,
                    cornerRadius = 16.dp
                )
                .squircleBorder(
                    width = 1.dp,
                    color = borderColor,
                    cornerRadius = 16.dp
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPrimaryClick
                )
                .padding(12.dp)
        ) {
            ResourceCardBody(
                card = card,
                primaryColor = primaryColor,
                onPrimaryColor = onPrimaryColor,
                surfaceHighColor = surfaceHighColor,
                onSurfaceColor = onSurfaceColor,
                onSurfaceSecondaryColor = onSurfaceSecondaryColor,
                iconBoxBg = iconBoxBg,
                iconTint = iconTint,
                typeIcon = typeIcon,
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
                containerColor = containerColor
            ),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                ResourceCardBody(
                    card = card,
                    primaryColor = primaryColor,
                    onPrimaryColor = onPrimaryColor,
                    surfaceHighColor = surfaceHighColor,
                    onSurfaceColor = onSurfaceColor,
                    onSurfaceSecondaryColor = onSurfaceSecondaryColor,
                    iconBoxBg = iconBoxBg,
                    iconTint = iconTint,
                    typeIcon = typeIcon,
                    actionText = actionText,
                    themeStyle = themeStyle,
                    onPrimaryClick = onPrimaryClick
                )
            }
        }
    }
}

@Composable
private fun ResourceCardBody(
    card: CopilotContentBlock.ResourceCardBlock,
    primaryColor: Color,
    onPrimaryColor: Color,
    surfaceHighColor: Color,
    onSurfaceColor: Color,
    onSurfaceSecondaryColor: Color,
    iconBoxBg: Color,
    iconTint: Color,
    typeIcon: ImageVector,
    actionText: String,
    themeStyle: AppThemeStyle,
    onPrimaryClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Fila Superior (Opcional): Badge del curso + Puntos + Botón Canvas/Web
        val hasCourse = !card.courseName.isNullOrBlank()
        val hasPoints = card.pointsPossible != null && card.pointsPossible > 0
        val hasUrl = !card.url.isNullOrBlank()

        if (hasCourse || hasPoints || hasUrl) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (hasCourse) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = surfaceHighColor,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = card.courseName.orEmpty(),
                                style = if (themeStyle == AppThemeStyle.MIUIX) {
                                    MiuixTheme.textStyles.footnote1.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                } else {
                                    MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                },
                                color = primaryColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (hasPoints) {
                        Text(
                            text = "• ${card.pointsPossible?.toInt()} pts",
                            style = if (themeStyle == AppThemeStyle.MIUIX) {
                                MiuixTheme.textStyles.footnote2.copy(fontSize = 10.5.sp)
                            } else {
                                MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp)
                            },
                            color = onSurfaceSecondaryColor,
                            maxLines = 1
                        )
                    }
                }

                if (hasUrl) {
                    IconButton(
                        onClick = { CanvasDeepLinkHandler.openCanvasUrl(card.url!!) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Abrir en Canvas o Navegador Web",
                            tint = onSurfaceSecondaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Fila Principal: Icono + Título/Vencimiento + Botón de Acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del recurso en caja redondeada
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBoxBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Textos del centro: Título y Fecha
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = card.title,
                    style = if (themeStyle == AppThemeStyle.MIUIX) {
                        MiuixTheme.textStyles.body1.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    } else {
                        MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    },
                    color = onSurfaceColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!card.dueDate.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = onSurfaceSecondaryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = card.dueDate,
                            style = if (themeStyle == AppThemeStyle.MIUIX) {
                                MiuixTheme.textStyles.footnote2.copy(fontSize = 11.5.sp)
                            } else {
                                MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp)
                            },
                            color = onSurfaceSecondaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón de Acción primario en color de la app (Monet / MIUIX Primary)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = primaryColor,
                contentColor = onPrimaryColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = actionText,
                        style = if (themeStyle == AppThemeStyle.MIUIX) {
                            MiuixTheme.textStyles.footnote1.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
