package com.quizzer.app.data

import android.net.Uri
import com.quizzer.app.domain.PdfParser
import com.quizzer.app.model.PdfResult
import javax.inject.Inject

/**
 * Test/debug stand-in for [PdfParser].
 *
 * Returns a configurable [response] without touching the file system.
 * [lastUri] lets tests assert which URI was passed.
 */
class FakePdfParser @Inject constructor() : PdfParser {

    var response: PdfResult = PdfResult.Success(FAKE_TEXT)

    var lastUri: Uri? = null
        private set

    override suspend fun extractText(uri: Uri): PdfResult {
        lastUri = uri
        return response
    }

    companion object {
        /** Enough words to pass the 100-word minimum check. */
        val FAKE_TEXT: String = "Sample word ".repeat(110).trim()
    }
}
