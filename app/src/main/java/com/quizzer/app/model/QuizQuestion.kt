package com.quizzer.app.model

/**
 * A single quiz question generated from user-supplied text.
 *
 * Invariants (enforced at the parse/validation boundary in the data layer):
 * - [id] is a non-blank UUID assigned at parse time.
 * - [options].size == 4 when [type] == [QuestionType.MULTIPLE_CHOICE].
 * - [options] == ["True", "False"] when [type] == [QuestionType.TRUE_FALSE].
 * - [answer] is exactly equal to one element of [options].
 * - [question], [answer], and [explanation] are grounded solely in the source text.
 * - [sourceReference] is non-blank: a section heading if one exists, otherwise the
 *   opening sentence of the nearest paragraph.
 */
data class QuizQuestion(
    val id: String,
    val type: QuestionType,
    val question: String,
    val options: List<String>,
    val answer: String,
    val explanation: String,
    val sourceReference: String,
)
