package com.quizzer.app.data.impl

import com.quizzer.app.domain.QuizGeneratorRepository
import com.quizzer.app.model.QuizConfig
import com.quizzer.app.model.QuizQuestion
import com.quizzer.app.model.Result
import javax.inject.Inject

/**
 * Production implementation of [QuizGeneratorRepository] backed by AICore (Gemini Nano).
 *
 * TODO(F2): Implement full AICore integration:
 *   1. Check model availability via GenerativeModel.checkAvailability()
 *   2. Call TextChunker.randomExcerpt() to select an excerpt
 *   3. Call PromptBuilder.build() to construct the prompt
 *   4. Send to GenerativeModel and collect the streamed response
 *   5. Parse the JSON response into List<QuizQuestion> via Kotlinx Serialization
 *   6. Surface all errors as typed Result.Failure(QuizError.*)
 */
class AiCoreQuizGeneratorRepository @Inject constructor() : QuizGeneratorRepository {

    override suspend fun generateQuiz(
        text: String,
        config: QuizConfig,
    ): Result<List<QuizQuestion>> {
        TODO("Implement in F2: AICore availability check → TextChunker → PromptBuilder → model call → parse")
    }
}
