package me.joxquin.notivas.ui.dashboard.draft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.joxquin.notivas.data.model.Assignment
import me.joxquin.notivas.di.AppModule
import me.joxquin.notivas.domain.review.AssignmentPreReviewReport
import me.joxquin.notivas.domain.review.AssignmentReviewUseCase
import me.joxquin.notivas.domain.review.ReviewOptions
import me.joxquin.notivas.domain.review.ReviewProgressStep
import me.joxquin.notivas.util.SelectedDocument

data class AssignmentPreReviewUiState(
    val options: ReviewOptions = ReviewOptions(),
    val isLoading: Boolean = false,
    val progressStep: ReviewProgressStep = ReviewProgressStep.IDLE,
    val progressMessage: String = "",
    val report: AssignmentPreReviewReport? = null,
    val error: Throwable? = null
)

class AssignmentPreReviewViewModel(
    private val reviewUseCase: () -> AssignmentReviewUseCase = { AppModule.assignmentReviewUseCase }
) : ViewModel() {
    private val _uiState = MutableStateFlow(AssignmentPreReviewUiState())
    val uiState: StateFlow<AssignmentPreReviewUiState> = _uiState.asStateFlow()

    private var reviewJob: Job? = null

    fun updateOptions(options: ReviewOptions) {
        _uiState.update { it.copy(options = options) }
    }

    fun startReview(
        assignment: Assignment,
        selectedDocument: SelectedDocument?,
        draftFileId: String?,
        textBody: String?
    ) {
        if (_uiState.value.isLoading) return

        reviewJob?.cancel()
        val options = _uiState.value.options
        _uiState.update {
            it.copy(isLoading = true, error = null, progressStep = ReviewProgressStep.READING_RUBRIC)
        }

        reviewJob = viewModelScope.launch {
            val result = reviewUseCase().reviewAssignment(
                assignment = assignment,
                selectedDocument = selectedDocument,
                draftFileId = draftFileId,
                textBody = textBody,
                options = options,
                onProgress = { step, message ->
                    launch {
                        _uiState.update { state ->
                            if (state.isLoading) {
                                state.copy(progressStep = step, progressMessage = message)
                            } else {
                                state
                            }
                        }
                    }
                }
            )
            ensureActive()
            result.fold(
                onSuccess = { report -> _uiState.update { it.copy(report = report, isLoading = false) } },
                onFailure = { error -> _uiState.update { it.copy(error = error, isLoading = false) } }
            )
        }
    }

    fun resetReview() {
        reviewJob?.cancel()
        reviewJob = null
        _uiState.update { it.copy(report = null, error = null, isLoading = false) }
    }
}
