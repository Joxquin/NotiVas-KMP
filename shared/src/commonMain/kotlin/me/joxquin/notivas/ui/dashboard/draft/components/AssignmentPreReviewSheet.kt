package me.joxquin.notivas.ui.dashboard.draft.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.di.AppModule
import me.joxquin.notivas.domain.review.AssignmentPreReviewReport
import me.joxquin.notivas.domain.review.CriterionEvaluation
import me.joxquin.notivas.domain.review.ReviewMode
import me.joxquin.notivas.domain.review.ReviewOptions
import me.joxquin.notivas.domain.review.ReviewProgressStep
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.aura.monetAuraBackground
import me.joxquin.notivas.util.SelectedDocument
import me.joxquin.notivas.util.rememberDocumentPicker
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PagerNavigationSpringSpec
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.utils.springAnimateToPage
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
    val scope = rememberCoroutineScope()
    var options by remember { mutableStateOf(ReviewOptions()) }
    var isLoading by remember { mutableStateOf(false) }
    var progressStep by remember { mutableStateOf(ReviewProgressStep.IDLE) }
    var progressMessage by remember { mutableStateOf("") }
    var report by remember { mutableStateOf<AssignmentPreReviewReport?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                options = options,
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

    val onNewReview: () -> Unit = {
        report = null
        errorMessage = null
        isLoading = false
    }

    var isSheetAnimationFinished by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (show) {
            isSheetAnimationFinished = false
            delay(420)
            isSheetAnimationFinished = true
        } else {
            isSheetAnimationFinished = false
            report = null
            errorMessage = null
            isLoading = false
        }
    }

    val auraAlpha by animateFloatAsState(
        targetValue = if (isLoading && isSheetAnimationFinished) 0.85f else 0f,
        animationSpec = tween(
            durationMillis = if (isLoading) 600 else 400,
            easing = FastOutSlowInEasing
        ),
        label = "preReviewAuraAlpha"
    )

    val isAuraActive = isLoading || auraAlpha > 0.001f

    if (themeStyle == AppThemeStyle.MIUIX) {
        val sheetBackgroundColor = if (isAuraActive) Color.Transparent else MiuixTheme.colorScheme.surfaceContainer
        val sheetModifier = if (isAuraActive) {
            Modifier
                .squircleClip(
                    topStart = 28.dp,
                    topEnd = 28.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 0.dp
                )
                .monetAuraBackground(
                    alpha = auraAlpha,
                    surfaceColor = MiuixTheme.colorScheme.surfaceContainer
                )
        } else {
            Modifier
        }

        WindowBottomSheet(
            title = stringResource(Res.string.review_sheet_title),
            show = show,
            onDismissRequest = onDismissRequest,
            backgroundColor = sheetBackgroundColor,
            modifier = sheetModifier,
            content = {
                PreReviewSheetContentMiuix(
                    options = options,
                    onOptionsChange = { options = it },
                    isLoading = isLoading,
                    progressStep = progressStep,
                    progressMessage = progressMessage,
                    report = report,
                    errorMessage = errorMessage,
                    onStartReview = runAnalysis,
                    onReanalyze = runAnalysis,
                    onNewReview = onNewReview,
                    onClose = onDismissRequest
                )
            }
        )
    } else {
        if (!show) return
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val sheetBackgroundColor = if (isAuraActive) Color.Transparent else MaterialTheme.colorScheme.surface
        val sheetModifier = if (isAuraActive) {
            Modifier
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .monetAuraBackground(
                    alpha = auraAlpha,
                    surfaceColor = MaterialTheme.colorScheme.surface
                )
        } else {
            Modifier
        }

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = sheetBackgroundColor,
            modifier = sheetModifier
        ) {
            PreReviewSheetContentMaterial(
                options = options,
                onOptionsChange = { options = it },
                isLoading = isLoading,
                progressStep = progressStep,
                progressMessage = progressMessage,
                report = report,
                errorMessage = errorMessage,
                onStartReview = runAnalysis,
                onReanalyze = runAnalysis,
                onNewReview = onNewReview,
                onClose = onDismissRequest
            )
        }
    }
}

private enum class SheetStage {
    CONFIG,
    LOADING,
    ERROR,
    REPORT
}

