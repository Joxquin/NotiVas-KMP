package me.joxquin.notivas.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.ui.copilot.components.CopilotContentBlock
import me.joxquin.notivas.ui.copilot.components.CopilotDividerComposable
import me.joxquin.notivas.ui.copilot.components.CopilotTableComposable
import me.joxquin.notivas.ui.copilot.components.formatMarkdown
import me.joxquin.notivas.ui.copilot.components.parseMessageBlocks
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Renderizador unificado de contenido enriquecido con soporte integral para:
 * - Texto Markdown (negritas, cursivas, listas, código inline y bloques)
 * - Divisores horizontales
 * - Tablas Markdown con scroll horizontal e intercalado de colores según el tema
 * - Soporte para vista colapsada / expandida
 */
@Composable
fun RichContentRenderer(
    text: String,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier,
    maxLinesCollapsed: Int? = null,
    isExpanded: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    textColor: Color = Color.Unspecified
) {
    val blocks = remember(text) { parseMessageBlocks(text) }
    val resolvedTextColor = if (textColor != Color.Unspecified) {
        textColor
    } else if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    if (maxLinesCollapsed != null && !isExpanded) {
        // En modo colapsado, mostrar solo una vista previa resumida de texto plano
        val summaryText = remember(text) {
            text.lines()
                .filter { !it.trim().startsWith("|") && !it.trim().matches(Regex("^([\\-*_])(\\s*\\1){2,}$")) }
                .joinToString("\n")
                .trim()
                .ifEmpty { text }
        }
        Text(
            text = remember(summaryText) { formatMarkdown(summaryText) },
            style = textStyle,
            color = resolvedTextColor,
            maxLines = maxLinesCollapsed,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
        )
    } else {
        // Modo expandido: renderizar bloques estructurados (Párrafos, Divisores, Tablas)
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            blocks.forEach { block ->
                when (block) {
                    is CopilotContentBlock.TextBlock -> {
                        Text(
                            text = remember(block.content) { formatMarkdown(block.content) },
                            style = textStyle,
                            color = resolvedTextColor
                        )
                    }
                    is CopilotContentBlock.DividerBlock -> {
                        CopilotDividerComposable(themeStyle = themeStyle)
                    }
                    is CopilotContentBlock.TableBlock -> {
                        CopilotTableComposable(
                            table = block,
                            themeStyle = themeStyle
                        )
                    }
                }
            }
        }
    }
}
