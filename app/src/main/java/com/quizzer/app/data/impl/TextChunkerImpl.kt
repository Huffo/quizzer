package com.quizzer.app.data.impl

import com.quizzer.app.data.TextChunker
import javax.inject.Inject
import kotlin.random.Random

/**
 * Production implementation of [TextChunker].
 *
 * TODO(F2): Implement word-count validation and token-aware chunking.
 */
class TextChunkerImpl @Inject constructor() : TextChunker {

    override fun randomExcerpt(text: String, seed: Long): String {
        TODO("Implement in F2: split text into token-capped chunks and return a seeded-random excerpt")
    }
}
