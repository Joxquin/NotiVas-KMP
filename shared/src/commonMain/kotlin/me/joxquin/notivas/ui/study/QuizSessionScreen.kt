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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold as MaterialScaffold
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.joxquin.notivas.data.local.AppThemeStyle
import me.joxquin.notivas.domain.study.model.QuizQuestion
import me.joxquin.notivas.domain.study.model.QuizResult
import me.joxquin.notivas.domain.study.model.StudyDeck
import me.joxquin.notivas.shared.resources.*
import me.joxquin.notivas.ui.components.miuix.BackNavigationIcon
import me.joxquin.notivas.ui.components.miuix.MiuixBlurredBar
import me.joxquin.notivas.ui.components.miuix.MiuixPageTopAppBar
import me.joxquin.notivas.ui.components.miuix.rememberMiuixBlurBackdrop
import me.joxquin.notivas.util.DateTimeUtil
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
fun QuizSessionScreen(
    deckId: Long,
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    themeStyle: AppThemeStyle = AppThemeStyle.MATERIAL,
    modifier: Modifier = Modifier
) {
    var deck by remember { mutableStateOf<StudyDeck?>(null) }
    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentIndex by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() }
    var isSubmitted by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(deckId) {
        val loadedDeck = viewModel.getDeck(deckId)
        deck = loadedDeck
        if (loadedDeck != null) {
            val result = viewModel.generateQuizForDeck(loadedDeck)
            result.onSuccess {
                questions = it
                isLoading = false
            }.onFailure {
                // Generar preguntas básicas a partir de las tarjetas del mazo si falla la IA
                val cards = viewModel.getCardsForDeck(deckId)
                if (cards.isNotEmpty()) {
                    questions = cards.take(5).mapIndexed { idx, card ->
                        val wrongAnswers = cards.filter { it.id != card.id }.map { it.back }.shuffled().take(3)
                        val allOptions = (wrongAnswers + card.back).shuffled()
                        val correctIdx = allOptions.indexOf(card.back).coerceAtLeast(0)
                        QuizQuestion(
                            id = (idx + 1).toString(),
                            question = card.front,
                            options = allOptions,
                            correctOptionIndex = correctIdx,
                            explanation = card.back
                        )
                    }
                }
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    LaunchedEffect(isLoading, isSubmitted) {
        if (!isLoading && !isSubmitted) {
            while (true) {
                delay(1000L)
                elapsedSeconds += 1
            }
        }
    }

    fun submitExam() {
        isSubmitted = true
        var correctCount = 0
        questions.forEachIndexed { idx, q ->
            if (userAnswers[idx] == q.correctOptionIndex) {
                correctCount += 1
            }
        }
        val percentage = if (questions.isNotEmpty()) (correctCount.toFloat() / questions.size) * 100f else 0f
        viewModel.saveQuizResult(
            QuizResult(
                deckId = deckId,
                totalQuestions = questions.size,
                correctAnswers = correctCount,
                scorePercentage = percentage,
                durationSeconds = elapsedSeconds.toLong(),
                timestampEpochMs = DateTimeUtil.nowEpochMillis()
            )
        )
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
                        title = stringResource(Res.string.study_action_start_quiz),
                        subtitle = if (questions.isNotEmpty() && !isSubmitted) "${currentIndex + 1} / ${questions.size} • ${elapsedSeconds}s" else "",
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
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(Res.string.study_ai_generating),
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                } else if (isSubmitted) {
                    MiuixQuizResultsView(
                        questions = questions,
                        userAnswers = userAnswers,
                        elapsedSeconds = elapsedSeconds,
                        onFinish = onBack
                    )
                } else if (questions.isNotEmpty()) {
                    val currentQuestion = questions[currentIndex]
                    MiuixQuizQuestionContent(
                        question = currentQuestion,
                        currentIndex = currentIndex,
                        totalQuestions = questions.size,
                        selectedOption = userAnswers[currentIndex],
                        onSelectOption = { userAnswers[currentIndex] = it },
                        onNext = {
                            if (currentIndex + 1 < questions.size) {
                                currentIndex += 1
                            } else {
                                submitExam()
                            }
                        },
                        isLast = currentIndex + 1 == questions.size
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
                                text = stringResource(Res.string.study_action_start_quiz),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (questions.isNotEmpty() && !isSubmitted) {
                                MaterialText(
                                    text = "${currentIndex + 1} / ${questions.size} • ${elapsedSeconds}s",
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
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        MaterialText(
                            text = stringResource(Res.string.study_ai_generating),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (isSubmitted) {
                    MaterialQuizResultsView(
                        questions = questions,
                        userAnswers = userAnswers,
                        elapsedSeconds = elapsedSeconds,
                        onFinish = onBack
                    )
                } else if (questions.isNotEmpty()) {
                    val currentQuestion = questions[currentIndex]
                    MaterialQuizQuestionContent(
                        question = currentQuestion,
                        currentIndex = currentIndex,
                        totalQuestions = questions.size,
                        selectedOption = userAnswers[currentIndex],
                        onSelectOption = { userAnswers[currentIndex] = it },
                        onNext = {
                            if (currentIndex + 1 < questions.size) {
                                currentIndex += 1
                            } else {
                                submitExam()
                            }
                        },
                        isLast = currentIndex + 1 == questions.size
                    )
                }
            }
        }
    }
}

@Composable
private fun MiuixQuizQuestionContent(
    question: QuizQuestion,
    currentIndex: Int,
    totalQuestions: Int,
    selectedOption: Int?,
    onSelectOption: (Int) -> Unit,
    onNext: () -> Unit,
    isLast: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(Res.string.study_quiz_question_counter, currentIndex + 1, totalQuestions),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                insideMargin = PaddingValues(16.dp),
                pressFeedbackType = PressFeedbackType.None
            ) {
                Text(
                    text = question.question,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(4.dp))

            question.options.forEachIndexed { optIdx, optText ->
                val isSelected = selectedOption == optIdx
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.15f) else MiuixTheme.colorScheme.surfaceContainer
                    ),
                    insideMargin = PaddingValues(14.dp),
                    pressFeedbackType = PressFeedbackType.Sink,
                    onClick = { onSelectOption(optIdx) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .squircleSurface(
                                    color = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainerHigh,
                                    cornerRadius = 6.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${('A'.code + optIdx).toChar()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = optText,
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        TextButton(
            text = if (isLast) stringResource(Res.string.study_quiz_finish) else "Siguiente",
            onClick = onNext,
            enabled = selectedOption != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColorsPrimary()
        )
    }
}

@Composable
private fun MiuixQuizResultsView(
    questions: List<QuizQuestion>,
    userAnswers: Map<Int, Int>,
    elapsedSeconds: Int,
    onFinish: () -> Unit
) {
    var correctCount = 0
    questions.forEachIndexed { idx, q ->
        if (userAnswers[idx] == q.correctOptionIndex) correctCount += 1
    }
    val percentage = if (questions.isNotEmpty()) ((correctCount.toFloat() / questions.size) * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                insideMargin = PaddingValues(20.dp),
                pressFeedbackType = PressFeedbackType.None
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.study_quiz_results_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.study_quiz_score, correctCount, questions.size, percentage),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (percentage >= 70) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.error
                    )
                    Text(
                        text = "Tiempo total: ${elapsedSeconds}s",
                        fontSize = 11.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }
        }

        itemsIndexed(questions) { idx, q ->
            val userAns = userAnswers[idx]
            val isCorrect = userAns == q.correctOptionIndex

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
                insideMargin = PaddingValues(14.dp),
                pressFeedbackType = PressFeedbackType.None
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pregunta ${idx + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isCorrect) Icons.Outlined.CheckCircle else Icons.Outlined.Close,
                            contentDescription = null,
                            tint = if (isCorrect) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = q.question,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MiuixTheme.colorScheme.onSurface
                    )

                    if (!isCorrect) {
                        val chosen = if (userAns != null && userAns in q.options.indices) q.options[userAns] else "Sin responder"
                        Text(
                            text = "Tu respuesta: $chosen",
                            fontSize = 11.sp,
                            color = MiuixTheme.colorScheme.error
                        )
                    }

                    val correctText = q.options.getOrNull(q.correctOptionIndex) ?: ""
                    Text(
                        text = "Correcta: $correctText",
                        fontSize = 11.sp,
                        color = MiuixTheme.colorScheme.primary
                    )

                    if (q.explanation.isNotBlank()) {
                        Text(
                            text = "${stringResource(Res.string.study_quiz_explanation)}: ${q.explanation}",
                            fontSize = 10.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            TextButton(
                text = "Volver al centro de estudio",
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColorsPrimary()
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MaterialQuizQuestionContent(
    question: QuizQuestion,
    currentIndex: Int,
    totalQuestions: Int,
    selectedOption: Int?,
    onSelectOption: (Int) -> Unit,
    onNext: () -> Unit,
    isLast: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MaterialText(
                text = stringResource(Res.string.study_quiz_question_counter, currentIndex + 1, totalQuestions),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                MaterialText(
                    text = question.question,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }

            question.options.forEachIndexed { optIdx, optText ->
                val isSelected = selectedOption == optIdx
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectOption(optIdx) },
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            MaterialText(
                                text = "${('A'.code + optIdx).toChar()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        MaterialText(
                            text = optText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        androidx.compose.material3.Button(
            onClick = onNext,
            enabled = selectedOption != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            MaterialText(if (isLast) stringResource(Res.string.study_quiz_finish) else "Siguiente")
        }
    }
}

@Composable
private fun MaterialQuizResultsView(
    questions: List<QuizQuestion>,
    userAnswers: Map<Int, Int>,
    elapsedSeconds: Int,
    onFinish: () -> Unit
) {
    var correctCount = 0
    questions.forEachIndexed { idx, q ->
        if (userAnswers[idx] == q.correctOptionIndex) correctCount += 1
    }
    val percentage = if (questions.isNotEmpty()) ((correctCount.toFloat() / questions.size) * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MaterialText(
                        text = stringResource(Res.string.study_quiz_results_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    MaterialText(
                        text = stringResource(Res.string.study_quiz_score, correctCount, questions.size, percentage),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (percentage >= 70) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    MaterialText(
                        text = "Tiempo total: ${elapsedSeconds}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        itemsIndexed(questions) { idx, q ->
            val userAns = userAnswers[idx]
            val isCorrect = userAns == q.correctOptionIndex

            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MaterialText(
                            text = "Pregunta ${idx + 1}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        androidx.compose.material3.Icon(
                            imageVector = if (isCorrect) Icons.Outlined.CheckCircle else Icons.Outlined.Close,
                            contentDescription = null,
                            tint = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    MaterialText(
                        text = q.question,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!isCorrect) {
                        val chosen = if (userAns != null && userAns in q.options.indices) q.options[userAns] else "Sin responder"
                        MaterialText(
                            text = "Tu respuesta: $chosen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    val correctText = q.options.getOrNull(q.correctOptionIndex) ?: ""
                    MaterialText(
                        text = "Correcta: $correctText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (q.explanation.isNotBlank()) {
                        MaterialText(
                            text = "${stringResource(Res.string.study_quiz_explanation)}: ${q.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            ) {
                MaterialText("Volver al centro de estudio")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
