package com.quizzer.app.model

/**
 * Outcome of a [com.quizzer.app.domain.PdfParser.extractText] call.
 */
sealed class PdfResult {

    /** Successful extraction. [text] may be blank if the PDF has no text layer. */
    data class Success(val text: String) : PdfResult()

    /** A typed failure; [error] describes the cause. */
    data class Failure(val error: PdfError) : PdfResult()
}
