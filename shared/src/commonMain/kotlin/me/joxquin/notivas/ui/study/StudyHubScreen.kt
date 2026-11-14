package me.joxquin.notivas.ui.study

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold as MaterialScaffold
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.domain.study.model.StudyDeck
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.miuix.BackNavigationIcon
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.miuixBackdropModifier
import me.joxquin.notivas.ui.components.miuix.miuixPageContentPadding
import me.joxquin.notivas.ui.components.miuix.miuixPageScrollModifiers
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun StudyHubScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    onOpenDeckSession: (Long) -> Unit,
    onStartQuizSession: (Long) -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    modifier: Modifier = Modifier
) {
    val decks by viewModel.decks.collectAsState()
    val overviewStats by viewModel.overviewStats.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    var showCreateDeckSheet by remember { mutableStateOf(false) }
    var showAiGenerateSheet by remember { mutableStateOf(false) }

    var newDeckTitle by remember { mutableStateOf("") }
    var newDeckDescription by remember { mutableStateOf("") }
    var aiTopicText by remember { mutableStateOf("") }

    if (themeStyle == AppThemeStyle.MIUIX) {
        val backdrop = rememberMiuixBlurBackdrop()
        val scrollBehavior = MiuixScrollBehavior()
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()

        Scaffold(
            topBar = {
                MiuixBlurredBar(
                    backdrop = backdrop,
                    scrollBehavior = scrollBehavior
                ) {
                    MiuixPageTopAppBar(
                        title = stringResource(Res.string.study_hub_title),
                        subtitle = stringResource(Res.string.study_hub_desc),
                        navigationIcon = { BackNavigationIcon(onClick = onBack) },
                        scrollBehavior = scrollBehavior
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            val contentPadding = miuixPageContentPadding(
                innerPadding = innerPadding,
                extraTop = 8.dp,
                extraStart = 0.dp,
                extraEnd = 0.dp,
                extraBottom = 96.dp
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MiuixTheme.colorScheme.surface)
                    .miuixBackdropModifier(backdrop)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .miuixPageScrollModifiers(scrollBehavior),
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Acciones Rápidas
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TextButton(
                                text = stringResource(Res.string.study_action_generate_ai),
                                onClick = { showAiGenerateSheet = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColorsPrimary()
                            )
                            TextButton(
                                text = stringResource(Res.string.study_action_new_deck),
                                onClick = { showCreateDeckSheet = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColors(
                                    color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                    textColor = MiuixTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    if (decks.isNotEmpty()) {
                        item {
                            SmallTitle(
                                text = stringResource(Res.string.study_bento_title),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        items(decks, key = { it.id }) { deck ->
                            MiuixDeckItemCard(
                                deck = deck,
                                onReviewClick = { onOpenDeckSession(deck.id) },
                                onQuizClick = { onStartQuizSession(deck.id) }
                            )
                        }
                    } else {
                        item {
                            MiuixEmptyDecksCard(
                                onGenerateClick = { showAiGenerateSheet = true }
                            )
                        }
                    }
                }

                VerticalScrollBar(
                    adapter = rememberScrollBarAdapter(listState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    trackPadding = contentPadding
                )
            }
        }
    } else {
        // Material Design 3 View
        MaterialScaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            MaterialText(
                                text = stringResource(Res.string.study_hub_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            MaterialText(
                                text = stringResource(Res.string.study_hub_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
            LazyColumn(
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = 96.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        androidx.compose.material3.Button(
                            onClick = { showAiGenerateSheet = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            androidx.compose.material3.Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            MaterialText(stringResource(Res.string.study_action_generate_ai))
                        }
                        androidx.compose.material3.OutlinedButton(
                            onClick = { showCreateDeckSheet = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            androidx.compose.material3.Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            MaterialText(stringResource(Res.string.study_action_new_deck))
                        }
                    }
                }

                if (decks.isNotEmpty()) {
                    items(decks, key = { it.id }) { deck ->
                        MaterialDeckItemCard(
                            deck = deck,
                            onReviewClick = { onOpenDeckSession(deck.id) },
                            onQuizClick = { onStartQuizSession(deck.id) }
                        )
                    }
                } else {
                    item {
                        MaterialEmptyDecksCard(
                            onGenerateClick = { showAiGenerateSheet = true }
                        )
                    }
                }
            }
        }
    }

    // Sheet: Crear Mazo Manual
    if (showCreateDeckSheet) {
        WindowBottomSheet(
            onDismissRequest = { showCreateDeckSheet = false },
            title = stringResource(Res.string.study_action_new_deck)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = newDeckTitle,
                    onValueChange = { newDeckTitle = it },
                    label = stringResource(Res.string.study_deck_create_name_label),
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = newDeckDescription,
                    onValueChange = { newDeckDescription = it },
                    label = stringResource(Res.string.study_deck_create_desc_label),
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    text = stringResource(Res.string.study_deck_create_confirm),
                    onClick = {
                        if (newDeckTitle.isNotBlank()) {
                            viewModel.createDeck(newDeckTitle, newDeckDescription)
                            newDeckTitle = ""
                            newDeckDescription = ""
                            showCreateDeckSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Sheet: Generar con IA
    if (showAiGenerateSheet) {
        WindowBottomSheet(
            onDismissRequest = { if (!isGenerating) showAiGenerateSheet = false },
            title = stringResource(Res.string.study_ai_generate_title)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = aiTopicText,
                    onValueChange = { aiTopicText = it },
                    label = stringResource(Res.string.study_ai_prompt_hint),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isGenerating) {
                    Text(
                        text = stringResource(Res.string.study_ai_generating),
                        fontSize = 12.sp,
                        color = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                TextButton(
                    text = stringResource(Res.string.study_action_generate_ai),
                    onClick = {
                        if (aiTopicText.isNotBlank() && !isGenerating) {
                            val topic = aiTopicText
                            val title = if (topic.length > 25) topic.take(25) + "..." else topic
                            viewModel.generateDeckWithAi(
                                topicOrContext = topic,
                                deckTitle = title,
                                onComplete = {
                                    aiTopicText = ""
                                    showAiGenerateSheet = false
                                }
                            )
                        }
                    },
                    enabled = !isGenerating && aiTopicText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MiuixDeckItemCard(
    deck: StudyDeck,
    onReviewClick: () -> Unit,
    onQuizClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        onClick = onReviewClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deck.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (deck.courseName != null) {
                        Text(
                            text = deck.courseName,
                            fontSize = 11.sp,
                            color = MiuixTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (deck.dueCardsCount > 0) {
                    Box(
                        modifier = Modifier
                            .squircleSurface(
                                color = MiuixTheme.colorScheme.primary,
                                cornerRadius = 6.dp
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${deck.dueCardsCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${deck.totalCards} tarjetas",
                    fontSize = 11.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        text = stringResource(Res.string.study_action_start_quiz),
                        onClick = onQuizClick,
                        colors = ButtonDefaults.textButtonColors(
                            color = MiuixTheme.colorScheme.surfaceContainerHigh,
                            textColor = MiuixTheme.colorScheme.onSurface
                        )
                    )
                    TextButton(
                        text = stringResource(Res.string.study_action_practice_now),
                        onClick = onReviewClick,
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }
        }
    }
}

@Composable
private fun MiuixEmptyDecksCard(
    onGenerateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        insideMargin = PaddingValues(20.dp),
        pressFeedbackType = PressFeedbackType.None
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.School,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = stringResource(Res.string.study_empty_decks_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(Res.string.study_empty_decks_desc),
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(4.dp))
            TextButton(
                text = stringResource(Res.string.study_action_generate_ai),
                onClick = onGenerateClick,
                colors = ButtonDefaults.textButtonColorsPrimary()
            )
        }
    }
}

@Composable
private fun MaterialDeckItemCard(
    deck: StudyDeck,
    onReviewClick: () -> Unit,
    onQuizClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onReviewClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    MaterialText(
                        text = deck.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (deck.courseName != null) {
                        MaterialText(
                            text = deck.courseName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (deck.dueCardsCount > 0) {
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        MaterialText(
                            text = "${deck.dueCardsCount}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MaterialText(
                    text = "${deck.totalCards} tarjetas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onQuizClick
                    ) {
                        MaterialText(stringResource(Res.string.study_action_start_quiz))
                    }
                    androidx.compose.material3.Button(
                        onClick = onReviewClick
                    ) {
                        MaterialText(stringResource(Res.string.study_action_practice_now))
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialEmptyDecksCard(
    onGenerateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Outlined.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            MaterialText(
                text = stringResource(Res.string.study_empty_decks_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            MaterialText(
                text = stringResource(Res.string.study_empty_decks_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            androidx.compose.material3.Button(
                onClick = onGenerateClick
            ) {
                MaterialText(stringResource(Res.string.study_action_generate_ai))
            }
        }
    }
}
