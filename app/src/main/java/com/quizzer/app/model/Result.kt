package com.quizzer.app.model

/**
 * Discriminated union representing the outcome of a suspending operation.
 *
 * All public APIs that can fail must return [Result] rather than throwing.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val error: QuizError) : Result<Nothing>()
}
