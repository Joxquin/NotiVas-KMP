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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun CopilotWarningBanner(
    isCopilotDisabled: Boolean,
    hasApiKey: Boolean,
    onNavigateToSettings: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    modifier: Modifier = Modifier
) {
    if (hasApiKey && !isCopilotDisabled) return

    val title = if (isCopilotDisabled) "Copilot Desactivado" else "API Key Requerida"
    val subtitle = if (isCopilotDisabled) {
        "Activa NotiVas Copilot en Ajustes"
    } else {
        "Configura tu OpenRouter Key en Ajustes"
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        // Estilo Stitch Miuix: Squircle Card reactiva a errorContainer
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .squircleSurface(
                    color = MiuixTheme.colorScheme.errorContainer,
                    cornerRadius = 20.dp
                )
                .squircleBorder(
                    width = 1.dp,
                    color = MiuixTheme.colorScheme.error.copy(alpha = 0.25f),
                    cornerRadius = 20.dp
                )
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
                                color = MiuixTheme.colorScheme.error.copy(alpha = 0.15f),
                                cornerRadius = 9999.dp
                            )
                            .squircleBorder(
                                width = 1.dp,
                                color = MiuixTheme.colorScheme.error.copy(alpha = 0.3f),
                                cornerRadius = 9999.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MiuixTheme.textStyles.title2.copy(fontWeight = FontWeight.Bold),
                            color = MiuixTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = subtitle,
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.error,
                            cornerRadius = 9999.dp
                        )
                        .clickable(onClick = onNavigateToSettings)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Ajustes",
                        style = MiuixTheme.textStyles.footnote1.copy(fontWeight = FontWeight.SemiBold),
                        color = MiuixTheme.colorScheme.onError
                    )
                }
            }
        }
    } else {
        // Estilo Material 3 Expressive
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Button(
                    onClick = onNavigateToSettings,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Ajustes", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
