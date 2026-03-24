package com.quizzer.app.domain

import com.quizzer.app.model.QuizConfig

/**
 * Constructs the prompt string sent to the on-device model.
 *
 * Injected via Hilt so it can be faked in tests without touching AI infrastructure.
 */
interface PromptBuilder {

    /**
     * Builds a fully-formed prompt from the given [excerpt].
     *
     * The returned string must instruct the model to:
     * - Return ONLY a JSON array (no markdown fences, no explanation text).
     * - Ground every question and answer solely in the excerpt.
     * - Include a sourceReference matching a heading or paragraph opening in the excerpt.
     * - Respect [config.questionCount] and [config.questionTypes].
     */
    fun build(excerpt: String, config: QuizConfig): String
}
