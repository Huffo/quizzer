package com.quizzer.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizzer.app.domain.QuizGeneratorRepository
import com.quizzer.app.model.QuizConfig
import com.quizzer.app.model.Result
import com.quizzer.app.ui.QuizGenerationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizGenerationViewModel @Inject constructor(
    private val repository: QuizGeneratorRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizGenerationUiState>(QuizGenerationUiState.Loading)
    val uiState: StateFlow<QuizGenerationUiState> = _uiState.asStateFlow()

    private var generationJob: Job? = null

    /**
     * Starts quiz generation.
     * A second call while [QuizGenerationUiState.Loading] is active is ignored (AC6).
     */
    fun generateQuiz(text: String, config: QuizConfig) {
        if (_uiState.value is QuizGenerationUiState.Loading && generationJob?.isActive == true) return

        generationJob = viewModelScope.launch {
            _uiState.value = QuizGenerationUiState.Loading
            when (val result = repository.generateQuiz(text, config)) {
                is Result.Success -> {
                    val questions = result.data
                    _uiState.value = if (questions.size < config.questionCount) {
                        QuizGenerationUiState.PartialSuccess(
                            questions = questions,
                            requestedCount = config.questionCount,
                        )
                    } else {
                        QuizGenerationUiState.Success(questions)
                    }
                }
                is Result.Failure -> {
                    _uiState.value = QuizGenerationUiState.Error(result.error)
                }
            }
        }
    }

    /**
     * Cancels any in-progress generation (AC7 — back-press during loading).
     * State is reset to [QuizGenerationUiState.Loading] so the screen can be
     * dismissed cleanly; the caller should pop the back stack after calling this.
     */
    fun cancelGeneration() {
        generationJob?.cancel()
        generationJob = null
        _uiState.value = QuizGenerationUiState.Loading
    }
}
