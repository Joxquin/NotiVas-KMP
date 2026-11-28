package me.joxquin.notivas.ui.dashboard.draft.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.domain.team.TeamChecklistItem
import me.joxquin.notivas.domain.team.formatTeamChecklist
import me.joxquin.notivas.domain.team.teamChecklistCanvasLink
import me.joxquin.notivas.getPlatform
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.AcademicFeatureButton
import me.joxquin.notivas.ui.components.AcademicFeatureCard
import me.joxquin.notivas.util.PlatformShareHandler
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val checklistSaver = Saver<List<TeamChecklistItem>, String>(
    save = { Json.encodeToString(it) },
    restore = { Json.decodeFromString(it) }
)
private val consignaLink = Regex("""\[([^\]]+)\]\([^)]+\)""")
private val consignaUrl = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)
private val consecutiveSpace = Regex("""\s+""")

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TeamChecklistCard(
    assignment: Assignment,
    consignaSections: List<ConsignaSection>,
    baseUrl: String?,
    themeStyle: AppThemeStyle
) {
    val isMiuix = themeStyle == AppThemeStyle.MIUIX
    val onSurface = if (isMiuix) MiuixTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val detailColor = if (isMiuix) MiuixTheme.colorScheme.onSurfaceVariantSummary else MaterialTheme.colorScheme.onSurfaceVariant
    val title = stringResource(Res.string.team_checklist_title)
    var expanded by rememberSaveable(assignment.id) { mutableStateOf(false) }
    var items by rememberSaveable(assignment.id, stateSaver = checklistSaver) {
        val suggestions = consignaSections.flatMap { it.bullets }
            .map { bullet ->
                listOf(bullet.label, bullet.value).filter { it.isNotBlank() }.joinToString(" ")
                    .replace(consignaLink, "$1")
                    .replace(consignaUrl, "")
                    .replace("**", "")
                    .replace(consecutiveSpace, " ")
                    .trim()
            }
            .filter { it.isNotEmpty() }.distinct().take(4)
        mutableStateOf(suggestions.mapIndexed { index, task -> TeamChecklistItem(index, task) })
    }
    var nextId by rememberSaveable(assignment.id) { mutableStateOf(items.size) }
    var copied by remember(assignment.id) { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val platform = remember { getPlatform().name }
    val nativeShare = platform.startsWith("Android") || platform.startsWith("iOS") || platform.startsWith("iPadOS")

    fun update(item: TeamChecklistItem) {
        items = items.map { if (it.id == item.id) item else it }
        copied = false
    }

    AcademicFeatureCard(themeStyle) {
        Text(title, color = onSurface, style = MaterialTheme.typography.titleMedium)
        if (!expanded) {
            Text(stringResource(Res.string.team_checklist_intro), color = detailColor, style = MaterialTheme.typography.bodySmall)
            AcademicFeatureButton(
                text = stringResource(Res.string.team_checklist_open),
                themeStyle = themeStyle,
                onClick = {
                    if (items.isEmpty()) items = listOf(TeamChecklistItem(nextId++))
                    expanded = true
                }
            )
        } else {
            items.forEach { item ->
                key(item.id) {
                    HorizontalDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ChecklistField(item.task, stringResource(Res.string.team_checklist_task), themeStyle) {
                            update(item.copy(task = it))
                        }
                        ChecklistField(item.owner, stringResource(Res.string.team_checklist_owner), themeStyle) {
                            update(item.copy(owner = it))
                        }
                        ChecklistField(item.deadline, stringResource(Res.string.team_checklist_date), themeStyle) {
                            update(item.copy(deadline = it))
                        }
                        val doneLabel = stringResource(Res.string.team_checklist_done)
                        if (isMiuix) {
                            CheckboxPreference(title = doneLabel, checked = item.done, onCheckedChange = { update(item.copy(done = it)) })
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = item.done, onCheckedChange = { update(item.copy(done = it)) })
                                Text(doneLabel, color = onSurface, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        AcademicFeatureButton(
                            text = stringResource(Res.string.team_checklist_remove),
                            themeStyle = themeStyle,
                            onClick = {
                                items = items.filterNot { it.id == item.id }
                                copied = false
                            }
                        )
                    }
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AcademicFeatureButton(
                    text = stringResource(Res.string.team_checklist_add),
                    themeStyle = themeStyle,
                    onClick = {
                        items = items + TeamChecklistItem(nextId++)
                        copied = false
                    }
                )
                AcademicFeatureButton(
                    text = stringResource(Res.string.team_checklist_collapse),
                    themeStyle = themeStyle,
                    onClick = { expanded = false }
                )
            }
            if (items.any { it.task.isNotBlank() }) {
                val preview = formatTeamChecklist(
                    heading = stringResource(Res.string.team_checklist_share_heading, assignment.name),
                    items = items,
                    ownerLabel = stringResource(Res.string.team_checklist_owner),
                    deadlineLabel = stringResource(Res.string.team_checklist_date),
                    sourceUrl = teamChecklistCanvasLink(baseUrl, assignment.courseId, assignment.id)
                )
                Text(stringResource(Res.string.team_checklist_preview), color = onSurface, style = MaterialTheme.typography.titleSmall)
                SelectionContainer {
                    Text(preview, color = detailColor, style = MaterialTheme.typography.bodySmall)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (nativeShare) {
                        AcademicFeatureButton(
                            text = stringResource(Res.string.team_checklist_share),
                            themeStyle = themeStyle,
                            onClick = { PlatformShareHandler.shareText(preview, title) }
                        )
                    }
                    AcademicFeatureButton(
                        text = stringResource(if (copied) Res.string.team_checklist_copied else Res.string.team_checklist_copy),
                        themeStyle = themeStyle,
                        onClick = {
                            clipboard.setText(AnnotatedString(preview))
                            copied = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChecklistField(value: String, label: String, themeStyle: AppThemeStyle, onChange: (String) -> Unit) {
    if (themeStyle == AppThemeStyle.MIUIX) {
        MiuixTextField(value = value, onValueChange = onChange, label = label, singleLine = true, modifier = Modifier.fillMaxWidth())
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