@Composable
private fun PreReviewSheetContentMiuix(
    options: ReviewOptions,
    onOptionsChange: (ReviewOptions) -> Unit,
    isLoading: Boolean,
    progressStep: ReviewProgressStep,
    progressMessage: String,
    report: AssignmentPreReviewReport?,
    errorMessage: String?,
    onStartReview: () -> Unit,
    onReanalyze: () -> Unit,
    onNewReview: () -> Unit,
    onClose: () -> Unit
) {
    val navInsets = WindowInsets.navigationBars.asPaddingValues()

    val currentStage = when {
        isLoading -> SheetStage.LOADING
        errorMessage != null -> SheetStage.ERROR
        report != null -> SheetStage.REPORT
        else -> SheetStage.CONFIG
    }

    AnimatedContent(
        targetState = currentStage,
        transitionSpec = {
            (fadeIn(animationSpec = tween(320, easing = FastOutSlowInEasing)) +
             scaleIn(initialScale = 0.97f, animationSpec = tween(320, easing = FastOutSlowInEasing)))
                .togetherWith(
                    fadeOut(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                    scaleOut(targetScale = 0.98f, animationSpec = tween(220, easing = FastOutSlowInEasing))
                )
        },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        label = "preReviewContentMiuix"
    ) { stage ->
        when (stage) {
            SheetStage.CONFIG -> {
                PreReviewConfigViewMiuix(
                    options = options,
                    onOptionsChange = onOptionsChange,
                    onStartReview = onStartReview,
                    onClose = onClose
                )
            }
            SheetStage.LOADING -> {
                ScanningProgressViewMiuix(
                    step = progressStep,
                    message = progressMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = navInsets.calculateBottomPadding() + 20.dp)
                )
            }
            SheetStage.ERROR -> {
                ErrorResultViewMiuix(
                    error = errorMessage ?: "",
                    onRetry = onReanalyze,
                    onClose = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = navInsets.calculateBottomPadding() + 20.dp)
                )
            }
            SheetStage.REPORT -> {
                if (report != null) {
                    ReportResultViewMiuix(
                        report = report,
                        onReanalyze = onReanalyze,
                        onNewReview = onNewReview,
                        onClose = onClose
                    )
                }
            }
        }
    }
}

