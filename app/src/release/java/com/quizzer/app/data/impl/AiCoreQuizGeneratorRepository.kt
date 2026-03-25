package com.quizzer.app.data.impl

import com.google.ai.edge.aicore.GenerativeAIException
import com.google.ai.edge.aicore.GenerativeModel
import com.quizzer.app.data.TextChunker
import com.quizzer.app.domain.PromptBuilder
import com.quizzer.app.domain.QuizGeneratorRepository
import com.quizzer.app.model.QuestionType
import com.quizzer.app.model.QuizConfig
import com.quizzer.app.model.QuizError
import com.quizzer.app.model.QuizQuestion
import com.quizzer.app.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

/**
 * Production implementation of [QuizGeneratorRepository] backed by AICore (Gemini Nano).
 *
 * Pipeline:
 * 1. Guard with [GenerativeModel.prepareInferenceEngine] → [QuizError.ModelUnavailable] on failure.
 * 2. Extract a token-capped excerpt via [TextChunker.randomExcerpt].
 * 3. Build the prompt via [PromptBuilder.build].
 * 4. Send to [GenerativeModel] and collect the response.
 * 5. Parse the JSON array → [List<QuizQuestion>] via Kotlinx Serialization.
 * 6. Surface all failures as typed [Result.Failure].
 */
class AiCoreQuizGeneratorRepository @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val textChunker: TextChunker,
    private val promptBuilder: PromptBuilder,
) : QuizGeneratorRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generateQuiz(
        text: String,
        config: QuizConfig,
    ): Result<List<QuizQuestion>> = withContext(Dispatchers.IO) {
        try {
            generativeModel.prepareInferenceEngine()
            val excerpt = textChunker.randomExcerpt(text)
            val prompt = promptBuilder.build(excerpt, config)
            val response = generativeModel.generateContent(prompt)
            val raw = response.text
                ?: return@withContext Result.Failure(
                    QuizError.ParseFailure(raw = "", cause = null),
                )
            parseResponse(raw, config)
        } catch (e: Exception) {
            val aiCoreCode = (e as? GenerativeAIException)?.errorCode ?: UNKNOWN_ERROR_CODE
            val isModelUnavailable = aiCoreCode == ERROR_CODE_NOT_AVAILABLE
                || e.message?.contains("NOT_AVAILABLE") == true
            if (isModelUnavailable) {
                Result.Failure(QuizError.ModelUnavailable(statusMessage = e.message ?: "NOT_AVAILABLE"))
            } else {
                Result.Failure(QuizError.Unknown(cause = e))
            }
        }
    }

    private fun parseResponse(raw: String, config: QuizConfig): Result<List<QuizQuestion>> {
        // Strip optional markdown fences the model may emit despite instructions.
        val cleaned = raw
            .trimIndent()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        return try {
            val dtos = json.decodeFromString<List<QuizQuestionDto>>(cleaned)
            if (dtos.isEmpty()) {
                return Result.Failure(QuizError.ParseFailure(raw = raw, cause = null))
            }
            val questions = dtos.map { it.toDomain() }
            Result.Success(questions)
        } catch (e: Exception) {
            Result.Failure(QuizError.ParseFailure(raw = raw, cause = e))
        }
    }

    private companion object {
        /** AICore error code indicating the Gemini Nano model is not downloaded or not supported. */
        private const val ERROR_CODE_NOT_AVAILABLE = 8
        private const val UNKNOWN_ERROR_CODE = -1
    }
}

/** Wire-format DTO matching the JSON schema instructed in [PromptBuilderImpl]. */
@Serializable
private data class QuizQuestionDto(
    val id: String = UUID.randomUUID().toString(),
    val type: String = QuestionType.MULTIPLE_CHOICE.name,
    val question: String,
    val options: List<String>,
    val answer: String,
    val explanation: String = "",
    @SerialName("sourceReference")
    val sourceReference: String = "",
) {
    fun toDomain() = QuizQuestion(
        id = id.ifBlank { UUID.randomUUID().toString() },
        type = runCatching { QuestionType.valueOf(type) }.getOrDefault(QuestionType.MULTIPLE_CHOICE),
        question = question,
        options = options,
        answer = answer,
        explanation = explanation,
        sourceReference = sourceReference,
    )
}
