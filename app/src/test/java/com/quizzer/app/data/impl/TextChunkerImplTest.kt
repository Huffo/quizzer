package com.quizzer.app.data.impl

import com.quizzer.app.data.TextChunker
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TextChunkerImplTest {

    private val chunker = TextChunkerImpl()

    /** Builds a multi-paragraph text with [paragraphCount] paragraphs of [wordsEach] words. */
    private fun buildText(paragraphCount: Int, wordsEach: Int): String =
        (1..paragraphCount).joinToString("\n\n") { p ->
            (1..wordsEach).joinToString(" ") { "para${p}word$it" }
        }

    @Test
    fun `randomExcerpt returns non-blank text for valid input`() {
        val text = buildText(paragraphCount = 5, wordsEach = 30)
        val result = chunker.randomExcerpt(text, seed = 42L)
        assertTrue(result.isNotBlank())
    }

    @Test
    fun `randomExcerpt is deterministic for the same seed`() {
        val text = buildText(paragraphCount = 10, wordsEach = 40)
        val first = chunker.randomExcerpt(text, seed = 99L)
        val second = chunker.randomExcerpt(text, seed = 99L)
        assertTrue(first == second)
    }

    @Test
    fun `randomExcerpt differs for different seeds`() {
        val text = buildText(paragraphCount = 20, wordsEach = 50)
        val a = chunker.randomExcerpt(text, seed = 1L)
        val b = chunker.randomExcerpt(text, seed = 2L)
        // With enough paragraphs, two different seeds produce different orderings.
        assertFalse(a == b)
    }

    @Test
    fun `randomExcerpt stays within MAX_CHUNK_TOKENS`() {
        // Build text large enough to exceed the token cap.
        val text = buildText(paragraphCount = 100, wordsEach = 60)
        val result = chunker.randomExcerpt(text, seed = 7L)
        val estimatedTokens = TextChunkerImpl.estimateTokens(result)
        assertTrue(estimatedTokens <= TextChunker.MAX_CHUNK_TOKENS) {
            "Expected estimated tokens <= ${TextChunker.MAX_CHUNK_TOKENS} but was $estimatedTokens"
        }
    }

    @Test
    fun `randomExcerpt handles single-paragraph input`() {
        val singleParagraph = "word ".repeat(50).trim()
        val result = chunker.randomExcerpt(singleParagraph, seed = 0L)
        assertTrue(result.isNotBlank())
    }

    @Test
    fun `estimateTokens returns zero for blank string`() {
        assertTrue(TextChunkerImpl.estimateTokens("") == 0)
    }

    @Test
    fun `estimateTokens scales with word count`() {
        val tenWords = "one two three four five six seven eight nine ten"
        val tokens = TextChunkerImpl.estimateTokens(tenWords)
        // 10 words × 1.33 = 13 (int truncation)
        assertTrue(tokens in 10..15) {
            "Expected token estimate in 10..15 but was $tokens"
        }
    }
}
