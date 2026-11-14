package me.joxquin.notivas.ui.dashboard.draft.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.platform.LocalUriHandler
import me.joxquin.notivas.util.encodeUrlComponent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.dashboard.DashboardViewModel
import me.joxquin.notivas.ui.dashboard.components.draft.DraftStatusEnum
import me.joxquin.notivas.ui.dashboard.components.draft.DraftSyncInfo
import me.joxquin.notivas.util.SelectedDocument
import me.joxquin.notivas.util.fileDropTarget
import me.joxquin.notivas.util.rememberDocumentPicker
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class SubmissionTab {
    UPLOAD,
    DRIVE,
    TEXT
}

private val TEMPLATE_TITLE_DOCX_REGEX = Regex("""<a\s+[^>]*title=["']([^"']+\.docx)["']""", RegexOption.IGNORE_CASE)
private val TEMPLATE_TAG_DOCX_REGEX = Regex("""<a\s+[^>]*>([^<]+\.docx)</a>""", RegexOption.IGNORE_CASE)
private val TEMPLATE_WORD_DOCX_REGEX = Regex("""([\w\-_]+\.docx)""")
private val FORMAT_SECTION_REGEX = Regex("""(?:formato|formatos|tipo(?:s)? de archivo|extension(?:es)?)[^:<]*:\s*([^\n<]+)""", RegexOption.IGNORE_CASE)
private val EXTENSION_WORD_REGEXES = listOf("pdf", "docx", "doc", "zip", "rar", "xlsx", "xls", "pptx", "ppt", "txt").associateWith { ext ->
    Regex("""\b$ext\b""")
}
private val EXTENSIONS_CACHE = mutableMapOf<Int, List<String>>()
private val TEMPLATE_NAME_CACHE = mutableMapOf<Int, String?>()

