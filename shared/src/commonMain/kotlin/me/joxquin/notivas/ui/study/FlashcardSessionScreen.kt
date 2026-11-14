package me.joxquin.notivas.ui.study

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold as MaterialScaffold
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.domain.study.model.Flashcard
import me.joxquin.notivas.domain.study.model.FsrsRating
import me.joxquin.notivas.domain.study.model.StudyDeck
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.miuix.BackNavigationIcon
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun FlashcardSessionScreen(
    deckId: Long,
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    modifier: Modifier = Modifier
) {
    var cards by remember { mutableStateOf<List<Flashcard>>(emptyList()) }
    var deck by remember { mutableStateOf<StudyDeck?>(null) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(deckId) {
        deck = viewModel.getDeck(deckId)
        val loaded = viewModel.getCardsForDeck(deckId)
        cards = loaded
        if (loaded.isEmpty()) {
            isCompleted = true
        }
    }

    val currentCard = cards.getOrNull(currentIndex)

    fun handleRating(rating: FsrsRating) {
        if (currentCard != null) {
            viewModel.reviewCard(currentCard, rating)
            if (currentIndex + 1 < cards.size) {
                isFlipped = false
                showHint = false
                currentIndex += 1
            } else {
                isCompleted = true
            }
        }
    }

    if (themeStyle == AppThemeStyle.MIUIX) {
        val backdrop = rememberMiuixBlurBackdrop()
        val scrollBehavior = MiuixScrollBehavior()

        Scaffold(
            topBar = {
                MiuixBlurredBar(
                    backdrop = backdrop,
                    scrollBehavior = scrollBehavior
                ) {
                    MiuixPageTopAppBar(
                        title = deck?.title ?: stringResource(Res.string.study_hub_title),
                        subtitle = if (!isCompleted && cards.isNotEmpty()) "${currentIndex + 1} / ${cards.size}" else "",
                        navigationIcon = { BackNavigationIcon(onClick = onBack) },
                        scrollBehavior = scrollBehavior
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MiuixTheme.colorScheme.surface)
                    .padding(innerPadding)
            ) {
                if (isCompleted) {
                    MiuixSessionCompletedContent(onFinish = onBack)
                } else if (currentCard != null) {
                    MiuixFlashcardContent(
                        card = currentCard,
                        isFlipped = isFlipped,
                        showHint = showHint,
                        onFlip = { isFlipped = !isFlipped },
                        onToggleHint = { showHint = !showHint },
                        onRating = { handleRating(it) }
                    )
                }
            }
        }
    } else {
        // Material 3 View
        MaterialScaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            MaterialText(
                                text = deck?.title ?: stringResource(Res.string.study_hub_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (!isCompleted && cards.isNotEmpty()) {
                                MaterialText(
                                    text = "${currentIndex + 1} / ${cards.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isCompleted) {
                    MaterialSessionCompletedContent(onFinish = onBack)
                } else if (currentCard != null) {
                    MaterialFlashcardContent(
                        card = currentCard,
                        isFlipped = isFlipped,
                        showHint = showHint,
                        onFlip = { isFlipped = !isFlipped },
                        onToggleHint = { showHint = !showHint },
                        onRating = { handleRating(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MiuixFlashcardContent(
    card: Flashcard,
    isFlipped: Boolean,
    showHint: Boolean,
    onFlip: () -> Unit,
    onToggleHint: () -> Unit,
    onRating: (FsrsRating) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "cardFlipAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Tarjeta interactiva con rotación 3D
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                },
            colors = CardDefaults.defaultColors(
                color = if (isFlipped) MiuixTheme.colorScheme.surfaceContainerHighest else MiuixTheme.colorScheme.surfaceContainer
            ),
            insideMargin = PaddingValues(24.dp),
            pressFeedbackType = PressFeedbackType.Sink,
            onClick = onFlip
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (rotation > 90f) {
                            rotationY = 180f
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isFlipped) stringResource(Res.string.study_card_back) else stringResource(Res.string.study_card_front),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = if (isFlipped) card.back else card.front,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MiuixTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    if (!isFlipped && card.hint != null) {
                        Spacer(Modifier.height(16.dp))
                        if (showHint) {
                            Text(
                                text = card.hint,
                                fontSize = 12.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            TextButton(
                                text = stringResource(Res.string.study_card_hint),
                                onClick = onToggleHint,
                                colors = ButtonDefaults.textButtonColors(
                                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                    textColor = MiuixTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(Res.string.study_card_flip),
                        fontSize = 10.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Controles de calificación FSRS (aparecen al voltear la tarjeta)
        if (isFlipped) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    text = stringResource(Res.string.study_rating_again),
                    onClick = { onRating(FsrsRating.AGAIN) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(
                        color = MiuixTheme.colorScheme.errorContainer,
                        textColor = MiuixTheme.colorScheme.onErrorContainer
                    )
                )
                TextButton(
                    text = stringResource(Res.string.study_rating_hard),
                    onClick = { onRating(FsrsRating.HARD) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(
                        color = MiuixTheme.colorScheme.surfaceContainerHigh,
                        textColor = MiuixTheme.colorScheme.onSurface
                    )
                )
                TextButton(
                    text = stringResource(Res.string.study_rating_good),
                    onClick = { onRating(FsrsRating.GOOD) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
                TextButton(
                    text = stringResource(Res.string.study_rating_easy),
                    onClick = { onRating(FsrsRating.EASY) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(
                        color = MiuixTheme.colorScheme.surfaceContainerHighest,
                        textColor = MiuixTheme.colorScheme.primary
                    )
                )
            }
        } else {
            TextButton(
                text = stringResource(Res.string.study_card_flip),
                onClick = onFlip,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                    textColor = MiuixTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun MiuixSessionCompletedContent(onFinish: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
            insideMargin = PaddingValues(24.dp),
            pressFeedbackType = PressFeedbackType.None
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = stringResource(Res.string.study_session_completed_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(Res.string.study_session_completed_desc),
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    text = "Aceptar",
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
            }
        }
    }
}

@Composable
private fun MaterialFlashcardContent(
    card: Flashcard,
    isFlipped: Boolean,
    showHint: Boolean,
    onFlip: () -> Unit,
    onToggleHint: () -> Unit,
    onRating: (FsrsRating) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "cardFlipAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        androidx.compose.material3.Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable(onClick = onFlip),
            shape = RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = if (isFlipped) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .graphicsLayer {
                        if (rotation > 90f) {
                            rotationY = 180f
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    MaterialText(
                        text = if (isFlipped) stringResource(Res.string.study_card_back) else stringResource(Res.string.study_card_front),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    MaterialText(
                        text = if (isFlipped) card.back else card.front,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    if (!isFlipped && card.hint != null) {
                        Spacer(Modifier.height(16.dp))
                        if (showHint) {
                            MaterialText(
                                text = card.hint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            androidx.compose.material3.TextButton(onClick = onToggleHint) {
                                MaterialText(stringResource(Res.string.study_card_hint))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    MaterialText(
                        text = stringResource(Res.string.study_card_flip),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (isFlipped) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.Button(
                    onClick = { onRating(FsrsRating.AGAIN) },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    MaterialText(stringResource(Res.string.study_rating_again), fontSize = 11.sp)
                }
                androidx.compose.material3.Button(
                    onClick = { onRating(FsrsRating.HARD) },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    MaterialText(stringResource(Res.string.study_rating_hard), fontSize = 11.sp)
                }
                androidx.compose.material3.Button(
                    onClick = { onRating(FsrsRating.GOOD) },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    MaterialText(stringResource(Res.string.study_rating_good), fontSize = 11.sp)
                }
                androidx.compose.material3.Button(
                    onClick = { onRating(FsrsRating.EASY) },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    MaterialText(stringResource(Res.string.study_rating_easy), fontSize = 11.sp)
                }
            }
        } else {
            androidx.compose.material3.OutlinedButton(
                onClick = onFlip,
                modifier = Modifier.fillMaxWidth()
            ) {
                MaterialText(stringResource(Res.string.study_card_flip))
            }
        }
    }
}

@Composable
private fun MaterialSessionCompletedContent(onFinish: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                MaterialText(
                    text = stringResource(Res.string.study_session_completed_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                MaterialText(
                    text = stringResource(Res.string.study_session_completed_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MaterialText("Aceptar")
                }
            }
        }
    }
}
