package com.quizzer.app.viewmodel

import androidx.lifecycle.ViewModel
import com.quizzer.app.model.QuizQuestion
import com.quizzer.app.model.UserAnswer
import com.quizzer.app.ui.ScoreUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for the Score screen (F5).
 *
 * Accepts the completed list of [QuizQuestion]s and the user's [UserAnswer]s, and
 * exposes a [ScoreUiState] containing the score and per-question breakdown.
 *
 * [initialise] is idempotent: successive calls after the first are silently ignored
 * to prevent double-initialisation from recomposition.
 */
@HiltViewModel
class ScoreViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ScoreUiState())
    val uiState: StateFlow<ScoreUiState> = _uiState.asStateFlow()

    /**
     * Seeds the score screen with the results of the completed quiz.
     *
     * Ignored if already initialised (idempotent).
     *
     * @param questions Original question list from the generator.
     * @param answers   Answers collected by [QuizDisplayViewModel] in submission order.
     */
    fun initialise(questions: List<QuizQuestion>, answers: List<UserAnswer>) {
        if (_uiState.value.questions.isNotEmpty()) return
        _uiState.value = ScoreUiState(questions = questions, answers = answers)
    }
}