@Composable
private fun PreReviewConfigViewMiuix(
    options: ReviewOptions,
    onOptionsChange: (ReviewOptions) -> Unit,
    onStartReview: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navInsets = WindowInsets.navigationBars.asPaddingValues()
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val displayCutoutPadding = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    val safeTopInset = remember(statusBarPadding, displayCutoutPadding) {
        maxOf(statusBarPadding, displayCutoutPadding)
    }

    val maxAvailableContentHeight = remember(windowInfo.containerDpSize.height, safeTopInset) {
        (windowInfo.containerDpSize.height - safeTopInset - 72.dp).coerceAtLeast(320.dp)
    }

    var headerHeightDp by remember { mutableStateOf(70.dp) }
    var footerHeightDp by remember { mutableStateOf(70.dp) }

    val dynamicPagerHeight = remember(maxAvailableContentHeight, headerHeightDp, footerHeightDp) {
        (maxAvailableContentHeight - headerHeightDp - footerHeightDp - 20.dp).coerceIn(180.dp, 400.dp)
    }

    val tabLabels = listOf(
        stringResource(Res.string.review_tab_mode_quick),
        stringResource(Res.string.review_tab_mode_exhaustive),
        stringResource(Res.string.review_tab_mode_custom)
    )

    val scope = rememberCoroutineScope()
    val initialPage = when (options.mode) {
        ReviewMode.QUICK -> 0
        ReviewMode.EXHAUSTIVE -> 1
        ReviewMode.CUSTOM -> 2
    }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { tabLabels.size })
    val contourTabListState = rememberLazyListState()
    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = PagerNavigationSpringSpec
    )
    val dismissState = LocalDismissState.current
    val referencePicker = rememberDocumentPicker { doc ->
        onOptionsChange(options.copy(referenceDocument = doc))
    }

    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            0 -> {
                if (options.mode != ReviewMode.QUICK) {
                    onOptionsChange(
                        options.copy(
                            mode = ReviewMode.QUICK,
                            checkInstructions = true,
                            checkRubric = true,
                            checkModules = true,
                            deepModuleScan = false,
                            inspectImages = false
                        )
                    )
                }
            }
            1 -> {
                if (options.mode != ReviewMode.EXHAUSTIVE) {
                    onOptionsChange(
                        options.copy(
                            mode = ReviewMode.EXHAUSTIVE,
                            checkInstructions = true,
                            checkRubric = true,
                            checkModules = true,
                            deepModuleScan = true,
                            inspectImages = true
                        )
                    )
                }
            }
            2 -> {
                if (options.mode != ReviewMode.CUSTOM) {
                    onOptionsChange(options.copy(mode = ReviewMode.CUSTOM))
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Encabezado con selector TabRowWithContour
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    headerHeightDp = with(density) { coordinates.size.height.toDp() }
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabRowWithContour(
                tabs = tabLabels,
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch {
                        pagerState.springAnimateToPage(index)
                    }
                },
                listState = contourTabListState,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Pager deslizable con altura acotada dinámica
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(dynamicPagerHeight),
            userScrollEnabled = true,
            flingBehavior = flingBehavior,
            key = { it },
            pageContent = { page ->
                when (page) {
                    0 -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .scrollEndHaptic()
                                .overScrollVertical(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            item(key = "quick_info") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                                    insideMargin = PaddingValues(16.dp),
                                    pressFeedbackType = PressFeedbackType.None
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        MiuixText(
                                            text = stringResource(Res.string.review_mode_quick_title),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MiuixTheme.colorScheme.onSurface
                                        )
                                        MiuixText(
                                            text = stringResource(Res.string.review_mode_quick_desc),
                                            fontSize = 13.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        BulletItemMiuix(
                                            text = stringResource(Res.string.review_opt_instructions_title),
                                            icon = Icons.Default.CheckCircle,
                                            iconTint = MiuixTheme.colorScheme.primary
                                        )
                                        BulletItemMiuix(
                                            text = stringResource(Res.string.review_opt_rubric_title),
                                            icon = Icons.Default.CheckCircle,
                                            iconTint = MiuixTheme.colorScheme.primary
                                        )
                                        BulletItemMiuix(
                                            text = stringResource(Res.string.review_opt_modules_title),
                                            icon = Icons.Default.CheckCircle,
                                            iconTint = MiuixTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .scrollEndHaptic()
                                .overScrollVertical(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            item(key = "exhaustive_info") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                                    insideMargin = PaddingValues(16.dp),
                                    pressFeedbackType = PressFeedbackType.None
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        MiuixText(
                                            text = stringResource(Res.string.review_mode_exhaustive_title),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MiuixTheme.colorScheme.onSurface
                                        )
                                        MiuixText(
                                            text = stringResource(Res.string.review_mode_exhaustive_desc),
                                            fontSize = 13.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        BulletItemMiuix(
                                            text = stringResource(Res.string.review_opt_instructions_title),
                                            icon = Icons.Default.CheckCircle,
                                            iconTint = MiuixTheme.colorScheme.primary
                                        )
                                        BulletItemMiuix(
                                            text = stringResource(Res.string.review_opt_rubric_title),
                                            icon = Icons.Default.CheckCircle,
                                            iconTint = MiuixTheme.colorScheme.primary
                                        )
                                        BulletItemMiuix(
                                            text = stringResource(Res.string.review_opt_deep_modules_title),
                                            icon = Icons.Default.CheckCircle,
                                            iconTint = MiuixTheme.colorScheme.primary
                                        )
                                        BulletItemMiuix(
                                            text = stringResource(Res.string.review_opt_images_title),
                                            icon = Icons.Default.CheckCircle,
                                            iconTint = MiuixTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .scrollEndHaptic()
                                .overScrollVertical(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            item(key = "custom_checkboxes") {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateContentSize(
                                            animationSpec = spring(
                                                dampingRatio = 0.85f,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        ),
                                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                                    insideMargin = PaddingValues(12.dp)
                                ) {
                                    CheckboxPreference(
                                        title = stringResource(Res.string.review_opt_instructions_title),
                                        summary = stringResource(Res.string.review_opt_instructions_desc),
                                        checked = options.checkInstructions,
                                        onCheckedChange = { onOptionsChange(options.copy(checkInstructions = it)) }
                                    )
                                    CheckboxPreference(
                                        title = stringResource(Res.string.review_opt_rubric_title),
                                        summary = stringResource(Res.string.review_opt_rubric_desc),
                                        checked = options.checkRubric,
                                        onCheckedChange = { onOptionsChange(options.copy(checkRubric = it)) }
                                    )
                                    CheckboxPreference(
                                        title = stringResource(Res.string.review_opt_modules_title),
                                        summary = stringResource(Res.string.review_opt_modules_desc),
                                        checked = options.checkModules,
                                        onCheckedChange = { checked ->
                                            onOptionsChange(
                                                options.copy(
                                                    checkModules = checked,
                                                    deepModuleScan = if (!checked) false else options.deepModuleScan
                                                )
                                            )
                                        }
                                    )
                                    AnimatedVisibility(
                                        visible = options.checkModules,
                                        enter = expandVertically(
                                            animationSpec = spring(
                                                dampingRatio = 0.85f,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        ) + fadeIn(),
                                        exit = shrinkVertically(
                                            animationSpec = spring(
                                                dampingRatio = 0.85f,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        ) + fadeOut()
                                    ) {
                                        CheckboxPreference(
                                            title = stringResource(Res.string.review_opt_deep_modules_title),
                                            summary = stringResource(Res.string.review_opt_deep_modules_desc),
                                            checked = options.deepModuleScan,
                                            onCheckedChange = { onOptionsChange(options.copy(deepModuleScan = it)) }
                                        )
                                    }
                                    CheckboxPreference(
                                        title = stringResource(Res.string.review_opt_images_title),
                                        summary = stringResource(Res.string.review_opt_images_desc),
                                        checked = options.inspectImages,
                                        onCheckedChange = { onOptionsChange(options.copy(inspectImages = it)) }
                                    )
                                    CheckboxPreference(
                                        title = stringResource(Res.string.review_opt_reference_title),
                                        summary = options.referenceDocument?.name ?: stringResource(Res.string.review_opt_reference_desc),
                                        checked = options.referenceDocument != null,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                referencePicker.launchDocumentPicker()
                                            } else {
                                                onOptionsChange(options.copy(referenceDocument = null))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )

        // Botones inferiores SIEMPRE fijados en la parte inferior
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    footerHeightDp = with(density) { coordinates.size.height.toDp() }
                }
                .padding(bottom = navInsets.calculateBottomPadding() + 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiuixTextButton(
                    text = stringResource(Res.string.review_action_close),
                    onClick = { dismissState?.invoke() ?: onClose() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(
                        color = MiuixTheme.colorScheme.surfaceContainerHigh,
                        textColor = MiuixTheme.colorScheme.onSurface
                    )
                )
                MiuixTextButton(
                    text = stringResource(Res.string.review_action_start),
                    onClick = onStartReview,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
            }
        }
    }
}

@Composable
private fun ScanningProgressViewMiuix(
    step: ReviewProgressStep,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
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
    onReanalyze: () -> Unit,
    onNewReview: () -> Unit,
    onClose: () -> Unit
) {
    val dismissState = LocalDismissState.current
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val displayCutoutPadding = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    val safeTopInset = remember(statusBarPadding, displayCutoutPadding) {
        maxOf(statusBarPadding, displayCutoutPadding)
    }
    val navInsets = WindowInsets.navigationBars.asPaddingValues()

    // Espacio vertical máximo disponible para el contenido dentro de WindowBottomSheet
    val maxAvailableContentHeight = remember(windowInfo.containerDpSize.height, safeTopInset) {
        windowInfo.containerDpSize.height - safeTopInset - 72.dp
    }

    var headerHeightDp by remember { mutableStateOf(240.dp) }
    var footerHeightDp by remember { mutableStateOf(90.dp) }

    // Altura calculada dinámicamente para que los botones inferiores SIEMPRE sean visibles
    val dynamicPagerHeight = remember(maxAvailableContentHeight, headerHeightDp, footerHeightDp) {
        (maxAvailableContentHeight - headerHeightDp - footerHeightDp - 20.dp).coerceIn(160.dp, 420.dp)
    }

    val tabLabels = listOf(
        stringResource(Res.string.review_tab_rubric),
        stringResource(Res.string.review_tab_improvements),
        stringResource(Res.string.review_tab_module)
    )

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { tabLabels.size })
    val contourTabListState = rememberLazyListState()
    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = PagerNavigationSpringSpec
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Encabezado medido dinámicamente (Puntaje, Resumen, Pestañas)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    headerHeightDp = with(density) { coordinates.size.height.toDp() }
                },
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tarjeta de Puntaje Estimado
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                insideMargin = PaddingValues(16.dp),
                pressFeedbackType = PressFeedbackType.None
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

            // Selector oficial TabRowWithContour de compose-miuix-ui
            TabRowWithContour(
                tabs = tabLabels,
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch {
                        pagerState.springAnimateToPage(index)
                    }
                },
                listState = contourTabListState,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Contenedor deslizable sincronizado con el TabRow con altura dinámica
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(dynamicPagerHeight),
            userScrollEnabled = true,
            flingBehavior = flingBehavior,
            key = { it },
            pageContent = { page ->
                when (page) {
                    0 -> {
                        if (report.criteriaEvaluations.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scrollEndHaptic()
                                    .overScrollVertical(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                items(
                                    items = report.criteriaEvaluations,
                                    key = { it.title }
                                ) { criterion ->
                                    CriterionCardMiuix(criterion)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scrollEndHaptic()
                                    .overScrollVertical(),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                item(key = "raw_markdown") {
                                    MiuixText(
                                        text = report.rawMarkdown.take(800),
                                        fontSize = 12.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .scrollEndHaptic()
                                .overScrollVertical(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            if (report.strengths.isNotEmpty()) {
                                item(key = "strengths_header") {
                                    SmallTitle(text = stringResource(Res.string.review_strengths_title))
                                }
                                items(report.strengths) { str ->
                                    BulletItemMiuix(
                                        text = str,
                                        icon = Icons.Default.CheckCircle,
                                        iconTint = MiuixTheme.colorScheme.primary
                                    )
                                }
                            }
                            if (report.improvements.isNotEmpty()) {
                                item(key = "improvements_header") {
                                    SmallTitle(text = stringResource(Res.string.review_improvements_title))
                                }
                                items(report.improvements) { imp ->
                                    BulletItemMiuix(
                                        text = imp,
                                        icon = Icons.Default.Lightbulb,
                                        iconTint = MiuixTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .scrollEndHaptic()
                                .overScrollVertical(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            item(key = "module_header") {
                                SmallTitle(text = stringResource(Res.string.review_module_alignment_title))
                            }
                            if (report.moduleAlignment.isNotEmpty()) {
                                items(report.moduleAlignment) { item ->
                                    BulletItemMiuix(
                                        text = item,
                                        icon = Icons.Default.MenuBook,
                                        iconTint = MiuixTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                item(key = "module_empty") {
                                    MiuixText(
                                        text = "El trabajo se encuentra alineado con la temática del curso.",
                                        fontSize = 12.sp,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )

        // Pie medido dinámicamente: botones de acción inferiores y espaciador seguro
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    footerHeightDp = with(density) { coordinates.size.height.toDp() }
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiuixTextButton(
                    text = stringResource(Res.string.review_action_new_review),
                    onClick = onNewReview,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(
                        color = MiuixTheme.colorScheme.surfaceContainerHigh,
                        textColor = MiuixTheme.colorScheme.onSurface
                    )
                )
                MiuixTextButton(
                    text = stringResource(Res.string.review_action_close),
                    onClick = { dismissState?.invoke() ?: onClose() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
            }

            // Espaciador para la barra de navegación
            Spacer(modifier = Modifier.height(navInsets.calculateBottomPadding() + 8.dp))
        }
    }
}

@Composable
private fun CriterionCardMiuix(criterion: CriterionEvaluation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        insideMargin = PaddingValues(14.dp),
        pressFeedbackType = PressFeedbackType.None
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
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = LocalDismissState.current
    Column(
        modifier = modifier
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
                onClick = { dismissState?.invoke() ?: onClose() },
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
    options: ReviewOptions,
    onOptionsChange: (ReviewOptions) -> Unit,
    isLoading: Boolean,
    progressStep: ReviewProgressStep,
    progressMessage: String,
    report: AssignmentPreReviewReport?,
    errorMessage: String?,
    onStartReview: () -> Unit,
    onReanalyze: () -> Unit,
    onNewReview: () -> Unit,
    onClose: () -> Unit
) {
    val navInsets = WindowInsets.navigationBars.asPaddingValues()

    val currentStage = when {
        isLoading -> SheetStage.LOADING
        errorMessage != null -> SheetStage.ERROR
        report != null -> SheetStage.REPORT
        else -> SheetStage.CONFIG
    }

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

        AnimatedContent(
            targetState = currentStage,
            transitionSpec = {
                (fadeIn(animationSpec = tween(320, easing = FastOutSlowInEasing)) +
                 scaleIn(initialScale = 0.97f, animationSpec = tween(320, easing = FastOutSlowInEasing)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 0.98f, animationSpec = tween(220, easing = FastOutSlowInEasing))
                    )
            },
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
            label = "preReviewContentMaterial"
        ) { stage ->
            when (stage) {
                SheetStage.CONFIG -> {
                    PreReviewConfigViewMaterial(
                        options = options,
                        onOptionsChange = onOptionsChange,
                        onStartReview = onStartReview,
                        onClose = onClose
                    )
                }
                SheetStage.LOADING -> {
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
                }
                SheetStage.ERROR -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
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
                }
                SheetStage.REPORT -> {
                    if (report != null) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item(key = "mat_score_card") {
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
                            }

                            if (report.summary.isNotBlank()) {
                                item(key = "mat_summary") {
                                    Text(
                                        text = report.summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            items(report.criteriaEvaluations, key = { it.title }) { criterion ->
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

                            item(key = "mat_actions") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        onClick = onNewReview,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(stringResource(Res.string.review_action_new_review))
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
                }
            }
        }
    }
}

@Composable
private fun PreReviewConfigViewMaterial(
    options: ReviewOptions,
    onOptionsChange: (ReviewOptions) -> Unit,
    onStartReview: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navInsets = WindowInsets.navigationBars.asPaddingValues()
    val referencePicker = rememberDocumentPicker { doc ->
        onOptionsChange(options.copy(referenceDocument = doc))
    }
    val tabLabels = listOf(
        stringResource(Res.string.review_tab_mode_quick),
        stringResource(Res.string.review_tab_mode_exhaustive),
        stringResource(Res.string.review_tab_mode_custom)
    )
    val scope = rememberCoroutineScope()
    val initialPage = when (options.mode) {
        ReviewMode.QUICK -> 0
        ReviewMode.EXHAUSTIVE -> 1
        ReviewMode.CUSTOM -> 2
    }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { tabLabels.size })

    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            0 -> {
                if (options.mode != ReviewMode.QUICK) {
                    onOptionsChange(
                        options.copy(
                            mode = ReviewMode.QUICK,
                            checkInstructions = true,
                            checkRubric = true,
                            checkModules = true,
                            deepModuleScan = false,
                            inspectImages = false
                        )
                    )
                }
            }
            1 -> {
                if (options.mode != ReviewMode.EXHAUSTIVE) {
                    onOptionsChange(
                        options.copy(
                            mode = ReviewMode.EXHAUSTIVE,
                            checkInstructions = true,
                            checkRubric = true,
                            checkModules = true,
                            deepModuleScan = true,
                            inspectImages = true
                        )
                    )
                }
            }
            2 -> {
                if (options.mode != ReviewMode.CUSTOM) {
                    onOptionsChange(options.copy(mode = ReviewMode.CUSTOM))
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabLabels.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            key = { it }
        ) { page ->
            when (page) {
                0 -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.review_mode_quick_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = stringResource(Res.string.review_mode_quick_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                1 -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.review_mode_exhaustive_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = stringResource(Res.string.review_mode_exhaustive_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                2 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item(key = "mat_custom_opts") {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = options.checkInstructions,
                                            onCheckedChange = { onOptionsChange(options.copy(checkInstructions = it)) }
                                        )
                                        Text(stringResource(Res.string.review_opt_instructions_title), style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = options.checkRubric,
                                            onCheckedChange = { onOptionsChange(options.copy(checkRubric = it)) }
                                        )
                                        Text(stringResource(Res.string.review_opt_rubric_title), style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = options.checkModules,
                                            onCheckedChange = { checked ->
                                                onOptionsChange(
                                                    options.copy(
                                                        checkModules = checked,
                                                        deepModuleScan = if (!checked) false else options.deepModuleScan
                                                    )
                                                )
                                            }
                                        )
                                        Text(stringResource(Res.string.review_opt_modules_title), style = MaterialTheme.typography.bodyMedium)
                                    }
                                    AnimatedVisibility(
                                        visible = options.checkModules,
                                        enter = expandVertically(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                                        exit = shrinkVertically(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) + fadeOut()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(start = 24.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = options.deepModuleScan,
                                                onCheckedChange = { onOptionsChange(options.copy(deepModuleScan = it)) }
                                            )
                                            Text(stringResource(Res.string.review_opt_deep_modules_title), style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = options.inspectImages,
                                            onCheckedChange = { onOptionsChange(options.copy(inspectImages = it)) }
                                        )
                                        Text(stringResource(Res.string.review_opt_images_title), style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = options.referenceDocument != null,
                                            onCheckedChange = { checked ->
                                                if (checked) referencePicker.launchDocumentPicker()
                                                else onOptionsChange(options.copy(referenceDocument = null))
                                            }
                                        )
                                        Text(options.referenceDocument?.name ?: stringResource(Res.string.review_opt_reference_title), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Acciones inferiores fijas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = navInsets.calculateBottomPadding() + 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(Res.string.review_action_close))
            }
            androidx.compose.material3.Button(
                onClick = onStartReview,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(Res.string.review_action_start))
            }
        }
    }
}
