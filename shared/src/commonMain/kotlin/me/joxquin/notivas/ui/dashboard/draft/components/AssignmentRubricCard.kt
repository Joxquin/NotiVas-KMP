package me.joxquin.notivas.ui.dashboard.draft.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
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
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.shared.resources.*
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class RubricCriterionUi(
    val title: String,
    val points: Double,
    val description: String
)

@Composable
fun AssignmentRubricCard(
    assignment: Assignment,
    themeStyle: AppThemeStyle,
    modifier: Modifier = Modifier
) {
    // Si la descripción o metadatos contienen información de rúbrica, o si se definió una rúbrica
    // De lo contrario, reflejar estado real: no tiene rúbrica configurada en Canvas LMS
    val hasRubrics = remember(assignment) {
        val desc = assignment.description?.lowercase() ?: ""
        desc.contains("rúbrica") || desc.contains("rubric") || desc.contains("criterio")
    }

    val totalPoints = assignment.pointsPossible ?: 20.0
    val perCriterionPoints = totalPoints / 4.0

    val criteria = remember(assignment, hasRubrics) {
        if (hasRubrics) {
            listOf(
                RubricCriterionUi("Planteamiento", perCriterionPoints, "Parametrización exacta"),
                RubricCriterionUi("Demostración", perCriterionPoints, "Aplicación Teorema"),
                RubricCriterionUi("Gráficas", perCriterionPoints, "Vectores y trayectoria"),
                RubricCriterionUi("Conclusión", perCriterionPoints, "Análisis de flujo")
            )
        } else {
            emptyList()
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .squircleSurface(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                    cornerRadius = 20.dp
                )
                .squircleBorder(
                    width = 1.dp,
                    color = MiuixTheme.colorScheme.outline.copy(alpha = 0.08f),
                    cornerRadius = 20.dp
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        MiuixText(
                            text = stringResource(Res.string.task_rubric_header),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }

                    if (criteria.isNotEmpty()) {
                        MiuixText(
                            text = stringResource(Res.string.task_rubric_criteria_count, criteria.size),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }

                if (criteria.isEmpty()) {
                    // Estado vacío amigable informativo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(20.dp)
                        )
                        MiuixText(
                            text = stringResource(Res.string.task_rubric_empty),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                } else {
                    // 2x2 Grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RubricItemMiuix(criteria[0], Modifier.weight(1f))
                            RubricItemMiuix(criteria[1], Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RubricItemMiuix(criteria[2], Modifier.weight(1f))
                            RubricItemMiuix(criteria[3], Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    } else {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(Res.string.task_rubric_header),
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (criteria.isNotEmpty()) {
                        Text(
                            text = stringResource(Res.string.task_rubric_criteria_count, criteria.size),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (criteria.isEmpty()) {
                    // Estado vacío amigable en Material 3
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(Res.string.task_rubric_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // 2x2 Grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RubricItemMaterial(criteria[0], Modifier.weight(1f))
                            RubricItemMaterial(criteria[1], Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RubricItemMaterial(criteria[2], Modifier.weight(1f))
                            RubricItemMaterial(criteria[3], Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RubricItemMiuix(item: RubricCriterionUi, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .squircleSurface(
                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                cornerRadius = 14.dp
            )
            .squircleBorder(
                width = 1.dp,
                color = MiuixTheme.colorScheme.outline.copy(alpha = 0.05f),
                cornerRadius = 14.dp
            )
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiuixText(
                    text = item.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                val pointsFormatted = if (item.points % 1.0 == 0.0) "${item.points.toInt()} pts" else "${item.points} pts"
                MiuixText(
                    text = pointsFormatted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.65f))
            )

            MiuixText(
                text = item.description,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RubricItemMaterial(item: RubricCriterionUi, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                val pointsFormatted = if (item.points % 1.0 == 0.0) "${item.points.toInt()} pts" else "${item.points} pts"
                Text(
                    text = pointsFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.65f))
            )

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
