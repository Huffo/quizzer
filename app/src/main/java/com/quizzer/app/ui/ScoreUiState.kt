package com.quizzer.app.ui

import com.quizzer.app.model.QuizQuestion
import com.quizzer.app.model.UserAnswer

/**
 * Immutable UI state for the Score screen (F5).
 *
 * @param questions The full ordered list of questions in the completed quiz.
 * @param answers   The list of [UserAnswer]s recorded during the quiz, in the same order.
 */
data class ScoreUiState(
    val questions: List<QuizQuestion> = emptyList(),
    val answers: List<UserAnswer> = emptyList(),
) {
    /** Number of answers where [UserAnswer.isCorrect] is true. */
    val correctCount: Int
        get() = answers.count { it.isCorrect }

    /** Total number of questions (denominator for the score). */
    val totalCount: Int
        get() = questions.size

    /**
     * Per-question breakdown rows, aligned by index.
     *
     * Each row contains the [QuizQuestion] and the corresponding [UserAnswer].
     * If (for any reason) an answer is missing it is treated as unanswered.
     */
    val breakdownRows: List<ScoreRow>
        get() = questions.mapIndexed { index, question ->
            val answer = answers.getOrNull(index)
            ScoreRow(question = question, answer = answer)
        }
}

/**
 * A single row in the per-question breakdown.
 *
 * [answer] is null if the question was not reached — treated as incorrect (AC4).
 */
data class ScoreRow(
    val question: QuizQuestion,
    val answer: UserAnswer?,
) {
    /** True when the user selected the correct answer for this question. */
    val isCorrect: Boolean get() = answer?.isCorrect == true
}
