package com.quizzer.app.ui

import com.quizzer.app.model.QuizError
import com.quizzer.app.model.QuizQuestion

/**
 * Complete UI state for the Quiz Generation screen.
 *
 * Transitions: [Loading] → [Success] | [PartialSuccess] | [Error]
 * Back-press on [Loading] should cancel and pop; the [TextInput] screen in the
 * back stack retains all PDF state via its own [TextInputViewModel].
 */
sealed interface QuizGenerationUiState {

    /** Generation is in progress; the UI shows a spinner and blocks input. */
    data object Loading : QuizGenerationUiState

    /**
     * Generation succeeded and the requested number of questions was returned.
     * The caller should navigate to the quiz screen immediately.
     */
    data class Success(val questions: List<QuizQuestion>) : QuizGenerationUiState

    /**
     * Generation succeeded but fewer questions than requested were returned.
     * The caller navigates to the quiz screen and shows a non-blocking notice.
     */
    data class PartialSuccess(
        val questions: List<QuizQuestion>,
        val requestedCount: Int,
    ) : QuizGenerationUiState

    /** Generation failed; [error] drives the error message shown to the user. */
    data class Error(val error: QuizError) : QuizGenerationUiState
}
