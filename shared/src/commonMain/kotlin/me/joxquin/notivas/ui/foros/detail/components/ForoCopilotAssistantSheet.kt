package me.joxquin.notivas.ui.foros.detail.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.CanvasDiscussionTopic
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.RichContentRenderer
import me.joxquin.notivas.ui.components.aura.monetAuraBackground
import me.joxquin.notivas.ui.copilot.components.formatMarkdown
import me.joxquin.notivas.ui.foros.CopilotForoStrategy
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PagerNavigationSpringSpec
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.utils.springAnimateToPage
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassBottomSheetDefaults
import top.yukonga.miuix.kmp.glass.glassBottomSheetSurface
import top.yukonga.miuix.kmp.layout.BottomSheetDefaults
import top.yukonga.miuix.kmp.material.GlassShape
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet

private enum class ForoCopilotProgressStep {
    READING_PROMPT,
    ANALYZING_REPLIES,
    EXTRACTING_OBJECTIVES,
    STRUCTURING_DRAFT
}

private enum class SheetStage {
    LOADING,
    ERROR,
    RESULT
}

private fun cleanModelName(activeModelName: String): String {
    return when {
        activeModelName.contains("gemini-3.5-flash-lite", ignoreCase = true) -> "Gemini 3.5 Flash-Lite"
        activeModelName.contains("gemini-3.5-flash", ignoreCase = true) -> "Gemini 3.5 Flash"
        activeModelName.contains("gemini-3.1-pro", ignoreCase = true) -> "Gemini 3.1 Pro"
        activeModelName.contains("gemini-2.5-flash", ignoreCase = true) -> "Gemini 2.5 Flash"
        activeModelName.contains("gemini-2.5-pro", ignoreCase = true) -> "Gemini 2.5 Pro"
        activeModelName.contains("claude-3-5-sonnet", ignoreCase = true) -> "Claude 3.5 Sonnet"
        activeModelName.contains("deepseek-r1", ignoreCase = true) -> "DeepSeek-R1"
        activeModelName.contains("gpt-4o", ignoreCase = true) -> "GPT-4o"
        else -> activeModelName.substringAfterLast("/")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForoCopilotAssistantSheet(
    show: Boolean = true,
    topic: CanvasDiscussionTopic,
    strategy: CopilotForoStrategy?,
    isGenerating: Boolean,
    errorMessage: String?,
    activeModelName: String,
    onDismiss: () -> Unit,
    onGenerate: (toneModifier: String?) -> Unit,
    onInsertDraft: (String) -> Unit,
    themeStyle: AppThemeStyle,
    backdrop: Backdrop? = null
) {
    var isSheetAnimationFinished by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (show) {
            isSheetAnimationFinished = false
            delay(420)
            isSheetAnimationFinished = true
        } else {
            isSheetAnimationFinished = false
        }
    }

    LaunchedEffect(topic.id, show) {
        if (show && strategy == null && !isGenerating && errorMessage == null) {
            onGenerate(null)
        }
    }

    val isInitialLoading = strategy == null && errorMessage == null
    val auraAlpha by animateFloatAsState(
        targetValue = if ((isGenerating || isInitialLoading) && isSheetAnimationFinished) 0.85f else 0f,
        animationSpec = tween(
            durationMillis = if (isGenerating || isInitialLoading) 600 else 400,
            easing = FastOutSlowInEasing
        ),
        label = "foroCopilotAuraAlpha"
    )

    val isAuraActive = isGenerating || isInitialLoading || auraAlpha > 0.001f

    if (themeStyle == AppThemeStyle.MIUIX) {
        val glassSurface = glassBottomSheetSurface(backdrop)

        OverlayBottomSheet(
            title = stringResource(Res.string.copilot_sheet_title),
            show = show,
            onDismissRequest = onDismiss,
            outsideMargin = BottomSheetDefaults.outsideMargin,
            insideMargin = GlassBottomSheetDefaults.InsideMargin,
            renderInRootScaffold = true,
            dimColor = GlassBottomSheetDefaults.ScrimColor,
            surface = {
                val baseModifier = glassSurface()
                if (isAuraActive) {
                    val auraSurfaceColor = MiuixTheme.colorScheme.surfaceContainer.copy(
                        alpha = (auraAlpha / 0.85f).coerceIn(0f, 1f)
                    )
                    if (isOverscroll) {
                        baseModifier.background(auraSurfaceColor)
                    } else {
                        val shape = remember(cornerRadius) {
                            GlassShape(
                                topStart = cornerRadius,
                                topEnd = cornerRadius,
                                bottomEnd = 0.dp,
                                bottomStart = 0.dp
                            )
                        }
                        baseModifier
                            .clip(shape)
                            .monetAuraBackground(
                                alpha = auraAlpha,
                                surfaceColor = auraSurfaceColor
                            )
                    }
                } else {
                    baseModifier
                }
            },
            content = {
                ForoCopilotContentMiuix(
                    topic = topic,
                    strategy = strategy,
                    isGenerating = isGenerating,
                    errorMessage = errorMessage,
                    activeModelName = activeModelName,
                    onDismiss = onDismiss,
                    onGenerate = onGenerate,
                    onInsertDraft = onInsertDraft
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
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = sheetBackgroundColor,
            modifier = sheetModifier
        ) {
            ForoCopilotContentMaterial(
                topic = topic,
                strategy = strategy,
                isGenerating = isGenerating,
                errorMessage = errorMessage,
                activeModelName = activeModelName,
                onDismiss = onDismiss,
                onGenerate = onGenerate,
                onInsertDraft = onInsertDraft
            )
        }
    }
}

@Composable
private fun ForoCopilotContentMiuix(
    topic: CanvasDiscussionTopic,
    strategy: CopilotForoStrategy?,
    isGenerating: Boolean,
    errorMessage: String?,
    activeModelName: String,
    onDismiss: () -> Unit,
    onGenerate: (toneModifier: String?) -> Unit,
    onInsertDraft: (String) -> Unit
) {
    val navInsets = WindowInsets.navigationBars.asPaddingValues()

    var lastValidStrategy by remember { mutableStateOf(strategy) }
    if (strategy != null) {
        lastValidStrategy = strategy
    }
    val currentStrategy = strategy ?: lastValidStrategy

    var progressStep by remember { mutableStateOf(ForoCopilotProgressStep.READING_PROMPT) }

    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            progressStep = ForoCopilotProgressStep.READING_PROMPT
            delay(1100)
            if (isGenerating) progressStep = ForoCopilotProgressStep.ANALYZING_REPLIES
            delay(1400)
            if (isGenerating) progressStep = ForoCopilotProgressStep.EXTRACTING_OBJECTIVES
            delay(1600)
            if (isGenerating) progressStep = ForoCopilotProgressStep.STRUCTURING_DRAFT
        }
    }

    val currentStage = when {
        errorMessage != null && currentStrategy == null -> SheetStage.ERROR
        currentStrategy != null && !isGenerating -> SheetStage.RESULT
        else -> SheetStage.LOADING
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
        label = "foroCopilotContentMiuix"
    ) { stage ->
        when (stage) {
            SheetStage.LOADING -> {
                CopilotLoadingViewMiuix(
                    step = progressStep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = navInsets.calculateBottomPadding() + 20.dp)
                )
            }
            SheetStage.ERROR -> {
                CopilotErrorViewMiuix(
                    error = errorMessage ?: stringResource(Res.string.copilot_assist_failed_strategy),
                    onRetry = { onGenerate(null) },
                    onClose = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = navInsets.calculateBottomPadding() + 20.dp)
                )
            }
            SheetStage.RESULT -> {
                if (currentStrategy != null) {
                    CopilotResultViewMiuix(
                        topic = topic,
                        strategy = currentStrategy,
                        activeModelName = activeModelName,
                        onGenerate = onGenerate,
                        onInsertDraft = onInsertDraft,
                        onClose = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun CopilotLoadingViewMiuix(
    step: ForoCopilotProgressStep,
    modifier: Modifier = Modifier
) {
    val message = when (step) {
        ForoCopilotProgressStep.READING_PROMPT -> stringResource(Res.string.copilot_step_reading_prompt)
        ForoCopilotProgressStep.ANALYZING_REPLIES -> stringResource(Res.string.copilot_step_analyzing_replies)
        ForoCopilotProgressStep.EXTRACTING_OBJECTIVES -> stringResource(Res.string.copilot_step_identifying_objectives)
        ForoCopilotProgressStep.STRUCTURING_DRAFT -> stringResource(Res.string.copilot_step_structuring_draft)
    }

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
            text = stringResource(Res.string.copilot_scanning_title),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onSurface
        )

        MiuixText(
            text = message,
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
        )

        Spacer(Modifier.height(8.dp))

        ProgressStepRowMiuix(
            label = stringResource(Res.string.copilot_step_reading_prompt),
            isActive = step == ForoCopilotProgressStep.READING_PROMPT,
            isCompleted = step.ordinal > ForoCopilotProgressStep.READING_PROMPT.ordinal
        )
        ProgressStepRowMiuix(
            label = stringResource(Res.string.copilot_step_analyzing_replies),
            isActive = step == ForoCopilotProgressStep.ANALYZING_REPLIES,
            isCompleted = step.ordinal > ForoCopilotProgressStep.ANALYZING_REPLIES.ordinal
        )
        ProgressStepRowMiuix(
            label = stringResource(Res.string.copilot_step_identifying_objectives),
            isActive = step == ForoCopilotProgressStep.EXTRACTING_OBJECTIVES,
            isCompleted = step.ordinal > ForoCopilotProgressStep.EXTRACTING_OBJECTIVES.ordinal
        )
        ProgressStepRowMiuix(
            label = stringResource(Res.string.copilot_step_structuring_draft),
            isActive = step == ForoCopilotProgressStep.STRUCTURING_DRAFT,
            isCompleted = step.ordinal > ForoCopilotProgressStep.STRUCTURING_DRAFT.ordinal
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
private fun CopilotErrorViewMiuix(
    error: String,
    onRetry: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .squircleSurface(MiuixTheme.colorScheme.errorContainer.copy(alpha = 0.2f), 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        }

        MiuixText(
            text = stringResource(Res.string.copilot_assist_failed_strategy),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onSurface
        )

        MiuixText(
            text = error,
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MiuixTextButton(
                text = stringResource(Res.string.copilot_assist_retry_btn),
                onClick = onRetry,
                colors = ButtonDefaults.textButtonColorsPrimary()
            )
            MiuixTextButton(
                text = stringResource(Res.string.review_action_close),
                onClick = onClose,
                colors = ButtonDefaults.textButtonColors(
                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                    textColor = MiuixTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun CopilotResultViewMiuix(
    topic: CanvasDiscussionTopic,
    strategy: CopilotForoStrategy,
    activeModelName: String,
    onGenerate: (toneModifier: String?) -> Unit,
    onInsertDraft: (String) -> Unit,
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

    val maxAvailableContentHeight = remember(windowInfo.containerDpSize.height, safeTopInset) {
        (windowInfo.containerDpSize.height - safeTopInset - 72.dp).coerceAtLeast(320.dp)
    }

    var headerHeightDp by remember { mutableStateOf(110.dp) }
    var footerHeightDp by remember { mutableStateOf(80.dp) }

    val dynamicPagerHeight = remember(maxAvailableContentHeight, headerHeightDp, footerHeightDp) {
        (maxAvailableContentHeight - headerHeightDp - footerHeightDp - 20.dp).coerceIn(160.dp, 440.dp)
    }

    val tabLabels = listOf(
        stringResource(Res.string.copilot_tab_draft),
        stringResource(Res.string.copilot_tab_structure),
        stringResource(Res.string.copilot_tab_tone)
    )

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { tabLabels.size })
    val contourTabListState = rememberLazyListState()
    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = PagerNavigationSpringSpec
    )

    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    var selectedTone by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Encabezado dinámico: Título del Foro, Chip de Modelo y TabRowWithContour
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    headerHeightDp = with(density) { coordinates.size.height.toDp() }
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                insideMargin = PaddingValues(12.dp),
                pressFeedbackType = PressFeedbackType.None
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        MiuixText(
                            text = stringResource(Res.string.copilot_assist_academic_copilot),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.primary
                        )
                        MiuixText(
                            text = topic.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .squircleSurface(MiuixTheme.colorScheme.primary.copy(alpha = 0.12f), 9999.dp)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        MiuixText(
                            text = cleanModelName(activeModelName),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.primary
                        )
                    }
                }
            }

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

        // HorizontalPager sincronizado con altura acotada dinámica
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
                        // Pestaña 0: Borrador sugerido
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .scrollEndHaptic(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            item(key = "draft_card") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                                    insideMargin = PaddingValues(14.dp),
                                    pressFeedbackType = PressFeedbackType.None
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .squircleSurface(MiuixTheme.colorScheme.primary.copy(alpha = 0.12f), 9999.dp)
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.AutoAwesome,
                                                            contentDescription = null,
                                                            tint = MiuixTheme.colorScheme.primary,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        MiuixText(
                                                            text = stringResource(Res.string.copilot_assist_draft_suggested),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MiuixTheme.colorScheme.primary
                                                        )
                                                    }
                                                }

                                                MiuixText(
                                                    text = stringResource(Res.string.copilot_assist_words_count, strategy.wordCount),
                                                    fontSize = 11.sp,
                                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .squircleSurface(MiuixTheme.colorScheme.surfaceContainerHigh, 9999.dp)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                MiuixText(
                                                    text = stringResource(Res.string.copilot_assist_academic_tone),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MiuixTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .squircleSurface(MiuixTheme.colorScheme.surfaceContainerHigh, 12.dp)
                                                .padding(12.dp)
                                        ) {
                                            RichContentRenderer(
                                                text = strategy.suggestedDraft,
                                                themeStyle = AppThemeStyle.MIUIX,
                                                textColor = MiuixTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Pestaña 1: Estructura pedagógica y fases
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .scrollEndHaptic(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            item(key = "diagnostic_card") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                                    insideMargin = PaddingValues(14.dp),
                                    pressFeedbackType = PressFeedbackType.None
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .squircleSurface(MiuixTheme.colorScheme.primary.copy(alpha = 0.12f), 9999.dp)
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Verified,
                                                            contentDescription = null,
                                                            tint = MiuixTheme.colorScheme.primary,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        MiuixText(
                                                            text = stringResource(Res.string.copilot_assist_objective_detected),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MiuixTheme.colorScheme.primary
                                                        )
                                                    }
                                                }

                                                val formattedObjective = remember(strategy.learningObjective) {
                                                    formatMarkdown(strategy.learningObjective)
                                                }
                                                MiuixText(
                                                    text = formattedObjective,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MiuixTheme.colorScheme.onSurface
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .squircleSurface(MiuixTheme.colorScheme.primary.copy(alpha = 0.12f), 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.FactCheck,
                                                    contentDescription = null,
                                                    tint = MiuixTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            MetricPillMiuix(
                                                title = stringResource(Res.string.copilot_assist_technical_rigor),
                                                value = strategy.technicalRigor,
                                                modifier = Modifier.weight(1f)
                                            )
                                            MetricPillMiuix(
                                                title = stringResource(Res.string.copilot_assist_counterexample),
                                                value = strategy.counterExample,
                                                modifier = Modifier.weight(1f)
                                            )
                                            MetricPillMiuix(
                                                title = stringResource(Res.string.copilot_assist_interaction),
                                                value = strategy.activeInteraction,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            item(key = "phases_header") {
                                SmallTitle(
                                    text = stringResource(Res.string.copilot_assist_structure_header),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            item(key = "phases_card") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                                    insideMargin = PaddingValues(0.dp),
                                    pressFeedbackType = PressFeedbackType.None
                                ) {
                                    Column {
                                        StructurePhaseItemMiuix(
                                            phaseNumber = "1",
                                            title = strategy.phase1Title,
                                            description = strategy.phase1Desc
                                        )
                                        StructurePhaseItemMiuix(
                                            phaseNumber = "2",
                                            title = strategy.phase2Title,
                                            description = strategy.phase2Desc,
                                            isAlternate = true
                                        )
                                        StructurePhaseItemMiuix(
                                            phaseNumber = "3",
                                            title = strategy.phase3Title,
                                            description = strategy.phase3Desc
                                        )
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Pestaña 2: Afinar tono
                        val toneFormalPrompt = stringResource(Res.string.copilot_assist_tone_formal_prompt)
                        val toneConcisePrompt = stringResource(Res.string.copilot_assist_tone_concise_prompt)
                        val toneCitationsPrompt = stringResource(Res.string.copilot_assist_tone_citations_prompt)
                        val toneCounterPrompt = stringResource(Res.string.copilot_assist_tone_counterexample_prompt)

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .scrollEndHaptic(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            item(key = "tone_header") {
                                SmallTitle(
                                    text = stringResource(Res.string.copilot_assist_tone_header),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            item(key = "tone_chips_row") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                                    insideMargin = PaddingValues(14.dp),
                                    pressFeedbackType = PressFeedbackType.None
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        MiuixText(
                                            text = "Selecciona un estilo para regenerar el borrador adaptando la profundidad conceptual y técnica:",
                                            fontSize = 12.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            ToneOptionRowMiuix(
                                                icon = Icons.Default.Psychology,
                                                text = stringResource(Res.string.copilot_assist_tone_formal),
                                                isSelected = selectedTone == toneFormalPrompt,
                                                onClick = {
                                                    selectedTone = toneFormalPrompt
                                                    onGenerate(selectedTone)
                                                }
                                            )
                                            ToneOptionRowMiuix(
                                                icon = Icons.Default.Compress,
                                                text = stringResource(Res.string.copilot_assist_tone_concise),
                                                isSelected = selectedTone == toneConcisePrompt,
                                                onClick = {
                                                    selectedTone = toneConcisePrompt
                                                    onGenerate(selectedTone)
                                                }
                                            )
                                            ToneOptionRowMiuix(
                                                icon = Icons.Default.MenuBook,
                                                text = stringResource(Res.string.copilot_assist_tone_citations),
                                                isSelected = selectedTone == toneCitationsPrompt,
                                                onClick = {
                                                    selectedTone = toneCitationsPrompt
                                                    onGenerate(selectedTone)
                                                }
                                            )
                                            ToneOptionRowMiuix(
                                                icon = Icons.Default.CrisisAlert,
                                                text = stringResource(Res.string.copilot_assist_tone_counterexample),
                                                isSelected = selectedTone == toneCounterPrompt,
                                                onClick = {
                                                    selectedTone = toneCounterPrompt
                                                    onGenerate(selectedTone)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )

        // Pie medido dinámicamente: Botones de Acción fijos
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
                    text = if (isCopied) stringResource(Res.string.copilot_assist_copied) else stringResource(Res.string.copilot_assist_copy_text),
                    onClick = {
                        clipboardManager.setText(AnnotatedString(strategy.suggestedDraft))
                        isCopied = true
                        scope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(
                        color = MiuixTheme.colorScheme.surfaceContainerHigh,
                        textColor = if (isCopied) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                    )
                )

                MiuixTextButton(
                    text = stringResource(Res.string.copilot_assist_insert_btn),
                    onClick = {
                        onInsertDraft(strategy.suggestedDraft)
                        dismissState?.invoke() ?: onClose()
                    },
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
            }

            Spacer(Modifier.height(navInsets.calculateBottomPadding() + 8.dp))
        }
    }
}

@Composable
private fun MetricPillMiuix(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .squircleSurface(MiuixTheme.colorScheme.surfaceContainerHigh, 10.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            MiuixText(
                text = title,
                fontSize = 10.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
            MiuixText(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StructurePhaseItemMiuix(
    phaseNumber: String,
    title: String,
    description: String,
    isAlternate: Boolean = false
) {
    val bg = if (isAlternate) MiuixTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .squircleSurface(MiuixTheme.colorScheme.primary.copy(alpha = 0.15f), 9999.dp),
            contentAlignment = Alignment.Center
        ) {
            MiuixText(
                text = phaseNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val formattedTitle = remember(title) { formatMarkdown(title) }
            val formattedDesc = remember(description) { formatMarkdown(description) }
            MiuixText(
                text = formattedTitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface
            )
            MiuixText(
                text = formattedDesc,
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        }
    }
}

@Composable
private fun ToneOptionRowMiuix(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainerHigh
    val textColor = if (isSelected) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
    val iconColor = if (isSelected) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .squircleSurface(bg, 12.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        MiuixText(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ForoCopilotContentMaterial(
    topic: CanvasDiscussionTopic,
    strategy: CopilotForoStrategy?,
    isGenerating: Boolean,
    errorMessage: String?,
    activeModelName: String,
    onDismiss: () -> Unit,
    onGenerate: (toneModifier: String?) -> Unit,
    onInsertDraft: (String) -> Unit
) {
    val navInsets = WindowInsets.navigationBars.asPaddingValues()

    var lastValidStrategy by remember { mutableStateOf(strategy) }
    if (strategy != null) {
        lastValidStrategy = strategy
    }
    val currentStrategy = strategy ?: lastValidStrategy

    val currentStage = when {
        errorMessage != null && currentStrategy == null -> SheetStage.ERROR
        currentStrategy != null && !isGenerating -> SheetStage.RESULT
        else -> SheetStage.LOADING
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = navInsets.calculateBottomPadding() + 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(Res.string.copilot_sheet_title),
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
            label = "foroCopilotContentMaterial"
        ) { stage ->
            when (stage) {
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
                            text = stringResource(Res.string.copilot_scanning_title),
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
                            text = errorMessage ?: stringResource(Res.string.copilot_assist_failed_strategy),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onGenerate(null) }) {
                                Text(stringResource(Res.string.copilot_assist_retry_btn))
                            }
                            TextButton(onClick = onDismiss) {
                                Text(stringResource(Res.string.review_action_close))
                            }
                        }
                    }
                }
                SheetStage.RESULT -> {
                    if (currentStrategy != null) {
                        CopilotResultViewMaterial(
                            topic = topic,
                            strategy = currentStrategy,
                            activeModelName = activeModelName,
                            onGenerate = onGenerate,
                            onInsertDraft = onInsertDraft,
                            onClose = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CopilotResultViewMaterial(
    topic: CanvasDiscussionTopic,
    strategy: CopilotForoStrategy,
    activeModelName: String,
    onGenerate: (toneModifier: String?) -> Unit,
    onInsertDraft: (String) -> Unit,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabLabels = listOf(
        stringResource(Res.string.copilot_tab_draft),
        stringResource(Res.string.copilot_tab_structure),
        stringResource(Res.string.copilot_tab_tone)
    )

    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    var selectedTone by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.copilot_assist_academic_copilot),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = cleanModelName(activeModelName),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        TabRow(selectedTabIndex = selectedTabIndex) {
            tabLabels.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(label) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.copilot_assist_draft_suggested),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(Res.string.copilot_assist_words_count, strategy.wordCount),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        RichContentRenderer(
                            text = strategy.suggestedDraft,
                            themeStyle = AppThemeStyle.MATERIAL,
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            1 -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.copilot_assist_objective_detected),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formatMarkdown(strategy.learningObjective),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            2 -> {
                val toneFormalPrompt = stringResource(Res.string.copilot_assist_tone_formal_prompt)
                val toneConcisePrompt = stringResource(Res.string.copilot_assist_tone_concise_prompt)
                val toneCitationsPrompt = stringResource(Res.string.copilot_assist_tone_citations_prompt)
                val toneCounterPrompt = stringResource(Res.string.copilot_assist_tone_counterexample_prompt)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            selectedTone = toneFormalPrompt
                            onGenerate(selectedTone)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.copilot_assist_tone_formal))
                    }
                    OutlinedButton(
                        onClick = {
                            selectedTone = toneConcisePrompt
                            onGenerate(selectedTone)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.copilot_assist_tone_concise))
                    }
                    OutlinedButton(
                        onClick = {
                            selectedTone = toneCitationsPrompt
                            onGenerate(selectedTone)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.copilot_assist_tone_citations))
                    }
                    OutlinedButton(
                        onClick = {
                            selectedTone = toneCounterPrompt
                            onGenerate(selectedTone)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.copilot_assist_tone_counterexample))
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(strategy.suggestedDraft))
                    isCopied = true
                    scope.launch {
                        delay(2000)
                        isCopied = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isCopied) stringResource(Res.string.copilot_assist_copied) else stringResource(Res.string.copilot_assist_copy_text))
            }

            Button(
                onClick = {
                    onInsertDraft(strategy.suggestedDraft)
                    onClose()
                },
                modifier = Modifier.weight(1.3f)
            ) {
                Text(stringResource(Res.string.copilot_assist_insert_btn))
            }
        }
    }
}
