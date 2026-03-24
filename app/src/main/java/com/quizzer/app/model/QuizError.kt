package com.quizzer.app.model

/**
 * All typed failure cases surfaced as [Result.Failure] throughout the app.
 * Every error path must use a subtype of this class — never a raw [Exception].
 */
sealed class QuizError : Exception() {

    /** AICore reports the on-device model is not downloaded or not supported. */
    data class ModelUnavailable(val statusMessage: String) : QuizError()

    /**
     * The model returned a response that could not be parsed into a valid quiz.
     * [raw] contains the unmodified model output to aid debugging.
     */
    data class ParseFailure(val raw: String, override val cause: Throwable? = null) : QuizError()

    /** Input text is blank or below [com.quizzer.app.data.TextChunker.MIN_INPUT_WORDS] words. */
    data object InputTooShort : QuizError()

    /** An unexpected error occurred; [cause] is the original exception. */
    data class Unknown(override val cause: Throwable) : QuizError()
}
