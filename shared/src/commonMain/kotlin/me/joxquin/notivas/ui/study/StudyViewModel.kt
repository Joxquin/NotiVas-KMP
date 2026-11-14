package me.joxquin.notivas.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.joxquin.notivas.domain.study.generator.StudyAiGenerator
import me.joxquin.notivas.domain.study.model.Flashcard
import me.joxquin.notivas.domain.study.model.FsrsRating
import me.joxquin.notivas.domain.study.model.QuizQuestion
import me.joxquin.notivas.domain.study.model.QuizResult
import me.joxquin.notivas.domain.study.model.StudyDeck
import me.joxquin.notivas.domain.study.model.StudyOverviewStats
import me.joxquin.notivas.domain.study.repository.StudyRepository
import me.joxquin.notivas.util.DateTimeUtil

data class StudyHubUiState(
    val decks: List<StudyDeck> = emptyList(),
    val dueCardsCount: Int = 0,
    val totalCardsCount: Int = 0,
    val isGeneratingAi: Boolean = false,
    val aiGenerationError: String? = null
)

class StudyViewModel(
    private val studyRepository: StudyRepository,
    private val studyAiGenerator: StudyAiGenerator
) : ViewModel() {

    val decks: StateFlow<List<StudyDeck>> = studyRepository.observeAllDecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overviewStats: StateFlow<StudyOverviewStats> = studyRepository.observeOverviewStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudyOverviewStats())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationMessage = MutableStateFlow<String?>(null)
    val generationMessage: StateFlow<String?> = _generationMessage.asStateFlow()

    fun createDeck(
        title: String,
        description: String = "",
        courseId: Long? = null,
        courseName: String? = null,
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = studyRepository.createDeck(title, description, courseId, courseName)
            onSuccess(id)
        }
    }

    fun deleteDeck(deckId: Long) {
        viewModelScope.launch {
            studyRepository.deleteDeck(deckId)
        }
    }

    fun generateDeckWithAi(
        topicOrContext: String,
        deckTitle: String,
        courseId: Long? = null,
        courseName: String? = null,
        cardCount: Int = 6,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationMessage.value = null
            val result = studyAiGenerator.generateFlashcards(topicOrContext, cardCount)
            result.onSuccess { cards ->
                val deckId = studyRepository.createDeck(deckTitle, "Generado con IA sobre $deckTitle", courseId, courseName)
                studyRepository.addCards(deckId, cards)
                _isGenerating.value = false
                _generationMessage.value = "Tarjetas creadas exitosamente"
                onComplete(deckId)
            }.onFailure { error ->
                _isGenerating.value = false
                _generationMessage.value = error.message ?: "Error al generar tarjetas"
            }
        }
    }

    fun addManualCard(
        deckId: Long,
        front: String,
        back: String,
        hint: String? = null,
        topic: String? = null
    ) {
        viewModelScope.launch {
            studyRepository.addCard(deckId, front, back, hint, topic)
        }
    }

    suspend fun getCardsForDeck(deckId: Long): List<Flashcard> {
        return studyRepository.getCardsByDeck(deckId)
    }

    suspend fun getDeck(deckId: Long): StudyDeck? {
        return studyRepository.getDeckById(deckId)
    }

    fun reviewCard(card: Flashcard, rating: FsrsRating) {
        viewModelScope.launch {
            studyRepository.reviewCard(card, rating)
        }
    }

    suspend fun generateQuizForDeck(deck: StudyDeck, count: Int = 5): Result<List<QuizQuestion>> {
        val cards = studyRepository.getCardsByDeck(deck.id)
        val context = if (cards.isNotEmpty()) {
            cards.joinToString("\n") { "Concepto: ${it.front} -> ${it.back}" }
        } else {
            deck.title + " " + deck.description
        }
        return studyAiGenerator.generateQuiz(context, count)
    }

    fun saveQuizResult(result: QuizResult) {
        viewModelScope.launch {
            studyRepository.recordQuizResult(result)
        }
    }
}
