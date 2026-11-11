// Copyright 2026, compose-miuix-ui contributors / NotiVas contributors
// SPDX-License-Identifier: Apache-2.0

package me.joxquin.notivas.ui.dashboard.components.search

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.data.model.CanvasDiscussionTopic
import me.joxquin.notivas.data.model.Course
import me.joxquin.notivas.shared.resources.Res
import me.joxquin.notivas.shared.resources.cancel
import me.joxquin.notivas.shared.resources.dashboard_search_hint
import me.joxquin.notivas.ui.components.BackHandler
import me.joxquin.notivas.ui.dashboard.AssignmentUiModel
import me.joxquin.notivas.ui.dashboard.CourseStat
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.glass.GlassDefaults
import top.yukonga.miuix.kmp.glass.GlassShadows
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Close
import top.yukonga.miuix.kmp.icon.glass.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

// Map one second of spring motion onto the 400ms search transition (HyperOS Official).
private val SearchModeEasing: Easing = run {
    val omega = 2.0 * PI / 0.75f
    val stiffness = (omega * omega).toFloat()
    val damping = (0.98f * 2.0 * omega).toFloat()
    val decay = -damping / 2f
    val frequency = sqrt(-(damping.toDouble() * damping - stiffness.toDouble() * 4)).toFloat() / 2f
    val sineCoefficient = decay / frequency
    Easing { fraction ->
        (
            1.0 + exp((decay * fraction).toDouble()) *
                (-cos((frequency * fraction).toDouble()) + sineCoefficient * sin((frequency * fraction).toDouble()))
            ).toFloat()
    }
}

/**
 * Overlay de búsqueda nativo de HyperOS (GlassSearchOverlay), adaptado a NotiVas con
 * historial reciente, resultados de Canvas en vivo y acceso directo a Copilot IA.
 */
