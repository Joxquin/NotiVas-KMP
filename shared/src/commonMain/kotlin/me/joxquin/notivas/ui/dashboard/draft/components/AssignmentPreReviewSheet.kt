package me.joxquin.notivas.ui.dashboard.draft.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.di.AppModule
import me.joxquin.notivas.domain.review.AssignmentPreReviewReport
import me.joxquin.notivas.domain.review.CriterionEvaluation
import me.joxquin.notivas.domain.review.ReviewProgressStep
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.util.SelectedDocument
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentPreReviewSheet(
    show: Boolean,
    assignment: Assignment,
    selectedDocument: SelectedDocument? = null,
    draftFileId: String? = null,
    textBody: String? = null,
    themeStyle: AppThemeStyle = AppThemeStyle.MIUIX,
    onDismissRequest: () -> Unit
) {
    if (!show) return

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var progressStep by remember { mutableStateOf(ReviewProgressStep.IDLE) }
    var progressMessage by remember { mutableStateOf("") }
    var report by remember { mutableStateOf<AssignmentPreReviewReport?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Rúbrica, 1: Sugerencias, 2: Módulo

    val runAnalysis: () -> Unit = {
        isLoading = true
        errorMessage = null
        progressStep = ReviewProgressStep.READING_RUBRIC
        scope.launch {
            val result = AppModule.assignmentReviewUseCase.reviewAssignment(
                assignment = assignment,
                selectedDocument = selectedDocument,
                draftFileId = draftFileId,
                textBody = textBody,
                onProgress = { step, msg ->
                    progressStep = step
                    progressMessage = msg
                }
            )
            result.onSuccess {
                report = it
                isLoading = false
            }.onFailure {
                errorMessage = it.message ?: "Error al analizar la entrega"
                isLoading = false
            }
        }
    }

    LaunchedEffect(show) {
        if (show && report == null) {
            runAnalysis()
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        WindowBottomSheet(
            title = stringResource(Res.string.review_sheet_title),
            show = show,
            onDismissRequest = onDismissRequest,
            content = {
                PreReviewSheetContentMiuix(
                    isLoading = isLoading,
                    progressStep = progressStep,
                    progressMessage = progressMessage,
                    report = report,
                    errorMessage = errorMessage,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onReanalyze = runAnalysis,
                    onClose = onDismissRequest
                )
            }
        )
    } else {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            PreReviewSheetContentMaterial(
                isLoading = isLoading,
                progressStep = progressStep,
                progressMessage = progressMessage,
                report = report,
                errorMessage = errorMessage,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onReanalyze = runAnalysis,
                onClose = onDismissRequest
            )
        }
    }
}

@Composable
private fun PreReviewSheetContentMiuix(
    isLoading: Boolean,
    progressStep: ReviewProgressStep,
    progressMessage: String,
    report: AssignmentPreReviewReport?,
    errorMessage: String?,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onReanalyze: () -> Unit,
    onClose: () -> Unit
) {
    val navInsets = WindowInsets.navigationBars.asPaddingValues()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = navInsets.calculateBottomPadding() + 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isLoading) {
            ScanningProgressViewMiuix(
                step = progressStep,
                message = progressMessage
            )
        } else if (errorMessage != null) {
            ErrorResultViewMiuix(
                error = errorMessage,
                onRetry = onReanalyze,
                onClose = onClose
            )
        } else if (report != null) {
            ReportResultViewMiuix(
                report = report,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                onReanalyze = onReanalyze,
                onClose = onClose
            )
        }
    }
}

