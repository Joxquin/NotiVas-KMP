package me.joxquin.notivas.ui.copilot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

private data class SuggestionItem(
    val icon: ImageVector,
    val text: String,
    val iconTint: Color
)

@Composable
fun CopilotQuickSuggestionsSection(
    onSuggestionClick: (String) -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf(
        SuggestionItem(
            icon = Icons.Default.CalendarMonth,
            text = "¿Qué tareas tengo pendientes de entrega esta semana?",
            iconTint = Color(0xFF3482FF)
        ),
        SuggestionItem(
            icon = Icons.AutoMirrored.Filled.Assignment,
            text = "¿Cuáles son las consignas y rúbricas de mi próxima tarea?",
            iconTint = Color(0xFFA855F7)
        ),
        SuggestionItem(
            icon = Icons.Default.Calculate,
            text = "¿Cuánto necesito sacar en el examen final para aprobar mis cursos?",
            iconTint = Color(0xFFFFB74D)
        )
    )

    if (themeStyle == AppThemeStyle.MIUIX) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallTitle(text = "SUGERENCIAS RÁPIDAS")

            suggestions.forEach { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.surfaceVariant,
                            cornerRadius = 18.dp
                        )
                        .squircleBorder(
                            width = 1.dp,
                            color = MiuixTheme.colorScheme.dividerLine,
                            cornerRadius = 18.dp
                        )
                        .clickable { onSuggestionClick(item.text) }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .squircleSurface(
                                        color = item.iconTint.copy(alpha = 0.12f),
                                        cornerRadius = 12.dp
                                    )
                                    .squircleBorder(
                                        width = 1.dp,
                                        color = item.iconTint.copy(alpha = 0.25f),
                                        cornerRadius = 12.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = item.iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = item.text,
                                style = MiuixTheme.textStyles.footnote1.copy(fontWeight = FontWeight.Medium),
                                color = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceSecondary.copy(alpha = 0.6f),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(16.dp)
                        )
                    }
                }
            }
        }
    } else {
        // Modo Material Design 3
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Sugerencias",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )

            suggestions.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(item.text) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
