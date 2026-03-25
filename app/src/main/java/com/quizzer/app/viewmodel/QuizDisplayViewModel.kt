package com.quizzer.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizzer.app.model.QuizQuestion
import com.quizzer.app.model.UserAnswer
import com.quizzer.app.ui.QuizDisplayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Quiz Display screen (F3).
 *
 * Tracks the currently shown question, the user's in-progress selection, and
 * the accumulated list of submitted answers.  When the last answer is submitted
 * it fires a [navigateToScore] event.
 *
 * [start] is idempotent: repeated calls with different lists are silently ignored
 * after the first successful call, preventing double-initialisation from recomposition.
 */
@HiltViewModel
class QuizDisplayViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(QuizDisplayUiState())
    val uiState: StateFlow<QuizDisplayUiState> = _uiState.asStateFlow()

    private val _navigateToScore = Channel<List<UserAnswer>>(Channel.BUFFERED)

    /** One-shot event: fires when every answer has been submitted. */
    val navigateToScore: Flow<List<UserAnswer>> = _navigateToScore.receiveAsFlow()

    private val _collectedAnswers = mutableListOf<UserAnswer>()

    /**
     * The list of answers collected so far, read by [AppNavHost] to seed the Score screen.
     *
     * This is a snapshot list; callers should read it only after [navigateToScore] fires.
     */
    val collectedAnswers: List<UserAnswer> get() = _collectedAnswers.toList()

    /**
     * Initialises the quiz with [questions].
     *
     * Ignored if the quiz has already been started (idempotent).
     *
     * @param partialCount If non-null, the AI returned fewer questions than requested;
     *   the screen may show a non-blocking notice.
     */
    fun start(questions: List<QuizQuestion>, partialCount: Int? = null) {
        if (_uiState.value.questions.isNotEmpty()) return
        require(questions.isNotEmpty()) { "questions must not be empty" }
        _uiState.value = _uiState.value.copy(
            questions = questions,
            partialCount = partialCount,
        )
    }

    /**
     * Called when the user taps an answer option.
     *
     * Ignored after the current question has been submitted (AC7).
     */
    fun onAnswerSelected(option: String) {
        if (_uiState.value.isSubmitted) return
        _uiState.value = _uiState.value.copy(selectedAnswer = option)
    }

    /**
     * Called when the user taps the Submit button.
     *
     * Records the answer (null selection is allowed — counts as wrong per AC3) and
     * marks the question as submitted so the screen can show feedback (AC4–AC6).
     * Idempotent: successive calls while already submitted are ignored.
     */
    fun onSubmitClicked() {
        val state = _uiState.value
        if (state.isSubmitted) return
        val question = state.currentQuestion ?: return

        _collectedAnswers.add(
            UserAnswer(
                questionId = question.id,
                selectedOption = state.selectedAnswer,
                correctAnswer = question.answer,
            ),
        )
        _uiState.value = state.copy(isSubmitted = true)
    }

    /**
     * Called when the user taps the Next button (visible only after submission).
     *
     * Advances to the next question or, if this was the last question, emits the
     * full answer list via [navigateToScore].
     */
    fun onNextClicked() {
        val state = _uiState.value
        if (!state.isSubmitted) return

        if (state.isLastQuestion) {
            viewModelScope.launch {
                _navigateToScore.send(_collectedAnswers.toList())
            }
        } else {
            _uiState.value = state.copy(
                currentIndex = state.currentIndex + 1,
                selectedAnswer = null,
                isSubmitted = false,
            )
        }
    }
}
