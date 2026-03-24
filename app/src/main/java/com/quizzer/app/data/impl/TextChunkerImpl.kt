package com.quizzer.app.data.impl

import com.quizzer.app.data.TextChunker
import javax.inject.Inject
import kotlin.random.Random

/**
 * Production implementation of [TextChunker].
 *
 * Strategy:
 * 1. Split the input into paragraphs (blank-line boundaries).
 * 2. Walk paragraphs in seeded-random order, accumulating tokens until
 *    [TextChunker.MAX_CHUNK_TOKENS] would be exceeded.
 * 3. Return the accumulated excerpt.
 *
 * Token estimation: 1 word ≈ 1.33 tokens (conservative; keeps well within
 * Gemini Nano's 4 096 token context window including prompt overhead).
 */
class TextChunkerImpl @Inject constructor() : TextChunker {

    override fun randomExcerpt(text: String, seed: Long): String {
        val paragraphs = text
            .split(Regex("\\n{2,}"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val rng = Random(seed)
        val shuffled = paragraphs.shuffled(rng)

        val excerptBuilder = StringBuilder()
        var tokenCount = 0

        for (paragraph in shuffled) {
            val paragraphTokens = estimateTokens(paragraph)
            if (tokenCount + paragraphTokens > TextChunker.MAX_CHUNK_TOKENS) break
            if (excerptBuilder.isNotEmpty()) excerptBuilder.append("\n\n")
            excerptBuilder.append(paragraph)
            tokenCount += paragraphTokens
        }

        return excerptBuilder.toString()
    }

    companion object {
        private const val TOKENS_PER_WORD = 1.33f

        /** Rough word-to-token estimate; errs on the side of under-filling the window. */
        fun estimateTokens(text: String): Int =
            (text.trim().split(Regex("\\s+")).count { it.isNotEmpty() } * TOKENS_PER_WORD).toInt()
    }
}