internal fun formatCanvasLockDate(isoDate: String?): String? {
    if (isoDate.isNullOrBlank()) return null
    val comp = me.joxquin.notivas.util.DateTimeUtil.parseIsoToComponents(isoDate) ?: return null
    val monthNames = listOf(
        "", "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )
    val monthName = monthNames.getOrElse(comp.date.month) { comp.date.month.toString() }
    val hourStr = comp.hour.toString().padStart(2, '0')
    val minuteStr = comp.minute.toString().padStart(2, '0')
    return "${comp.date.day} de $monthName de ${comp.date.year} a las $hourStr:$minuteStr"
}

internal fun resolveAvailableSubmissionTabs(
    supportsUpload: Boolean,
    supportsText: Boolean,
    isDriveConnected: Boolean
): List<SubmissionTab> = buildList {
    if (supportsUpload) add(SubmissionTab.UPLOAD)
    if (isDriveConnected) add(SubmissionTab.DRIVE)
    if (supportsText) add(SubmissionTab.TEXT)
}


@Composable
fun AssignmentSubmissionCard(
    assignment: Assignment,
    courseName: String,
    themeStyle: AppThemeStyle,
    viewModel: DashboardViewModel?,
    draftInfo: DraftSyncInfo? = null,
    modifier: Modifier = Modifier,
    onStartDraft: (isTemplate: Boolean, templateName: String?) -> Unit = { _, _ -> },
    onExploreTemplates: () -> Unit = {},
    onCancelDraft: () -> Unit = {},
    onOpenDocs: (String) -> Unit = {},
    onSubmitDraftToCanvas: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val isDriveConnected by (viewModel?.isGoogleDriveConnected?.collectAsState()
        ?: remember { mutableStateOf(me.joxquin.notivas.di.AppModule.preferencesManager.isGoogleDriveConnected()) })

    val types = assignment.submissionTypes ?: emptyList()
    val supportsUpload = types.contains("online_upload")
    val supportsText = types.contains("online_text_entry")

    val availableTabs = remember(supportsUpload, supportsText, isDriveConnected) {
        resolveAvailableSubmissionTabs(supportsUpload, supportsText, isDriveConnected)
    }

    var selectedTab by remember(availableTabs) {
        mutableStateOf(availableTabs.firstOrNull() ?: SubmissionTab.UPLOAD)
    }

    LaunchedEffect(isDriveConnected) {
        if (!isDriveConnected && selectedTab == SubmissionTab.DRIVE) {
            selectedTab = availableTabs.firstOrNull() ?: SubmissionTab.UPLOAD
        }
    }

    var selectedDocument by remember { mutableStateOf<SelectedDocument?>(null) }
    var textBody by remember { mutableStateOf("") }
    var commentText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showAiReviewSheet by remember { mutableStateOf(false) }

    val effectiveAllowedExtensions: List<String> = remember(assignment.allowedExtensions, assignment.description) {
        val fromAssignment = assignment.allowedExtensions?.filter { it.isNotBlank() }?.map { it.removePrefix(".").lowercase() }
        if (!fromAssignment.isNullOrEmpty()) {
            fromAssignment
        } else {
            detectExtensionsFromHtml(assignment.description)
        }
    }

    val onDocumentLoaded: (SelectedDocument) -> Unit = { doc ->
        val ext = doc.name.substringAfterLast('.', "").lowercase()
        if (effectiveAllowedExtensions.isNotEmpty() && ext.isNotBlank() && ext !in effectiveAllowedExtensions) {
            val allowedUpper = effectiveAllowedExtensions.joinToString(", ") { it.uppercase() }
            errorMessage = "Formato no válido (.${ext}). Solo se admiten archivos: $allowedUpper"
            selectedDocument = null
        } else {
            selectedDocument = doc
            errorMessage = null
        }
    }

    var isDraggingOver by remember { mutableStateOf(false) }

    val documentPicker = rememberDocumentPicker { doc ->
        onDocumentLoaded(doc)
    }

    val isDraftSyncing by (viewModel?.isDraftSyncing?.collectAsState() ?: remember { mutableStateOf(false) })
    val isSyncingNow = isDraftSyncing || (draftInfo != null && draftInfo.draftStatus == DraftStatusEnum.IN_PROGRESS && draftInfo.driveUrl.isBlank())
    val isDraftActive = draftInfo != null && draftInfo.draftStatus != DraftStatusEnum.NOT_STARTED && !isSyncingNow && draftInfo.driveUrl.isNotBlank()

    val courseTemplates by (viewModel?.courseTemplates?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    // Detección contextual de plantilla docente en descripción HTML
    val detectedTemplateName = remember(assignment, courseTemplates) {
        val candidateMatch = courseTemplates.firstOrNull { it.isCurrentAssignmentModule }?.title
            ?: courseTemplates.firstOrNull()?.title
        candidateMatch ?: run {
            val desc = assignment.description ?: ""
            if (desc.isBlank()) return@run null
            TEMPLATE_NAME_CACHE.getOrPut(desc.hashCode()) {
                val linkMatch = TEMPLATE_TITLE_DOCX_REGEX.find(desc)?.groupValues?.get(1)
                    ?: TEMPLATE_TAG_DOCX_REGEX.find(desc)?.groupValues?.get(1)
                linkMatch
                    ?: TEMPLATE_WORD_DOCX_REGEX.find(desc)?.value
                    ?: if (desc.contains("docs.google.com")) "Plantilla_Docente_Enlace" else null
            }
        }
    }

    val currentDraftFileName = remember(draftInfo, detectedTemplateName, assignment.name) {
        draftInfo?.fileName?.takeIf { it.isNotBlank() }
            ?: detectedTemplateName
            ?: "${assignment.name.take(24)} - Borrador.docx"
    }

    val uriHandler = LocalUriHandler.current

    val isAlreadySubmitted = assignment.isCompleted ||
            assignment.status == "completed" ||
            assignment.submission?.workflowState in listOf("submitted", "graded") ||
            !assignment.submittedAt.isNullOrBlank() ||
            !assignment.submission?.submittedAt.isNullOrBlank()

    val scorePair = remember(assignment) {
        val score = assignment.score ?: assignment.submission?.score
        val points = assignment.pointsPossible
        if (score != null) {
            val s = if (score % 1.0 == 0.0) score.toInt().toString() else score.toString()
            val p = if (points != null && points > 0) {
                if (points % 1.0 == 0.0) points.toInt().toString() else points.toString()
            } else "-"
            s to p
        } else null
    }

    val nowMs = remember { me.joxquin.notivas.util.DateTimeUtil.nowEpochMillis() }
    val isHardLocked = remember(assignment, nowMs) { assignment.isHardLocked(nowMs) }
    val isPastDue = remember(assignment, nowMs) { assignment.isPastDue(nowMs) }
    val hasMoreAttempts = remember(assignment, isAlreadySubmitted) {
        assignment.hasRemainingAttempts(isAlreadySubmitted)
    }
    val canSubmit = remember(assignment, nowMs, isAlreadySubmitted) {
        assignment.canSubmit(nowMs, isAlreadySubmitted)
    }
    val formattedLockDate = remember(assignment.lockAt) {
        formatCanvasLockDate(assignment.lockAt)
    }

    var showDeliveryOptions by remember(isAlreadySubmitted, canSubmit) {
        mutableStateOf(!isAlreadySubmitted && canSubmit)
    }

    val formattedSubmittedAt = remember(assignment.submittedAt, assignment.submission?.submittedAt) {
        val dateStr = assignment.submittedAt ?: assignment.submission?.submittedAt
        if (dateStr.isNullOrBlank()) null
        else {
            val comp = me.joxquin.notivas.util.DateTimeUtil.parseIsoToComponents(dateStr)
            if (comp != null) {
                "${comp.date.day.toString().padStart(2, '0')}/${comp.date.month.toString().padStart(2, '0')}/${comp.date.year} ${comp.hour.toString().padStart(2, '0')}:${comp.minute.toString().padStart(2, '0')}"
            } else dateStr.take(16)
        }
    }

    val submittedAttachments = assignment.submission?.attachments.orEmpty()
    val submittedTextBody = assignment.submission?.body
    val submittedUrl = assignment.submission?.url
    val currentAttempt = assignment.submission?.attempt ?: 1
    val allowedAttempts = assignment.allowedAttempts

    fun handleSubmitDirect() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        errorMessage = null

        if (selectedTab == SubmissionTab.UPLOAD) {
            if (selectedDocument == null) {
                errorMessage = "Por favor selecciona un archivo para entregar"
                return
            }
            val ext = selectedDocument?.name?.substringAfterLast('.', "")?.lowercase().orEmpty()
            if (effectiveAllowedExtensions.isNotEmpty() && ext.isNotBlank() && ext !in effectiveAllowedExtensions) {
                val allowedUpper = effectiveAllowedExtensions.joinToString(", ") { it.uppercase() }
                errorMessage = "Formato no válido. Solo se admiten archivos $allowedUpper"
                return
            }
        } else if (selectedTab == SubmissionTab.TEXT) {
            if (textBody.isBlank()) {
                errorMessage = "Por favor ingresa el texto de la entrega"
                return
            }
        }

        if (viewModel == null) {
            errorMessage = "Servicio no disponible"
            return
        }

        isSubmitting = true
        scope.launch {
            val submissionTypeStr = if (selectedTab == SubmissionTab.UPLOAD) "online_upload" else "online_text_entry"
            viewModel.submitAssignmentDirect(
                assignment = assignment,
                submissionType = submissionTypeStr,
                fileBytes = selectedDocument?.bytes,
                fileName = selectedDocument?.name,
                mimeType = selectedDocument?.mimeType ?: "application/octet-stream",
                bodyText = if (selectedTab == SubmissionTab.TEXT) textBody.trim() else null,
                comment = commentText.trim().ifBlank { null },
                onSuccess = {
                    isSubmitting = false
                    successMessage = "Tarea entregada con éxito en Canvas"
                    selectedDocument = null
                    textBody = ""
                    commentText = ""
                    onSubmitSuccess()
                },
                onError = { err ->
                    isSubmitting = false
                    errorMessage = err.ifBlank { "Error al procesar la entrega" }
                }
            )
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        if (!isAlreadySubmitted && !canSubmit) {
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(20.dp)
                        )
                        MiuixText(
                            text = stringResource(Res.string.assignment_submission_locked_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                    }
                    val lockedMessage = when {
                        formattedLockDate != null && isHardLocked -> {
                            stringResource(Res.string.assignment_submission_locked_by_date, formattedLockDate)
                        }
                        !assignment.supportsDirectSubmission -> {
                            stringResource(Res.string.assignment_submission_no_online_submissions)
                        }
                        else -> {
                            stringResource(Res.string.assignment_submission_locked_generic)
                        }
                    }
                    MiuixText(
                        text = lockedMessage,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }
        } else {
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
                    // Encabezado con estado de entrega
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
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            MiuixText(
                                text = stringResource(Res.string.assignment_submission_card_title),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                        }

                        if (isAlreadySubmitted) {
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.clip(RoundedCornerShape(9999.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    MiuixText(
                                        text = stringResource(Res.string.assignment_submission_status_submitted),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MiuixTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else if (isPastDue) {
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = MiuixTheme.colorScheme.errorContainer,
                                modifier = Modifier.clip(RoundedCornerShape(9999.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    MiuixText(
                                        text = stringResource(Res.string.assignment_submission_late_allowed_badge),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MiuixTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                if (isAlreadySubmitted) {
                    if (scorePair != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .squircleSurface(
                                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                    cornerRadius = 14.dp
                                )
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MiuixText(
                                text = stringResource(Res.string.assignment_submission_score_label),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                            MiuixText(
                                text = stringResource(
                                    Res.string.assignment_quiz_score_format,
                                    scorePair.first,
                                    scorePair.second
                                ),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .squircleSurface(
                                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                    cornerRadius = 14.dp
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.size(16.dp)
                            )
                            MiuixText(
                                text = stringResource(Res.string.assignment_submission_pending_grade),
                                fontSize = 12.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }

                    if (submittedAttachments.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            submittedAttachments.forEach { attachment ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .squircleSurface(
                                            color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                            cornerRadius = 14.dp
                                        )
                                        .squircleBorder(
                                            width = 1.dp,
                                            color = MiuixTheme.colorScheme.outline.copy(alpha = 0.1f),
                                            cornerRadius = 14.dp
                                        )
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .squircleSurface(
                                                        color = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                        cornerRadius = 10.dp
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.InsertDriveFile,
                                                    contentDescription = null,
                                                    tint = MiuixTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                MiuixText(
                                                    text = attachment.displayName ?: attachment.filename ?: stringResource(Res.string.assignment_submission_submitted_file_title),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MiuixTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    if (attachment.size != null && attachment.size > 0L) {
                                                        MiuixText(
                                                            text = formatFileSize(attachment.size),
                                                            fontSize = 11.sp,
                                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                                        )
                                                    }
                                                    if (formattedSubmittedAt != null) {
                                                        MiuixText(
                                                            text = "• " + stringResource(Res.string.assignment_submission_submitted_at_prefix, formattedSubmittedAt),
                                                            fontSize = 11.sp,
                                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .squircleSurface(
                                                        color = MiuixTheme.colorScheme.surfaceContainer,
                                                        cornerRadius = 10.dp
                                                    )
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        val targetUrl = resolveViewableUrl(attachment, assignment.htmlUrl)
                                                        targetUrl?.let { openUrlSafely(it, uriHandler) }
                                                    }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Visibility,
                                                        contentDescription = null,
                                                        tint = MiuixTheme.colorScheme.primary,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    MiuixText(
                                                        text = stringResource(Res.string.assignment_submission_view_file),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MiuixTheme.colorScheme.primary
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .squircleSurface(
                                                        color = MiuixTheme.colorScheme.surfaceContainer,
                                                        cornerRadius = 10.dp
                                                    )
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        attachment.url?.let { openUrlSafely(it, uriHandler) }
                                                    }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Download,
                                                        contentDescription = null,
                                                        tint = MiuixTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    MiuixText(
                                                        text = stringResource(Res.string.assignment_submission_download_file),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MiuixTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (!submittedTextBody.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .squircleSurface(
                                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                    cornerRadius = 14.dp
                                )
                                .squircleBorder(
                                    width = 1.dp,
                                    color = MiuixTheme.colorScheme.outline.copy(alpha = 0.1f),
                                    cornerRadius = 14.dp
                                )
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    MiuixText(
                                        text = stringResource(Res.string.assignment_submission_submitted_text_title),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.onSurface
                                    )
                                }
                                MiuixText(
                                    text = stripHtmlTags(submittedTextBody),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                                if (formattedSubmittedAt != null) {
                                    MiuixText(
                                        text = stringResource(Res.string.assignment_submission_submitted_at_prefix, formattedSubmittedAt),
                                        fontSize = 11.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                }
                            }
                        }
                    } else if (!submittedUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .squircleSurface(
                                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                    cornerRadius = 14.dp
                                )
                                .squircleBorder(
                                    width = 1.dp,
                                    color = MiuixTheme.colorScheme.outline.copy(alpha = 0.1f),
                                    cornerRadius = 14.dp
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        MiuixText(
                                            text = stringResource(Res.string.assignment_submission_submitted_url_title),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MiuixTheme.colorScheme.onSurface
                                        )
                                        MiuixText(
                                            text = submittedUrl,
                                            fontSize = 11.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .squircleSurface(
                                            color = MiuixTheme.colorScheme.primary,
                                            cornerRadius = 10.dp
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            openUrlSafely(submittedUrl, uriHandler)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    MiuixText(
                                        text = stringResource(Res.string.assignment_submission_view_file),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MiuixText(
                            text = if (allowedAttempts != null && allowedAttempts > 0) {
                                stringResource(Res.string.assignment_submission_attempt_indicator, currentAttempt, allowedAttempts)
                            } else {
                                stringResource(Res.string.assignment_submission_attempt_unlimited, currentAttempt)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }

                    if (canSubmit) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .squircleSurface(
                                    color = if (showDeliveryOptions) MiuixTheme.colorScheme.surfaceContainerHigh else MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    cornerRadius = 12.dp
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showDeliveryOptions = !showDeliveryOptions
                                }
                                .padding(vertical = 11.dp, horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (showDeliveryOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = if (showDeliveryOptions) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                MiuixText(
                                    text = stringResource(
                                        if (showDeliveryOptions) Res.string.assignment_submission_toggle_hide_options
                                        else Res.string.assignment_submission_toggle_show_options
                                    ),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (showDeliveryOptions) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .squircleSurface(
                                    color = MiuixTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                                    cornerRadius = 12.dp
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isHardLocked) Icons.Default.Lock else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isHardLocked) MiuixTheme.colorScheme.onSurfaceVariantSummary else MiuixTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            val noticeText = when {
                                formattedLockDate != null && isHardLocked -> stringResource(Res.string.assignment_submission_locked_by_date, formattedLockDate)
                                isHardLocked -> stringResource(Res.string.assignment_submission_locked_generic)
                                !hasMoreAttempts -> stringResource(Res.string.assignment_submission_no_more_attempts)
                                else -> stringResource(Res.string.assignment_submission_locked_generic)
                            }
                            MiuixText(
                                text = noticeText,
                                fontSize = 12.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showDeliveryOptions && canSubmit,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Selector de pestañas unificado: [ Archivo local ] [ Borrador Drive ] [ Texto ]
                        if (availableTabs.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .squircleSurface(
                                        color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                        cornerRadius = 12.dp
                                    )
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (supportsUpload) {
                                    // Pestaña 1: Archivo local
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .squircleSurface(
                                                color = if (selectedTab == SubmissionTab.UPLOAD) MiuixTheme.colorScheme.surfaceContainer else MiuixTheme.colorScheme.surfaceContainerHigh,
                                                cornerRadius = 10.dp
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedTab = SubmissionTab.UPLOAD
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AttachFile,
                                                contentDescription = null,
                                                tint = if (selectedTab == SubmissionTab.UPLOAD) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            MiuixText(
                                                text = stringResource(Res.string.assignment_submission_tab_upload),
                                                fontSize = 12.sp,
                                                fontWeight = if (selectedTab == SubmissionTab.UPLOAD) FontWeight.Bold else FontWeight.Medium,
                                                color = if (selectedTab == SubmissionTab.UPLOAD) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                if (isDriveConnected) {
                                    // Pestaña 2: Borrador Drive
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .squircleSurface(
                                                color = if (selectedTab == SubmissionTab.DRIVE) MiuixTheme.colorScheme.surfaceContainer else MiuixTheme.colorScheme.surfaceContainerHigh,
                                                cornerRadius = 10.dp
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedTab = SubmissionTab.DRIVE
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudSync,
                                                contentDescription = null,
                                                tint = if (selectedTab == SubmissionTab.DRIVE) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            MiuixText(
                                                text = stringResource(Res.string.assignment_submission_tab_drive),
                                                fontSize = 12.sp,
                                                fontWeight = if (selectedTab == SubmissionTab.DRIVE) FontWeight.Bold else FontWeight.Medium,
                                                color = if (selectedTab == SubmissionTab.DRIVE) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                if (supportsText) {
                                    // Pestaña 3: Texto (si aplica)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .squircleSurface(
                                                color = if (selectedTab == SubmissionTab.TEXT) MiuixTheme.colorScheme.surfaceContainer else MiuixTheme.colorScheme.surfaceContainerHigh,
                                                cornerRadius = 10.dp
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedTab = SubmissionTab.TEXT
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EditNote,
                                                contentDescription = null,
                                                tint = if (selectedTab == SubmissionTab.TEXT) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            MiuixText(
                                                text = stringResource(Res.string.assignment_submission_tab_text),
                                                fontSize = 12.sp,
                                                fontWeight = if (selectedTab == SubmissionTab.TEXT) FontWeight.Bold else FontWeight.Medium,
                                                color = if (selectedTab == SubmissionTab.TEXT) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                // CONTENIDO SEGÚN LA PESTAÑA ACTIVA
                when (selectedTab) {
                    SubmissionTab.UPLOAD -> {
                        if (selectedDocument == null) {
                            // Dropzone para seleccionar archivo del dispositivo
                            val miuixDropBorderColor = if (isDraggingOver) {
                                MiuixTheme.colorScheme.primary
                            } else {
                                MiuixTheme.colorScheme.outline.copy(alpha = 0.15f)
                            }
                            val miuixDropContainerColor = if (isDraggingOver) {
                                MiuixTheme.colorScheme.primary.copy(alpha = 0.08f)
                            } else {
                                MiuixTheme.colorScheme.surfaceContainerHigh
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fileDropTarget(
                                        enabled = true,
                                        allowedExtensions = effectiveAllowedExtensions.ifEmpty { null },
                                        onDragStateChange = { isDraggingOver = it },
                                        onDocumentDropped = { onDocumentLoaded(it) }
                                    )
                                    .squircleSurface(
                                        color = miuixDropContainerColor,
                                        cornerRadius = 14.dp
                                    )
                                    .squircleBorder(
                                        width = if (isDraggingOver) 2.dp else 1.dp,
                                        color = miuixDropBorderColor,
                                        cornerRadius = 14.dp
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        documentPicker.launchDocumentPicker(effectiveAllowedExtensions.ifEmpty { null })
                                    }
                                    .padding(vertical = 24.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.UploadFile,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    MiuixText(
                                        text = if (isDraggingOver) {
                                            stringResource(Res.string.assignment_submission_drop_here)
                                        } else {
                                            stringResource(Res.string.assignment_submission_pick_file)
                                        },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDraggingOver) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                                    )
                                    if (!isDraggingOver) {
                                        MiuixText(
                                            text = stringResource(Res.string.assignment_submission_drag_drop_hint),
                                            fontSize = 12.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        )
                                    }
                                    if (effectiveAllowedExtensions.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(9999.dp),
                                            color = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            modifier = Modifier.clip(RoundedCornerShape(9999.dp))
                                        ) {
                                            MiuixText(
                                                text = stringResource(
                                                    Res.string.assignment_submission_required_format,
                                                    effectiveAllowedExtensions.joinToString(", ") { it.uppercase() }
                                                ),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MiuixTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        MiuixText(
                                            text = stringResource(Res.string.assignment_submission_formats_hint),
                                            fontSize = 11.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        )
                                    }
                                }
                            }
                        } else {
                            // Tarjeta de archivo seleccionado
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .squircleSurface(
                                        color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                        cornerRadius = 14.dp
                                    )
                                    .squircleBorder(
                                        width = 1.dp,
                                        color = MiuixTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        cornerRadius = 14.dp
                                    )
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        MiuixText(
                                            text = selectedDocument?.name.orEmpty(),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MiuixTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        MiuixText(
                                            text = formatFileSize(selectedDocument?.sizeBytes ?: 0L),
                                            fontSize = 11.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { documentPicker.launchDocumentPicker(effectiveAllowedExtensions.ifEmpty { null }) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = stringResource(Res.string.assignment_submission_change_file),
                                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { selectedDocument = null },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(Res.string.assignment_submission_remove_file),
                                                tint = MiuixTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SubmissionTab.DRIVE -> {
                        if (isDriveConnected) {
                            // Sección integrada de Google Drive
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Visualización de documento / plantilla de Drive
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .squircleSurface(
                                        color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                        cornerRadius = 14.dp
                                    )
                                    .squircleBorder(
                                        width = 1.dp,
                                        color = MiuixTheme.colorScheme.outline.copy(alpha = 0.12f),
                                        cornerRadius = 14.dp
                                    )
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .squircleSurface(
                                                color = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                cornerRadius = 10.dp
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isDraftActive) Icons.Default.CloudSync else Icons.Default.InsertDriveFile,
                                            contentDescription = null,
                                            tint = MiuixTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            MiuixText(
                                                text = currentDraftFileName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MiuixTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MiuixTheme.colorScheme.surfaceContainerHighest
                                            ) {
                                                MiuixText(
                                                    text = if (currentDraftFileName.endsWith(".docx", ignoreCase = true)) "DOCX" else "GDOC",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MiuixTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        MiuixText(
                                            text = if (isSyncingNow) {
                                                stringResource(Res.string.draft_syncing_cloud_status)
                                            } else if (isDraftActive) {
                                                "Borrador sincronizado Google Drive"
                                            } else if (detectedTemplateName != null) {
                                                "Plantilla oficial Canvas LMS"
                                            } else {
                                                "Borrador nuevo en Google Drive"
                                            },
                                            fontSize = 11.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        )
                                    }
                                }
                            }

                            // Botones de acción de Google Drive
                            if (isSyncingNow) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .squircleSurface(
                                            color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                            cornerRadius = 12.dp
                                        )
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = MiuixTheme.colorScheme.primary
                                        )
                                        MiuixText(
                                            text = stringResource(Res.string.draft_syncing_cloud_status),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MiuixTheme.colorScheme.primary
                                        )
                                    }
                                }
                            } else if (!isDraftActive) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .squircleSurface(
                                            color = MiuixTheme.colorScheme.primary,
                                            cornerRadius = 12.dp
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onStartDraft(detectedTemplateName != null, detectedTemplateName)
                                        }
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PostAdd,
                                            contentDescription = null,
                                            tint = MiuixTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        MiuixText(
                                            text = if (detectedTemplateName != null) {
                                                "Iniciar copia de: ${detectedTemplateName.take(22)}..."
                                            } else {
                                                "Crear borrador en Google Drive"
                                            },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MiuixTheme.colorScheme.onPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .squircleSurface(
                                            color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                            cornerRadius = 12.dp
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onExploreTemplates()
                                        }
                                        .padding(vertical = 11.dp, horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FolderOpen,
                                            contentDescription = null,
                                            tint = MiuixTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        MiuixText(
                                            text = "Buscar plantilla en módulos del curso",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MiuixTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .squircleSurface(
                                                color = MiuixTheme.colorScheme.primary,
                                                cornerRadius = 12.dp
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onOpenDocs(draftInfo?.driveUrl.orEmpty())
                                            }
                                            .padding(vertical = 11.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                                contentDescription = null,
                                                tint = MiuixTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            MiuixText(
                                                text = "Abrir en Docs",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MiuixTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }

                                    // Botón HTTP PATCH para renombrar en Google Drive
                                    Box(
                                        modifier = Modifier
                                            .squircleSurface(
                                                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                                cornerRadius = 12.dp
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel?.renameDraft(assignment.id, "v2")
                                            }
                                            .padding(horizontal = 14.dp, vertical = 11.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditNote,
                                            contentDescription = "Renombrar vía PATCH",
                                            tint = MiuixTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .squircleSurface(
                                                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                                cornerRadius = 12.dp
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onCancelDraft()
                                            }
                                            .padding(horizontal = 14.dp, vertical = 11.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Cancelar borrador",
                                            tint = MiuixTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }

                    SubmissionTab.TEXT -> {
                        // Editor de texto
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .squircleSurface(
                                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                    cornerRadius = 14.dp
                                )
                                .squircleBorder(
                                    width = 1.dp,
                                    color = MiuixTheme.colorScheme.outline.copy(alpha = 0.1f),
                                    cornerRadius = 14.dp
                                )
                                .padding(14.dp)
                        ) {
                            BasicTextField(
                                value = textBody,
                                onValueChange = {
                                    textBody = it
                                    errorMessage = null
                                },
                                textStyle = TextStyle(
                                    color = MiuixTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                ),
                                cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                decorationBox = { innerTextField ->
                                    if (textBody.isEmpty()) {
                                        MiuixText(
                                            text = stringResource(Res.string.assignment_submission_text_hint),
                                            fontSize = 13.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.6f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                // Sección común de comentarios para el docente
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .squircleSurface(
                            color = MiuixTheme.colorScheme.surfaceContainerHigh,
                            cornerRadius = 12.dp
                        )
                        .squircleBorder(
                            width = 1.dp,
                            color = MiuixTheme.colorScheme.outline.copy(alpha = 0.08f),
                            cornerRadius = 12.dp
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(16.dp)
                        )
                        BasicTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            textStyle = TextStyle(
                                color = MiuixTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            ),
                            cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (commentText.isEmpty()) {
                                    MiuixText(
                                        text = stringResource(Res.string.assignment_submission_comment_hint),
                                        fontSize = 12.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.6f)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                // Mensajes de error y éxito
                AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                    errorMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .squircleSurface(
                                    color = MiuixTheme.colorScheme.errorContainer,
                                    cornerRadius = 10.dp
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            MiuixText(
                                text = msg,
                                fontSize = 12.sp,
                                color = MiuixTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = successMessage != null, enter = fadeIn(), exit = fadeOut()) {
                    successMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .squircleSurface(
                                    color = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    cornerRadius = 10.dp
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            MiuixText(
                                text = msg,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MiuixTheme.colorScheme.primary
                            )
                        }
                    }
                }

                val hasContentToReview = when (selectedTab) {
                    SubmissionTab.UPLOAD -> selectedDocument != null
                    SubmissionTab.DRIVE -> isDriveConnected && isDraftActive && !draftInfo?.driveFileId.isNullOrBlank()
                    SubmissionTab.TEXT -> textBody.isNotBlank()
                }

                // Botón animado de "Revisar con IA"
                AnimatedVisibility(
                    visible = canSubmit && !isSubmitting && hasContentToReview,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .squircleSurface(
                                color = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                                cornerRadius = 14.dp
                            )
                            .squircleBorder(
                                width = 1.dp,
                                color = MiuixTheme.colorScheme.primary.copy(alpha = 0.25f),
                                cornerRadius = 14.dp
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showAiReviewSheet = true
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                MiuixText(
                                    text = stringResource(Res.string.review_with_ai_button),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MiuixTheme.colorScheme.primary
                                )
                                MiuixText(
                                    text = stringResource(Res.string.review_with_ai_subtitle),
                                    fontSize = 11.sp,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            }
                        }
                    }
                }

                // Botón principal de Entrega
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .squircleSurface(
                            color = if (canSubmit && !isSubmitting) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainerHighest,
                            cornerRadius = 14.dp
                        )
                        .clickable(enabled = canSubmit && !isSubmitting) {
                            if (selectedTab == SubmissionTab.DRIVE && isDriveConnected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSubmitDraftToCanvas()
                            } else {
                                handleSubmitDirect()
                            }
                        }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSubmitting) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MiuixTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            MiuixText(
                                text = stringResource(Res.string.assignment_submission_submitting),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = if (canSubmit) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.size(16.dp)
                            )
                            MiuixText(
                                text = if (selectedTab == SubmissionTab.DRIVE && isDriveConnected) {
                                    if (isDraftActive) "Entregar borrador a Canvas" else "Crear borrador primero"
                                } else {
                                    if (isAlreadySubmitted) stringResource(Res.string.assignment_submission_resubmit_action) else stringResource(Res.string.assignment_submission_submit_action)
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canSubmit) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }
                }
                    }
                }
            }
        }
        }
    } else {
        // Material Design 3
        if (!isAlreadySubmitted && !canSubmit) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(Res.string.assignment_submission_locked_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    val lockedMessage = when {
                        formattedLockDate != null && isHardLocked -> {
                            stringResource(Res.string.assignment_submission_locked_by_date, formattedLockDate)
                        }
                        !assignment.supportsDirectSubmission -> {
                            stringResource(Res.string.assignment_submission_no_online_submissions)
                        }
                        else -> {
                            stringResource(Res.string.assignment_submission_locked_generic)
                        }
                    }
                    Text(
                        text = lockedMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = modifier.fillMaxWidth()
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
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(Res.string.assignment_submission_card_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (isAlreadySubmitted) {
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = stringResource(Res.string.assignment_submission_status_submitted),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        } else if (isPastDue) {
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = stringResource(Res.string.assignment_submission_late_allowed_badge),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                if (isAlreadySubmitted) {
                    if (scorePair != null) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(Res.string.assignment_submission_score_label),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(
                                        Res.string.assignment_quiz_score_format,
                                        scorePair.first,
                                        scorePair.second
                                    ),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(Res.string.assignment_submission_pending_grade),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (submittedAttachments.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            submittedAttachments.forEach { attachment ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.InsertDriveFile,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = attachment.displayName ?: attachment.filename ?: stringResource(Res.string.assignment_submission_submitted_file_title),
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    if (attachment.size != null && attachment.size > 0L) {
                                                        Text(
                                                            text = formatFileSize(attachment.size),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    if (formattedSubmittedAt != null) {
                                                        Text(
                                                            text = "• " + stringResource(Res.string.assignment_submission_submitted_at_prefix, formattedSubmittedAt),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    val targetUrl = resolveViewableUrl(attachment, assignment.htmlUrl)
                                                    targetUrl?.let { openUrlSafely(it, uriHandler) }
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = stringResource(Res.string.assignment_submission_view_file),
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    attachment.url?.let { openUrlSafely(it, uriHandler) }
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = stringResource(Res.string.assignment_submission_download_file),
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (!submittedTextBody.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = stringResource(Res.string.assignment_submission_submitted_text_title),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = stripHtmlTags(submittedTextBody),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (formattedSubmittedAt != null) {
                                    Text(
                                        text = stringResource(Res.string.assignment_submission_submitted_at_prefix, formattedSubmittedAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else if (!submittedUrl.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = stringResource(Res.string.assignment_submission_submitted_url_title),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = submittedUrl,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Button(
                                    onClick = { openUrlSafely(submittedUrl, uriHandler) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = stringResource(Res.string.assignment_submission_view_file),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (allowedAttempts != null && allowedAttempts > 0) {
                                stringResource(Res.string.assignment_submission_attempt_indicator, currentAttempt, allowedAttempts)
                            } else {
                                stringResource(Res.string.assignment_submission_attempt_unlimited, currentAttempt)
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (canSubmit) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (showDeliveryOptions) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDeliveryOptions = !showDeliveryOptions }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 11.dp, horizontal = 14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (showDeliveryOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = if (showDeliveryOptions) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = stringResource(
                                        if (showDeliveryOptions) Res.string.assignment_submission_toggle_hide_options
                                        else Res.string.assignment_submission_toggle_show_options
                                    ),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (showDeliveryOptions) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isHardLocked) Icons.Default.Lock else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isHardLocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                val noticeText = when {
                                    formattedLockDate != null && isHardLocked -> stringResource(Res.string.assignment_submission_locked_by_date, formattedLockDate)
                                    isHardLocked -> stringResource(Res.string.assignment_submission_locked_generic)
                                    !hasMoreAttempts -> stringResource(Res.string.assignment_submission_no_more_attempts)
                                    else -> stringResource(Res.string.assignment_submission_locked_generic)
                                }
                                Text(
                                    text = noticeText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showDeliveryOptions && canSubmit,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Selector de pestañas Material: [ Archivo local ] [ Borrador Drive ] [ Texto ]
                        if (availableTabs.size > 1) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (supportsUpload) {
                                        // Tab 1: Archivo
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (selectedTab == SubmissionTab.UPLOAD) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerHigh,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedTab = SubmissionTab.UPLOAD }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AttachFile,
                                                    contentDescription = null,
                                                    tint = if (selectedTab == SubmissionTab.UPLOAD) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = stringResource(Res.string.assignment_submission_tab_upload),
                                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = if (selectedTab == SubmissionTab.UPLOAD) FontWeight.Bold else FontWeight.Medium),
                                                    color = if (selectedTab == SubmissionTab.UPLOAD) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    if (isDriveConnected) {
                                        // Tab 2: Borrador Drive
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (selectedTab == SubmissionTab.DRIVE) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerHigh,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedTab = SubmissionTab.DRIVE }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CloudSync,
                                                    contentDescription = null,
                                                    tint = if (selectedTab == SubmissionTab.DRIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = stringResource(Res.string.assignment_submission_tab_drive),
                                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = if (selectedTab == SubmissionTab.DRIVE) FontWeight.Bold else FontWeight.Medium),
                                                    color = if (selectedTab == SubmissionTab.DRIVE) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    // Tab 3: Texto (si aplica)
                                    if (supportsText) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (selectedTab == SubmissionTab.TEXT) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerHigh,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedTab = SubmissionTab.TEXT }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.EditNote,
                                                    contentDescription = null,
                                                    tint = if (selectedTab == SubmissionTab.TEXT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = stringResource(Res.string.assignment_submission_tab_text),
                                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = if (selectedTab == SubmissionTab.TEXT) FontWeight.Bold else FontWeight.Medium),
                                                    color = if (selectedTab == SubmissionTab.TEXT) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                // CONTENIDO MATERIAL SEGÚN PESTAÑA
                when (selectedTab) {
                    SubmissionTab.UPLOAD -> {
                        if (selectedDocument == null) {
                            val materialDropBorderColor = if (isDraggingOver) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            }
                            val materialDropContainerColor = if (isDraggingOver) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = materialDropContainerColor,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isDraggingOver) 2.dp else 1.dp,
                                    color = materialDropBorderColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fileDropTarget(
                                        enabled = true,
                                        allowedExtensions = effectiveAllowedExtensions.ifEmpty { null },
                                        onDragStateChange = { isDraggingOver = it },
                                        onDocumentDropped = { onDocumentLoaded(it) }
                                    )
                                    .clickable { documentPicker.launchDocumentPicker(effectiveAllowedExtensions.ifEmpty { null }) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.UploadFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = if (isDraggingOver) {
                                            stringResource(Res.string.assignment_submission_drop_here)
                                        } else {
                                            stringResource(Res.string.assignment_submission_pick_file)
                                        },
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (isDraggingOver) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!isDraggingOver) {
                                        Text(
                                            text = stringResource(Res.string.assignment_submission_drag_drop_hint),
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (effectiveAllowedExtensions.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(9999.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = stringResource(
                                                    Res.string.assignment_submission_required_format,
                                                    effectiveAllowedExtensions.joinToString(", ") { it.uppercase() }
                                                ),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = stringResource(Res.string.assignment_submission_formats_hint),
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = selectedDocument?.name.orEmpty(),
                                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = formatFileSize(selectedDocument?.sizeBytes ?: 0L),
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { documentPicker.launchDocumentPicker(effectiveAllowedExtensions.ifEmpty { null }) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = stringResource(Res.string.assignment_submission_change_file),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { selectedDocument = null },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(Res.string.assignment_submission_remove_file),
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SubmissionTab.DRIVE -> {
                        if (isDriveConnected) {
                            // Sección integrada de Google Drive en Material
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
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
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isDraftActive) Icons.Default.CloudSync else Icons.Default.InsertDriveFile,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = currentDraftFileName,
                                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                                            ) {
                                                Text(
                                                    text = if (currentDraftFileName.endsWith(".docx", ignoreCase = true)) "DOCX" else "GDOC",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = if (isSyncingNow) {
                                                stringResource(Res.string.draft_syncing_cloud_status)
                                            } else if (isDraftActive) {
                                                "Borrador sincronizado Google Drive"
                                            } else if (detectedTemplateName != null) {
                                                "Plantilla oficial Canvas LMS"
                                            } else {
                                                "Borrador nuevo en Google Drive"
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (isSyncingNow) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = stringResource(Res.string.draft_syncing_cloud_status),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            } else if (!isDraftActive) {
                                Button(
                                    onClick = { onStartDraft(detectedTemplateName != null, detectedTemplateName) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (detectedTemplateName != null) {
                                            "Iniciar copia de: ${detectedTemplateName.take(22)}..."
                                        } else {
                                            "Crear borrador en Google Drive"
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                OutlinedButton(
                                    onClick = onExploreTemplates,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Buscar plantilla en módulos del curso")
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onOpenDocs(draftInfo?.driveUrl.orEmpty()) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Abrir en Docs")
                                    }

                                    OutlinedButton(
                                        onClick = onCancelDraft,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Cancelar borrador",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }

                    SubmissionTab.TEXT -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicTextField(
                                value = textBody,
                                onValueChange = {
                                    textBody = it
                                    errorMessage = null
                                },
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(14.dp),
                                decorationBox = { innerTextField ->
                                    if (textBody.isEmpty()) {
                                        Text(
                                            text = stringResource(Res.string.assignment_submission_text_hint),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                // Sección común de comentarios para el docente
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        BasicTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (commentText.isEmpty()) {
                                    Text(
                                        text = stringResource(Res.string.assignment_submission_comment_hint),
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                // Mensajes de error y éxito
                AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                    errorMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = successMessage != null, enter = fadeIn(), exit = fadeOut()) {
                    successMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Botón principal de Entrega
                val canSubmit = when (selectedTab) {
                    SubmissionTab.UPLOAD -> selectedDocument != null
                    SubmissionTab.DRIVE -> isDriveConnected && isDraftActive
                    SubmissionTab.TEXT -> textBody.isNotBlank()
                }

                val hasContentToReview = when (selectedTab) {
                    SubmissionTab.UPLOAD -> selectedDocument != null
                    SubmissionTab.DRIVE -> isDriveConnected && isDraftActive && !draftInfo?.driveFileId.isNullOrBlank()
                    SubmissionTab.TEXT -> textBody.isNotBlank()
                }

                // Botón animado de "Revisar con IA"
                AnimatedVisibility(
                    visible = canSubmit && !isSubmitting && hasContentToReview,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    OutlinedButton(
                        onClick = { showAiReviewSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(Res.string.review_with_ai_button),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = stringResource(Res.string.review_with_ai_subtitle),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (selectedTab == SubmissionTab.DRIVE && isDriveConnected) {
                            onSubmitDraftToCanvas()
                        } else {
                            handleSubmitDirect()
                        }
                    },
                    enabled = canSubmit && !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.assignment_submission_submitting),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (selectedTab == SubmissionTab.DRIVE && isDriveConnected) {
                                if (isDraftActive) "Entregar borrador a Canvas" else "Crear borrador primero"
                            } else {
                                if (isAlreadySubmitted) stringResource(Res.string.assignment_submission_resubmit_action) else stringResource(Res.string.assignment_submission_submit_action)
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
        }
    }
}

    val draftFileId = if (selectedTab == SubmissionTab.DRIVE && isDriveConnected && isDraftActive) {
        draftInfo?.driveFileId
    } else null

    AssignmentPreReviewSheet(
        show = showAiReviewSheet,
        assignment = assignment,
        selectedDocument = if (selectedTab == SubmissionTab.UPLOAD) selectedDocument else null,
        draftFileId = draftFileId,
        textBody = if (selectedTab == SubmissionTab.TEXT) textBody else null,
        themeStyle = themeStyle,
        onDismissRequest = { showAiReviewSheet = false }
    )
}

private fun resolveViewableUrl(
    attachment: me.joxquin.notivas.data.model.CanvasAttachment,
    assignmentHtmlUrl: String?
): String? {
    val downloadUrl = attachment.url
    if (!downloadUrl.isNullOrBlank()) {
        val name = (attachment.displayName ?: attachment.filename ?: "").lowercase()
        val mime = (attachment.contentType ?: "").lowercase()
        val isDoc = name.endsWith(".pdf") || name.endsWith(".docx") || name.endsWith(".doc") ||
                name.endsWith(".pptx") || name.endsWith(".ppt") || name.endsWith(".xlsx") ||
                name.endsWith(".xls") || mime.contains("pdf") || mime.contains("officedocument") ||
                mime.contains("msword") || mime.contains("powerpoint") || mime.contains("excel")
        return if (isDoc) {
            "https://docs.google.com/viewer?url=" + encodeUrlComponent(downloadUrl)
        } else {
            downloadUrl
        }
    }

    val preview = attachment.previewUrl
    if (!preview.isNullOrBlank() && !preview.startsWith("/api/")) {
        if (preview.startsWith("http://") || preview.startsWith("https://")) {
            return preview
        }
        val base = assignmentHtmlUrl?.let { Regex("""^(https?://[^/]+)""").find(it)?.value } ?: ""
        return "$base${if (preview.startsWith("/")) "" else "/"}$preview"
    }

    return assignmentHtmlUrl
}


private fun openUrlSafely(url: String, uriHandler: androidx.compose.ui.platform.UriHandler) {
    try {
        uriHandler.openUri(url)
    } catch (_: Exception) {
        me.joxquin.notivas.util.CanvasDeepLinkHandler.openCanvasUrl(url)
    }
}

private fun stripHtmlTags(html: String): String {
    return html.replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> "${(mb * 10).toInt() / 10.0} MB"
        kb >= 1.0 -> "${(kb * 10).toInt() / 10.0} KB"
        else -> "$bytes B"
    }
}

internal fun detectExtensionsFromHtml(description: String?): List<String> {
    if (description.isNullOrBlank()) return emptyList()
    val hash = description.hashCode()
    return EXTENSIONS_CACHE.getOrPut(hash) {
        val lower = description.lowercase()
        val results = mutableSetOf<String>()

        val formatMatch = FORMAT_SECTION_REGEX.find(lower)
        if (formatMatch != null) {
            val raw = formatMatch.groupValues[1]
            for ((ext, regex) in EXTENSION_WORD_REGEXES) {
                if (regex.containsMatchIn(raw)) {
                    results.add(ext)
                }
            }
        }
        if (results.isEmpty()) {
            if (lower.contains("formato pdf") || lower.contains("archivo pdf") || lower.contains("documento en pdf") || lower.contains("documento en formato pdf")) {
                results.add("pdf")
            }
        }
        results.toList()
    }
}
