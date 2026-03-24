package com.quizzer.app.data

import kotlin.random.Random

/**
 * Selects a random excerpt from user-supplied text for use in prompt construction.
 *
 * Injected via Hilt so tests can inject a deterministic fake without touching the real impl.
 */
interface TextChunker {

    /**
     * Returns a random excerpt from [text] that fits within [MAX_CHUNK_TOKENS].
     *
     * @param text  The full source text. Must contain at least [MIN_INPUT_WORDS] words.
     * @param seed  Random seed; pass a fixed value in tests for deterministic output.
     */
    fun randomExcerpt(text: String, seed: Long = Random.nextLong()): String

    companion object {
        /** Minimum word count required before chunking is attempted. */
        const val MIN_INPUT_WORDS: Int = 100

        /** Maximum approximate token budget for a single excerpt sent to the model. */
        const val MAX_CHUNK_TOKENS: Int = 3_000
    }
}
