package com.quizzer.app.ui

import com.quizzer.app.model.QuizQuestion

/**
 * UI state for the Quiz Display screen.
 *
 * An empty [questions] list means the quiz has not yet been started
 * (awaiting delivery from the Generation back-stack entry).
 *
 * @param questions The full ordered list of questions in this quiz.
 * @param currentIndex Zero-based index of the question currently shown.
 * @param selectedAnswer The option string the user has tapped, or null if none yet.
 * @param isSubmitted True once the user has submitted the current question's answer.
 *   While true, options are locked and the feedback / explanation are shown (F4 AC4–AC7).
 *   Reset to false when [onNextClicked] advances to the next question.
 * @param partialCount If non-null, the AI returned fewer questions than requested;
 *   [partialCount] is the number that was originally requested.
 */
data class QuizDisplayUiState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: String? = null,
    val isSubmitted: Boolean = false,
    val partialCount: Int? = null,
) {
    /** The question currently shown; null only when [questions] is empty. */
    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentIndex)

    /** Total number of questions in the quiz. */
    val totalCount: Int
        get() = questions.size

    /** True when [currentIndex] points to the last question in the list. */
    val isLastQuestion: Boolean
        get() = questions.isNotEmpty() && currentIndex == questions.size - 1
}
