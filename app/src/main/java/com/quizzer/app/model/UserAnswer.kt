package com.quizzer.app.model

import kotlinx.serialization.Serializable

/**
 * Records the user's response to a single quiz question.
 *
 * [selectedOption] — the option the user chose; null if no option was selected before Submit.
 * [correctAnswer]  — the correct option string from [QuizQuestion.answer].
 */
@Serializable
data class UserAnswer(
    val questionId: String,
    val selectedOption: String?,
    val correctAnswer: String,
) {
    /** True when [selectedOption] exactly matches [correctAnswer]. */
    val isCorrect: Boolean get() = selectedOption == correctAnswer
}
