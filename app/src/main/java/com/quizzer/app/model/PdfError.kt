package com.quizzer.app.model

/**
 * All typed failure cases surfaced by [com.quizzer.app.domain.PdfParser].
 */
sealed class PdfError : Exception() {

    /** The PDF is password-protected and cannot be read without a password. */
    data object PasswordProtected : PdfError()

    /** The file is corrupt or not a valid PDF. */
    data class ParseFailure(override val cause: Throwable? = null) : PdfError()

    /** The URI could not be opened (file deleted, permission revoked, etc.). */
    data class FileNotAccessible(override val cause: Throwable? = null) : PdfError()

    /** Any other unhandled error. */
    data class Unknown(override val cause: Throwable) : PdfError()
}