@Composable
private fun ScanningProgressViewMiuix(
    step: ReviewProgressStep,
    message: String
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(pulseScale)
                .squircleSurface(MiuixTheme.colorScheme.primary.copy(alpha = 0.12f), 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        MiuixText(
            text = stringResource(Res.string.review_scanning_title),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onSurface
        )

        MiuixText(
            text = message.ifBlank { stringResource(Res.string.review_step_evaluating) },
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
        )

        Spacer(Modifier.height(8.dp))

        // Fases de progreso
        ProgressStepRowMiuix(
            label = stringResource(Res.string.review_step_rubric),
            isActive = step == ReviewProgressStep.READING_RUBRIC,
            isCompleted = step.ordinal > ReviewProgressStep.READING_RUBRIC.ordinal
        )
        ProgressStepRowMiuix(
            label = stringResource(Res.string.review_step_modules),
            isActive = step == ReviewProgressStep.FETCHING_MODULES,
            isCompleted = step.ordinal > ReviewProgressStep.FETCHING_MODULES.ordinal
        )
        ProgressStepRowMiuix(
            label = stringResource(Res.string.review_step_document),
            isActive = step == ReviewProgressStep.EXTRACTING_DOCUMENT,
            isCompleted = step.ordinal > ReviewProgressStep.EXTRACTING_DOCUMENT.ordinal
        )
        ProgressStepRowMiuix(
            label = stringResource(Res.string.review_step_evaluating),
            isActive = step == ReviewProgressStep.EVALUATING,
            isCompleted = step.ordinal > ReviewProgressStep.EVALUATING.ordinal
        )
    }
}

@Composable
private fun ProgressStepRowMiuix(
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        } else if (isActive) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = MiuixTheme.colorScheme.primary
            )
        } else {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MiuixTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
        }

        MiuixText(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive || isCompleted) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurfaceVariantSummary
        )
    }
}

@Composable
private fun ReportResultViewMiuix(
    report: AssignmentPreReviewReport,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onReanalyze: () -> Unit,
    onClose: () -> Unit
) {
    // Tarjeta de Puntaje Estimado
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .squircleSurface(MiuixTheme.colorScheme.surfaceContainer, 16.dp)
            .squircleBorder(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.08f), 16.dp)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MiuixText(
                    text = stringResource(Res.string.review_score_estimated),
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                val scoreText = if (report.overallScore != null) {
                    "${report.overallScore} / ${report.maxScore}"
                } else {
                    "${report.percentage}%"
                }
                MiuixText(
                    text = scoreText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .squircleSurface(MiuixTheme.colorScheme.primary.copy(alpha = 0.12f), 12.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                MiuixText(
                    text = "${report.percentage}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.primary
                )
            }
        }
    }

    // Resumen breve
    if (report.summary.isNotBlank()) {
        MiuixText(
            text = report.summary,
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }

    // Selector de pestaña
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .squircleSurface(MiuixTheme.colorScheme.surfaceContainerHigh, 12.dp)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ReviewTabButtonMiuix(
            label = stringResource(Res.string.review_tab_rubric),
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )
        ReviewTabButtonMiuix(
            label = stringResource(Res.string.review_tab_improvements),
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
        ReviewTabButtonMiuix(
            label = stringResource(Res.string.review_tab_module),
            isSelected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            modifier = Modifier.weight(1f)
        )
    }

    // Contenido según pestaña
    when (selectedTab) {
        0 -> {
            if (report.criteriaEvaluations.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    report.criteriaEvaluations.forEach { criterion ->
                        CriterionCardMiuix(criterion)
                    }
                }
            } else {
                MiuixText(
                    text = report.rawMarkdown.take(800),
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }
        1 -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (report.strengths.isNotEmpty()) {
                    SmallTitle(text = stringResource(Res.string.review_strengths_title))
                    report.strengths.forEach { str ->
                        BulletItemMiuix(text = str, icon = Icons.Default.CheckCircle, iconTint = MiuixTheme.colorScheme.primary)
                    }
                }
                if (report.improvements.isNotEmpty()) {
                    SmallTitle(text = stringResource(Res.string.review_improvements_title))
                    report.improvements.forEach { imp ->
                        BulletItemMiuix(text = imp, icon = Icons.Default.Lightbulb, iconTint = MiuixTheme.colorScheme.secondary)
                    }
                }
            }
        }
        2 -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallTitle(text = stringResource(Res.string.review_module_alignment_title))
                if (report.moduleAlignment.isNotEmpty()) {
                    report.moduleAlignment.forEach { item ->
                        BulletItemMiuix(text = item, icon = Icons.Default.MenuBook, iconTint = MiuixTheme.colorScheme.primary)
                    }
                } else {
                    MiuixText(
                        text = "El trabajo se encuentra alineado con la temática del curso.",
                        fontSize = 12.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }
        }
    }

    // Botones de acción inferiores
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MiuixTextButton(
            text = stringResource(Res.string.review_action_reanalyze),
            onClick = onReanalyze,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.textButtonColors(
                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                textColor = MiuixTheme.colorScheme.onSurface
            )
        )
        MiuixTextButton(
            text = stringResource(Res.string.review_action_close),
            onClick = onClose,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.textButtonColorsPrimary()
        )
    }
}

