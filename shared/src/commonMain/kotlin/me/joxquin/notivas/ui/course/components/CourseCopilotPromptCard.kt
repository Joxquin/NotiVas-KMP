package me.joxquin.notivas.ui.course.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.shared.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/** Pregunta rápida: texto del botón y pregunta que se envía al Copiloto con el nombre del curso. */
private data class CoursePromptChip(val label: StringResource, val prompt: StringResource)

private val COURSE_PROMPT_CHIPS = listOf(
    CoursePromptChip(Res.string.course_copilot_chip_status, Res.string.course_copilot_prompt_status),
    CoursePromptChip(Res.string.course_copilot_chip_needed, Res.string.course_copilot_prompt_needed),
    CoursePromptChip(Res.string.course_copilot_chip_pending, Res.string.course_copilot_prompt_pending),
)

/**
 * Tarjeta del detalle del curso que abre el Copiloto con una pregunta sobre ese curso.
 * El Copiloto responde con las herramientas de notas (promedio, nota necesaria) y de tareas.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourseCopilotPromptCard(
    courseName: String,
    themeStyle: AppThemeStyle,
    onAsk: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val chips = COURSE_PROMPT_CHIPS.map { chip ->
        stringResource(chip.label) to stringResource(chip.prompt, courseName)
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixCard(
            modifier = modifier.fillMaxWidth(),
            colors = MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
            insideMargin = PaddingValues(16.dp),
            pressFeedbackType = PressFeedbackType.None
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiuixIcon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    MiuixText(
                        text = stringResource(Res.string.course_copilot_card_title),
                        style = MiuixTheme.textStyles.body1.copy(fontWeight = FontWeight.SemiBold),
                        color = MiuixTheme.colorScheme.onSurface
                    )
                }
                MiuixText(
                    text = stringResource(Res.string.course_copilot_card_subtitle),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chips.forEach { (label, prompt) ->
                        MiuixText(
                            text = label,
                            style = MiuixTheme.textStyles.body2.copy(fontWeight = FontWeight.Medium),
                            color = MiuixTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                                .clickable { onAsk(prompt) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(Res.string.course_copilot_card_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = stringResource(Res.string.course_copilot_card_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chips.forEach { (label, prompt) ->
                        SuggestionChip(
                            onClick = { onAsk(prompt) },
                            label = { Text(text = label) }
                        )
                    }
                }
            }
        }
    }
}
