package com.quizzer.app.domain

import android.net.Uri

/**
 * Extracts plain text from a user-supplied PDF file.
 *
 * The implementation runs on a background dispatcher and is fully on-device.
 * Image-only (scanned) PDFs are not an error — they will return blank or very
 * short text, which the caller handles via word-count validation.
 *
 * @see com.quizzer.app.model.PdfError for all typed failure cases.
 */
interface PdfParser {

    /**
     * Extracts and returns the full text content of the PDF at [uri].
     *
     * Never throws; all failures are wrapped in [PdfResult.Failure].
     */
    suspend fun extractText(uri: Uri): PdfResult
}