@Composable
private fun ReviewTabButtonMiuix(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .squircleSurface(
                color = if (isSelected) MiuixTheme.colorScheme.surfaceContainer else Color.Transparent,
                cornerRadius = 10.dp
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        MiuixText(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurfaceVariantSummary
        )
    }
}

@Composable
private fun CriterionCardMiuix(criterion: CriterionEvaluation) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .squircleSurface(MiuixTheme.colorScheme.surfaceContainer, 14.dp)
            .squircleBorder(1.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.08f), 14.dp)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiuixText(
                    text = criterion.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .squircleSurface(MiuixTheme.colorScheme.primary.copy(alpha = 0.1f), 8.dp)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    MiuixText(
                        text = "${criterion.pointsEarned} / ${criterion.maxPoints} pts",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.primary
                    )
                }
            }

            if (criterion.level.isNotBlank()) {
                MiuixText(
                    text = criterion.level,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }

            if (criterion.feedback.isNotBlank()) {
                MiuixText(
                    text = criterion.feedback,
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun BulletItemMiuix(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        MiuixText(
            text = text,
            fontSize = 12.sp,
            color = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ErrorResultViewMiuix(
    error: String,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.error,
            modifier = Modifier.size(36.dp)
        )
        MiuixText(
            text = error,
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onErrorContainer
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MiuixTextButton(
                text = stringResource(Res.string.review_action_reanalyze),
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary()
            )
            MiuixTextButton(
                text = stringResource(Res.string.review_action_close),
                onClick = onClose,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColors(
                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                    textColor = MiuixTheme.colorScheme.onSurface
                )
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// MATERIAL 3 IMPLEMENTATION
// -------------------------------------------------------------------------------------------------

@Composable
private fun PreReviewSheetContentMaterial(
    isLoading: Boolean,
    progressStep: ReviewProgressStep,
    progressMessage: String,
    report: AssignmentPreReviewReport?,
    errorMessage: String?,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onReanalyze: () -> Unit,
    onClose: () -> Unit
) {
    val navInsets = WindowInsets.navigationBars.asPaddingValues()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = navInsets.calculateBottomPadding() + 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(Res.string.review_sheet_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(38.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = progressMessage.ifBlank { stringResource(Res.string.review_scanning_title) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onReanalyze) {
                        Text(stringResource(Res.string.review_action_reanalyze))
                    }
                    TextButton(onClick = onClose) {
                        Text(stringResource(Res.string.review_action_close))
                    }
                }
            }
        } else if (report != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.review_score_estimated),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${report.overallScore ?: report.percentage} / ${report.maxScore}",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${report.percentage}%",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (report.summary.isNotBlank()) {
                Text(
                    text = report.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                report.criteriaEvaluations.forEach { criterion ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = criterion.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${criterion.pointsEarned} / ${criterion.maxPoints} pts",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (criterion.feedback.isNotBlank()) {
                                Text(
                                    text = criterion.feedback,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onReanalyze,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.review_action_reanalyze))
                }
                androidx.compose.material3.Button(
                    onClick = onClose,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(Res.string.review_action_close))
                }
            }
        }
    }
}
