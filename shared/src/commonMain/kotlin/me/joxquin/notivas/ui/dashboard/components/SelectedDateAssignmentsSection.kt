package me.joxquin.notivas.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.util.DateComponents
import me.joxquin.notivas.util.DateTimeUtil

@Composable
fun SelectedDateAssignmentsSection(
    selectedDate: DateComponents,
    assignments: List<AssignmentUiModel>,
    onClearDate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatted = DateTimeUtil.formatFullDate(selectedDate)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Entregas: $dateFormatted",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(
                onClick = onClearDate,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Ver todo",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (assignments.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Text(
                    text = "No hay entregas programadas para esta fecha.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            assignments.forEach { item ->
                ExpressiveAssignmentCard(uiModel = item)
            }
        }
    }
}

@Composable
fun ExpressiveAssignmentCard(
    uiModel: AssignmentUiModel,
    modifier: Modifier = Modifier
) {
    val assignment = uiModel.assignment
    val accentColor = when (assignment.status) {
        "upcoming" -> MaterialTheme.colorScheme.primary
        "completed" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiModel.courseName,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                when (assignment.status) {
                    "completed" -> {
                        val gradeText = when {
                            assignment.score != null -> "${assignment.score} pts"
                            assignment.grade != null -> assignment.grade
                            else -> "Entregada"
                        }
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Text(gradeText, fontWeight = FontWeight.Bold)
                        }
                    }

                    "missing" -> {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ) {
                            Text("Vencida", fontWeight = FontWeight.Bold)
                        }
                    }

                    else -> {
                        if (assignment.dueAt == null) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Text("Sin fecha", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = assignment.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val ptsText = assignment.pointsPossible?.let { "${it.toInt()} pts" } ?: "Sin puntos"
                Text(
                    text = "Valor: $ptsText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val dueText = assignment.dueAt?.let { iso ->
                    val comp = DateTimeUtil.parseIsoToComponents(iso)
                    if (comp != null) {
                        val d = comp.date
                        val h = if (comp.hour < 10) "0${comp.hour}" else "${comp.hour}"
                        val m = if (comp.minute < 10) "0${comp.minute}" else "${comp.minute}"
                        "${d.day}/${d.month} $h:$m"
                    } else {
                        iso
                    }
                } ?: "Sin fecha límite"

                val dueLabel = if (assignment.status == "missing") "Venció:" else "Vence:"
                Text(
                    text = "$dueLabel $dueText",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (assignment.status == "missing") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