@Composable
fun GlassSearchOverlay(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchCommit: (String) -> Unit,
    recentSearches: List<String>,
    onRemoveRecentQuery: (String) -> Unit,
    onClearRecentHistory: () -> Unit,
    courses: List<Course>,
    courseStats: List<CourseStat>,
    assignments: List<AssignmentUiModel>,
    discussions: List<CanvasDiscussionTopic> = emptyList(),
    onCourseClick: (Course) -> Unit,
    onAssignmentClick: (AssignmentUiModel) -> Unit,
    onForumClick: (CanvasDiscussionTopic) -> Unit = {},
    onAskAi: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = updateTransition(expanded, label = "glassSearch")
    val progress = transition.animateFloat(
        transitionSpec = { tween(400, easing = SearchModeEasing) },
        label = "glassSearchGeometry",
    ) { if (it) 1f else 0f }

    val cancelAlpha = transition.animateFloat(
        transitionSpec = { tween(150, delayMillis = if (targetState) 100 else 0, easing = LinearEasing) },
        label = "glassSearchCancelMaterial",
    ) { if (it) 1f else 0f }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val direction = LocalLayoutDirection.current
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)
    val active = expanded || transition.currentState || transition.isRunning

    LaunchedEffect(expanded) {
        if (expanded) {
            focusRequester.requestFocus()
            keyboard?.show()
        } else if (active) {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }

    if (!active) return

    BackHandler(
        enabled = expanded,
        onBack = onDismissRequest,
    )

    val colors = MiuixTheme.colorScheme
    val hintText = stringResource(Res.string.dashboard_search_hint)
    val cancelContentDesc = stringResource(Res.string.cancel)

    Box(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent {
                if (it.key == Key.Escape && it.type == KeyEventType.KeyUp) {
                    onDismissRequest()
                    true
                } else {
                    false
                }
            },
    ) {
        // Scrim de fondo que atenúa la pantalla detrás del overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress.value }
                .background(colors.surface)
                .clickable(interactionSource = null, indication = null) {
                    keyboard?.hide()
                    focusManager.clearFocus()
                },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress.value }
                .windowInsetsPadding(WindowInsets.statusBars)
                .imePadding(),
        ) {
            // Cabecera de búsqueda con animación elástica
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .graphicsLayer { translationY = -statusBarHeight * (1f - progress.value) },
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    enabled = expanded,
                    singleLine = true,
                    textStyle = MiuixTheme.textStyles.body1.copy(color = colors.onSurface),
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (query.isNotBlank()) {
                                onSearchCommit(query.trim())
                            }
                            keyboard?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .layout { measurable, constraints ->
                            val endSpace = (54.dp.toPx() * progress.value).toInt()
                            val width = (constraints.maxWidth - endSpace).coerceAtLeast(0)
                            val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                            layout(constraints.maxWidth, placeable.height) {
                                placeable.placeRelative(0, 0)
                            }
                        }
                        .height(44.dp)
                        .focusRequester(focusRequester),
                    decorationBox = { field ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.surfaceContainerHigh, CircleShape)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Glass.Search,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = colors.onSurfaceVariantSummary,
                            )
                            Box(Modifier.weight(1f)) {
                                if (query.isEmpty()) {
                                    Text(
                                        text = hintText,
                                        color = colors.onSurfaceVariantSummary,
                                        style = MiuixTheme.textStyles.body1,
                                    )
                                }
                                field()
                            }
                            if (query.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        onQueryChange("")
                                        focusRequester.requestFocus()
                                    },
                                    modifier = Modifier.size(24.dp),
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.Glass.Close,
                                        contentDescription = "Limpiar",
                                        modifier = Modifier.size(18.dp),
                                        tint = colors.onSurfaceVariantSummary,
                                    )
                                }
                            }
                        }
                    },
                )

                // Botón Cancelar con animación de salida angular y blur
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .graphicsLayer {
                            val fraction = progress.value
                            translationX = 66.dp.toPx() * (1f - fraction) * if (direction == LayoutDirection.Ltr) 1f else -1f
                            scaleX = fraction
                            scaleY = fraction
                            alpha = cancelAlpha.value
                            val blur = 50f * (1f - cancelAlpha.value)
                            renderEffect = if (blur > 0.5f) BlurEffect(blur, blur, TileMode.Decal) else null
                        },
                ) {
                    IconButton(
                        onClick = onDismissRequest,
                        enabled = expanded,
                        modifier = Modifier
                            .size(44.dp)
                            .background(colors.surfaceContainerHigh, CircleShape),
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Glass.Close,
                            contentDescription = cancelContentDesc,
                            modifier = Modifier.size(24.dp),
                            tint = colors.onSurface,
                        )
                    }
                }
            }

            // Cuerpo de resultados / historial con desvanecimiento coordinado
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = progress.value
                    },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    if (query.isBlank()) {
                        item(key = "search_history") {
                            SearchHistorySection(
                                recentSearches = recentSearches,
                                onSelectQuery = { selectedQuery ->
                                    onQueryChange(selectedQuery)
                                    onSearchCommit(selectedQuery)
                                    keyboard?.hide()
                                    focusManager.clearFocus()
                                },
                                onRemoveQuery = onRemoveRecentQuery,
                                onClearHistory = onClearRecentHistory,
                                onAskAi = { onAskAi(query) },
                                themeStyle = AppThemeStyle.MIUIX,
                            )
                        }
                    } else {
                        item(key = "search_live_results") {
                            SearchLiveResultsSection(
                                query = query,
                                courses = courses,
                                courseStats = courseStats,
                                assignments = assignments,
                                discussions = discussions,
                                onCourseClick = { course ->
                                    onSearchCommit(query)
                                    onDismissRequest()
                                    onCourseClick(course)
                                },
                                onAssignmentClick = { assignment ->
                                    onSearchCommit(query)
                                    onDismissRequest()
                                    onAssignmentClick(assignment)
                                },
                                onForumClick = { forum ->
                                    onSearchCommit(query)
                                    onDismissRequest()
                                    onForumClick(forum)
                                },
                                onAskCopilot = { onAskAi(query) },
                                themeStyle = AppThemeStyle.MIUIX,
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val SEARCH_TOP_BAR_BLUR_RADIUS = 50f

/**
 * Controla la opacidad del TopAppBar durante la entrada y salida del modo de búsqueda.
 */
@Composable
fun rememberGlassSearchBarAlpha(expanded: Boolean): State<Float> {
    val transition = updateTransition(expanded, label = "glassSearchTopBarOpacity")
    return transition.animateFloat(
        transitionSpec = {
            tween(150, easing = if (targetState) CubicBezierEasing(0f, 0f, 0.67f, 1f) else CubicBezierEasing(0.26f, 0f, 0.6f, 0.2f))
        },
        label = "glassSearchTopBarAlpha",
    ) { if (it) 0f else 1f }
}

/**
 * Transición HyperOS canónica para la barra superior (TopAppBar) al entrar o salir del modo de búsqueda.
 * El TopAppBar se desvanece (alpha), reduce su escala a 0.9 y aplica un desenfoque progresivo de 50f con SearchModeEasing.
 */
@Composable
fun Modifier.glassSearchBarTransition(expanded: Boolean, barAlpha: () -> Float): Modifier {
    val density = LocalDensity.current
    val layerOutset = remember(density) {
        val shadow = GlassShadows.Regular
        val shadowOutset = ((shadow.radius + maxOf(abs(shadow.offsetX), abs(shadow.offsetY))) / GlassDefaults.SourceDensity).dp
        // Mantiene un halo fijo para las sombras y el desenfoque máximo durante el fade offscreen
        with(density) { shadowOutset.toPx() } + SEARCH_TOP_BAR_BLUR_RADIUS * 3f
    }
    val transition = updateTransition(expanded, label = "glassSearchTopBar")
    val scale = transition.animateFloat(
        transitionSpec = { tween(400, easing = SearchModeEasing) },
        label = "glassSearchTopBarScale",
    ) { if (it) 0.9f else 1f }
    val blur = transition.animateFloat(
        transitionSpec = { tween(if (targetState) 150 else 120, easing = LinearEasing) },
        label = "glassSearchTopBarBlur",
    ) { if (it) SEARCH_TOP_BAR_BLUR_RADIUS else 0f }
    return drawWithCache {
        val layer = obtainGraphicsLayer()
        val halo = ceil(layerOutset).toInt()
        val layerSize = IntSize(size.width.toInt() + halo * 2, size.height.toInt() + halo * 2)
        onDrawWithContent {
            val opacity = barAlpha().coerceIn(0f, 1f)
            val currentScale = scale.value
            val currentBlur = blur.value
            if (opacity > 0f) {
                if (opacity == 1f && currentScale == 1f && currentBlur == 0f) {
                    drawContent()
                } else {
                    layer.record(density = this@drawWithCache, size = layerSize) {
                        translate(halo.toFloat(), halo.toFloat()) { this@record.drawContent() }
                    }
                    layer.alpha = opacity
                    layer.scaleX = currentScale
                    layer.scaleY = currentScale
                    layer.renderEffect = if (currentBlur > 0.5f) BlurEffect(currentBlur, currentBlur, TileMode.Decal) else null
                    translate(-halo.toFloat(), -halo.toFloat()) { drawLayer(layer) }
                }
            }
        }
    }
}

/**
 * Controla el desvanecimiento y reaparición con rebote del título de la barra al salir del modo búsqueda.
 */
@Composable
fun rememberGlassSearchTitleAlpha(expanded: Boolean, collapsed: Boolean): () -> Float {
    val transition = updateTransition(expanded, label = "glassSearchTitlePhase")
    val progress = transition.animateFloat(
        transitionSpec = { tween(400, easing = SearchModeEasing) },
        label = "glassSearchTitleExitProgress",
    ) { if (it) 0f else 1f }
    val reveal by remember(expanded, progress) { derivedStateOf { expanded || progress.value > 0.8f } }
    val response = if (collapsed) 0.15f else 0.6f
    val titleAlpha = animateFloatAsState(
        targetValue = if (reveal) 1f else 0f,
        animationSpec = if (reveal && !expanded) {
            spring(dampingRatio = 1f, stiffness = (2 * PI / response).let { (it * it).toFloat() }, visibilityThreshold = 0.001f)
        } else {
            snap()
        },
        label = "glassSearchTitleAlpha",
    )
    return remember(titleAlpha) { { titleAlpha.value } }
}