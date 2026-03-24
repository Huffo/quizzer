package com.quizzer.app.model

/**
 * Configuration for a quiz generation request.
 *
 * @param questionCount Number of questions to request; must be in [1..MAX_QUESTION_COUNT].
 *                      The model may return fewer if the text does not contain enough
 *                      verifiable content (partial success — not an error).
 * @param questionTypes Set of [QuestionType]s to include; must be non-empty.
 */
data class QuizConfig(
    val questionCount: Int = DEFAULT_QUESTION_COUNT,
    val questionTypes: Set<QuestionType> = QuestionType.entries.toSet(),
) {
    init {
        require(questionCount in MIN_QUESTION_COUNT..MAX_QUESTION_COUNT) {
            "questionCount must be in $MIN_QUESTION_COUNT..$MAX_QUESTION_COUNT but was $questionCount"
        }
        require(questionTypes.isNotEmpty()) {
            "questionTypes must not be empty"
        }
    }

    companion object {
        const val DEFAULT_QUESTION_COUNT: Int = 5
        const val MIN_QUESTION_COUNT: Int = 1
        const val MAX_QUESTION_COUNT: Int = 20
    }
}
