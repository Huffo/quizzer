package com.quizzer.app.domain

import com.quizzer.app.model.QuizConfig
import com.quizzer.app.model.QuizQuestion
import com.quizzer.app.model.Result

/**
 * Entry point for quiz generation. Abstracts all AI model interaction so that
 * ViewModels and use-cases never depend on AICore directly.
 *
 * Contract:
 * - [generateQuiz] never throws; all failures are returned as [Result.Failure].
 * - A [Result.Success] always contains at least one [QuizQuestion].
 * - The implementation is fully on-device; no network calls are made.
 */
interface QuizGeneratorRepository {

    /**
     * Generates a quiz from the provided [text].
     *
     * @param text  The full user-supplied source text. Must not be blank.
     * @param config Quiz generation options.
     * @return [Result.Success] with a non-empty list of [QuizQuestion], or
     *         [Result.Failure] wrapping a [com.quizzer.app.model.QuizError].
     */
    suspend fun generateQuiz(
        text: String,
        config: QuizConfig = QuizConfig(),
    ): Result<List<QuizQuestion>>
}
