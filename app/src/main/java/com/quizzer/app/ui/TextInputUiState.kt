package com.quizzer.app.ui

import com.quizzer.app.data.TextChunker
import com.quizzer.app.model.PdfError
import com.quizzer.app.model.QuestionType
import com.quizzer.app.model.QuizConfig

/**
 * Complete UI state for the Text Input screen.
 *
 * [isGenerateEnabled] is computed: a PDF must be loaded with at least
 * [TextChunker.MIN_INPUT_WORDS] words of extractable text.
 */
data class TextInputUiState(
    val pdfState: PdfState = PdfState.Idle,
    val questionCount: Int = QuizConfig.DEFAULT_QUESTION_COUNT,
    val selectedTypes: Set<QuestionType> = QuestionType.entries.toSet(),
) {
    val isGenerateEnabled: Boolean
        get() = pdfState is PdfState.Ready &&
            pdfState.wordCount >= TextChunker.MIN_INPUT_WORDS
}

/** Describes the lifecycle of the PDF loading and parsing pipeline. */
sealed interface PdfState {

    /** No file has been selected yet. */
    data object Idle : PdfState

    /** A file has been selected; [PdfParser.extractText] is running. */
    data object Parsing : PdfState

    /**
     * Parsing completed successfully.
     * [text] is the full extracted content; [wordCount] may be below the minimum.
     */
    data class Ready(val text: String, val wordCount: Int) : PdfState

    /** Parsing failed; [error] carries the typed reason. */
    data class ParseError(val error: PdfError) : PdfState
}
