package me.joxquin.notivas.ui.notas.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.SimulationGroup
import me.joxquin.notivas.data.model.SimulationItem
import me.joxquin.notivas.ui.notas.SimulationGroupUiModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

@Composable
fun GruposPorcentajeCard(
    simulationGroups: List<SimulationGroupUiModel>,
    totalConfiguredWeight: Float,
    onAddGroup: () -> Unit,
    onAddItem: (Long) -> Unit,
    onEditGroup: (SimulationGroup) -> Unit,
    onUpdateItemScore: (Long, Float) -> Unit,
    onDeleteGroup: (SimulationGroup) -> Unit,
    onDeleteItem: (SimulationItem) -> Unit,
    onLinkPlaceholder: (SimulationItem) -> Unit = {},
    onImportTemplateClick: () -> Unit = {},
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    modifier: Modifier = Modifier
) {
    val isWeightComplete = totalConfiguredWeight.roundToInt() == 100

    if (themeStyle == AppThemeStyle.MIUIX) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .squircleSurface(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                    cornerRadius = 24.dp
                )
                .squircleBorder(
                    width = 0.5.dp,
                    color = MiuixTheme.colorScheme.dividerLine,
                    cornerRadius = 24.dp
                )
                .padding(18.dp)
                .animateContentSize(),
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
                        imageVector = Icons.Default.GroupWork,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Grupos y porcentaje",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .squircleSurface(
                            color = if (isWeightComplete) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                            cornerRadius = 9999.dp
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${totalConfiguredWeight.roundToInt()}% Ponderado",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWeightComplete) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                }
            }

            if (simulationGroups.isEmpty()) {
                EmptyGruposState(
                    onImportTemplateClick = onImportTemplateClick,
                    onCreateManualGroupClick = onAddGroup
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    simulationGroups.forEach { groupModel ->
                        SingleGroupItemMiuix(
                            groupModel = groupModel,
                            onAddItem = { onAddItem(groupModel.group.id) },
                            onEditGroup = { onEditGroup(groupModel.group) },
                            onUpdateScore = onUpdateItemScore,
                            onDeleteGroup = { onDeleteGroup(groupModel.group) },
                            onDeleteItem = onDeleteItem,
                            onLinkPlaceholder = onLinkPlaceholder
                        )
                    }

                    Button(
                        onClick = onAddGroup,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "+ Añadir grupo de evaluación",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            imageVector = Icons.Default.GroupWork,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Grupos y porcentaje",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (isWeightComplete) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = "${totalConfiguredWeight.roundToInt()}% Ponderado",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isWeightComplete) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (simulationGroups.isEmpty()) {
                    EmptyGruposState(
                        onImportTemplateClick = onImportTemplateClick,
                        onCreateManualGroupClick = onAddGroup
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        simulationGroups.forEach { groupModel ->
                            SingleGroupItemMaterial(
                                groupModel = groupModel,
                                onAddItem = { onAddItem(groupModel.group.id) },
                                onEditGroup = { onEditGroup(groupModel.group) },
                                onUpdateScore = onUpdateItemScore,
                                onDeleteGroup = { onDeleteGroup(groupModel.group) },
                                onDeleteItem = onDeleteItem,
                                onLinkPlaceholder = onLinkPlaceholder
                            )
                        }

                        TextButton(
                            onClick = onAddGroup,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "+ Añadir grupo de evaluación",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleGroupItemMiuix(
    groupModel: SimulationGroupUiModel,
    onAddItem: () -> Unit,
    onEditGroup: () -> Unit,
    onUpdateScore: (Long, Float) -> Unit,
    onDeleteGroup: () -> Unit,
    onDeleteItem: (SimulationItem) -> Unit,
    onLinkPlaceholder: (SimulationItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val avgScore = ((groupModel.groupAverage * 10f).roundToInt() / 10f).toString()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .squircleSurface(
                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                cornerRadius = 16.dp
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = groupModel.group.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Peso: ${groupModel.group.weightPercentage.roundToInt()}%",
                        fontSize = 11.sp,
                        color = MiuixTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                    Text(
                        text = "Prom: $avgScore (${groupModel.items.size} notas)",
                        fontSize = 11.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                    if (groupModel.group.dropLowest) {
                        Box(
                            modifier = Modifier
                                .squircleSurface(
                                    color = Color(0xFFFFF3E0),
                                    cornerRadius = 6.dp
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Descarta menor",
                                fontSize = 9.sp,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onAddItem, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir evaluación",
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onEditGroup, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar grupo",
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDeleteGroup, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar grupo",
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (groupModel.items.isEmpty()) {
                    Text(
                        text = "Sin evaluaciones. Pulsa + para agregar desde Canvas o manual.",
                        fontSize = 12.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                } else {
                    groupModel.items.forEach { item ->
                        val isManualOrSimulated = item.isPlaceholder || item.isSimulated
                        val isGraded = item.manualScore != null

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .squircleSurface(
                                    color = MiuixTheme.colorScheme.surface,
                                    cornerRadius = 10.dp
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (item.isPlaceholder) {
                                        Box(
                                            modifier = Modifier
                                                .squircleSurface(
                                                    color = Color(0xFFEDE7F6),
                                                    cornerRadius = 4.dp
                                                )
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("Vacío", fontSize = 9.sp, color = Color(0xFF512DA8), fontWeight = FontWeight.Bold)
                                        }
                                    } else if (isGraded) {
                                        Box(
                                            modifier = Modifier
                                                .squircleSurface(
                                                    color = Color(0xFFE8F5E9),
                                                    cornerRadius = 4.dp
                                                )
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("Calificada", fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = item.name,
                                        fontSize = 12.sp,
                                        color = MiuixTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Slider(
                                value = item.simulatedScore,
                                onValueChange = { onUpdateScore(item.id, it) },
                                enabled = !isGraded,
                                valueRange = 0f..20f,
                                steps = 39,
                                modifier = Modifier.width(110.dp)
                            )
                            Text(
                                text = "${((item.simulatedScore * 10f).roundToInt() / 10f)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGraded) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.primary,
                                modifier = Modifier.width(36.dp)
                            )
                            if (item.isPlaceholder) {
                                IconButton(
                                    onClick = { onLinkPlaceholder(item) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = "Vincular",
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onDeleteItem(item) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Eliminar",
                                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = onAddItem,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "+ Añadir evaluación",
                        fontSize = 12.sp,
                        color = MiuixTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SingleGroupItemMaterial(
    groupModel: SimulationGroupUiModel,
    onAddItem: () -> Unit,
    onEditGroup: () -> Unit,
    onUpdateScore: (Long, Float) -> Unit,
    onDeleteGroup: () -> Unit,
    onDeleteItem: (SimulationItem) -> Unit,
    onLinkPlaceholder: (SimulationItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val avgScore = ((groupModel.groupAverage * 10f).roundToInt() / 10f).toString()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = groupModel.group.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Peso: ${groupModel.group.weightPercentage.roundToInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Prom: $avgScore (${groupModel.items.size} notas)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (groupModel.group.dropLowest) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = "Descarta peor",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAddItem, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir evaluación",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onEditGroup, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar grupo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDeleteGroup, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar grupo",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (groupModel.items.isEmpty()) {
                        Text(
                            text = "Sin evaluaciones en este grupo. Pulsa + para añadir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        groupModel.items.forEach { item ->
                            val isGraded = item.manualScore != null

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (item.isPlaceholder) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                                ) {
                                                    Text(
                                                        text = "Vacío",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                    )
                                                }
                                            } else if (isGraded) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        text = "Calificada",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Slider(
                                        value = item.simulatedScore,
                                        onValueChange = { onUpdateScore(item.id, it) },
                                        enabled = !isGraded,
                                        valueRange = 0f..20f,
                                        steps = 39,
                                        modifier = Modifier.width(110.dp)
                                    )
                                    Text(
                                        text = "${((item.simulatedScore * 10f).roundToInt() / 10f)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isGraded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(36.dp)
                                    )
                                    if (item.isPlaceholder) {
                                        IconButton(
                                            onClick = { onLinkPlaceholder(item) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Link,
                                                contentDescription = "Vincular",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteItem(item) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = onAddItem,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "+ Añadir evaluación",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}
