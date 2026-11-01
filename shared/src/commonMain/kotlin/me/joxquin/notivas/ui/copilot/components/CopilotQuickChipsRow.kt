package me.joxquin.notivas.ui.copilot.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
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
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class QuickChipItem(
    val label: String,
    val prompt: String,
    val icon: ImageVector,
    val iconTint: Color
)

@Composable
fun CopilotQuickChipsRow(
    onChipClick: (String) -> Unit,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    val chips = listOf(
        QuickChipItem(
            label = "Buscar en la web",
            prompt = "Busca en la web fuentes académicas y artículos actualizados sobre este tema para mi informe.",
            icon = Icons.Default.Search,
            iconTint = Color(0xFF10B981)
        ),
        QuickChipItem(
            label = "Buscar tesis / papers",
            prompt = "Busca tesis universitarias y papers científicos relevantes en PDF sobre este tema con enlaces de referencia.",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            iconTint = Color(0xFF3B82F6)
        ),
        QuickChipItem(
            label = "Estructurar informe",
            prompt = "Sintetiza y estructura estos puntos clave en formato de informe académico universitario (introducción, desarrollo, metodología y conclusiones).",
            icon = Icons.Default.AutoAwesome,
            iconTint = Color(0xFFA855F7)
        ),
        QuickChipItem(
            label = "Explicar paso a paso",
            prompt = "Explícame detalladamente paso a paso los conceptos y procedimientos teóricos y prácticos de este tema.",
            icon = Icons.Default.Psychology,
            iconTint = Color(0xFFF59E0B)
        )
    )

    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        chips.forEach { chip ->
            if (themeStyle == AppThemeStyle.MIUIX) {
                Box(
                    modifier = Modifier
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                            cornerRadius = 9999.dp
                        )
                        .squircleBorder(
                            width = 0.8.dp,
                            color = MiuixTheme.colorScheme.dividerLine,
                            cornerRadius = 9999.dp
                        )
                        .clip(CircleShape)
                        .clickable { onChipClick(chip.prompt) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = chip.icon,
                            contentDescription = null,
                            tint = chip.iconTint,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = chip.label,
                            style = MiuixTheme.textStyles.footnote2.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onChipClick(chip.prompt) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = chip.icon,
                            contentDescription = null,
                            tint = chip.iconTint,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = chip.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
