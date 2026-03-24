package com.quizzer.app.data

import com.quizzer.app.domain.QuizGeneratorRepository
import com.quizzer.app.model.QuizConfig
import com.quizzer.app.model.QuizQuestion
import com.quizzer.app.model.QuestionType
import com.quizzer.app.model.Result
import javax.inject.Inject

/**
 * Test/debug stand-in for [QuizGeneratorRepository].
 *
 * Returns a configurable [response] without calling AICore.
 * Bound in debug builds via [com.quizzer.app.data.di.RepositoryModule].
 *
 * [lastText] and [lastConfig] let tests assert what arguments were passed.
 */
class FakeQuizGeneratorRepository @Inject constructor() : QuizGeneratorRepository {

    var response: Result<List<QuizQuestion>> = Result.Success(FAKE_QUESTIONS)

    var lastText: String? = null
        private set

    var lastConfig: QuizConfig? = null
        private set

    override suspend fun generateQuiz(
        text: String,
        config: QuizConfig,
    ): Result<List<QuizQuestion>> {
        lastText = text
        lastConfig = config
        return response
    }

    companion object {
        val FAKE_QUESTIONS: List<QuizQuestion> = listOf(
            QuizQuestion(
                id = "fake-q1",
                type = QuestionType.MULTIPLE_CHOICE,
                question = "According to the text, what is the main subject discussed?",
                options = listOf("A) Subject A", "B) Subject B", "C) Subject C", "D) Subject D"),
                answer = "A) Subject A",
                explanation = "The text explicitly states that Subject A is the primary topic.",
                sourceReference = "Introduction",
            ),
            QuizQuestion(
                id = "fake-q2",
                type = QuestionType.TRUE_FALSE,
                question = "The text describes Subject A as important.",
                options = listOf("True", "False"),
                answer = "True",
                explanation = "The text directly states that Subject A is important.",
                sourceReference = "Introduction",
            ),
        )
    }
}
