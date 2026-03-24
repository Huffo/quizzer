package com.quizzer.app.data.impl

import com.quizzer.app.domain.PromptBuilder
import com.quizzer.app.model.QuizConfig
import com.quizzer.app.model.QuestionType
import javax.inject.Inject

/**
 * Production implementation of [PromptBuilder].
 *
 * TODO(F2): Refine prompt wording based on QuizConfig question types and count.
 */
class PromptBuilderImpl @Inject constructor() : PromptBuilder {

    override fun build(excerpt: String, config: QuizConfig): String {
        val typesDescription = buildTypesDescription(config.questionTypes)
        return """
            Generate ${config.questionCount} quiz questions based solely on the following text.
            Use only $typesDescription questions.
            Every question, answer, and explanation must be verifiable from the text below.
            Include a sourceReference field matching the nearest section heading or paragraph opening.
            Return ONLY a JSON array with no markdown, no extra text:
            [{"id":"<uuid>","type":"MULTIPLE_CHOICE|TRUE_FALSE","question":"...","options":["A) ...","B) ...","C) ...","D) ..."],"answer":"A) ...","explanation":"...","sourceReference":"..."}]
            For TRUE_FALSE questions use options ["True","False"] only.
            
            Text:
            $excerpt
        """.trimIndent()
    }

    private fun buildTypesDescription(types: Set<QuestionType>): String = when {
        types.containsAll(QuestionType.entries) -> "multiple-choice and true/false"
        types.contains(QuestionType.MULTIPLE_CHOICE) -> "multiple-choice"
        else -> "true/false"
    }
}
