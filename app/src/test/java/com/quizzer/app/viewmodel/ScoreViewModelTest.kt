package com.quizzer.app.viewmodel

import com.quizzer.app.data.FakeQuizGeneratorRepository
import com.quizzer.app.model.QuizQuestion
import com.quizzer.app.model.UserAnswer
import com.quizzer.app.ui.ScoreUiState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [ScoreViewModel].
 *
 * Covers all scenarios defined in f5-scoring.md Required Test Coverage section.
 */
class ScoreViewModelTest {

    private lateinit var viewModel: ScoreViewModel

    private val questions: List<QuizQuestion> = FakeQuizGeneratorRepository.FAKE_QUESTIONS

    @BeforeEach
    fun setUp() {
        viewModel = ScoreViewModel()
    }

    // ── Spec: correctCount equals answers where selectedOption == correctAnswer ──

    @Test
    fun `correctCount equals number of answers with matching selectedOption`() {
        val expectedCorrectCount = 1
        val answers = listOf(
            UserAnswer(questionId = questions[0].id, selectedOption = questions[0].answer, correctAnswer = questions[0].answer),
            UserAnswer(questionId = questions[1].id, selectedOption = "False", correctAnswer = questions[1].answer),
        )

        viewModel.initialise(questions, answers)

        assertEquals(expectedCorrectCount, viewModel.uiState.value.correctCount)
    }

    // ── Spec: unanswered (null selectedOption) counted as wrong ───────────────

    @Test
    fun `unanswered question has null selectedOption and counts as wrong`() {
        val answers = listOf(
            UserAnswer(questionId = questions[0].id, selectedOption = null, correctAnswer = questions[0].answer),
            UserAnswer(questionId = questions[1].id, selectedOption = questions[1].answer, correctAnswer = questions[1].answer),
        )

        viewModel.initialise(questions, answers)

        val state = viewModel.uiState.value
        assertFalse(state.breakdownRows[0].isCorrect)
        assertTrue(state.breakdownRows[1].isCorrect)
        assertEquals(1, state.correctCount)
    }

    // ── Spec: denominator equals actual question count, not requested count ───

    @Test
    fun `totalCount equals actual question count not any larger requested count`() {
        val partialQuestions = questions.take(1)
        val answers = listOf(
            UserAnswer(questionId = partialQuestions[0].id, selectedOption = partialQuestions[0].answer, correctAnswer = partialQuestions[0].answer),
        )
        val expectedTotal = 1

        viewModel.initialise(partialQuestions, answers)

        assertEquals(expectedTotal, viewModel.uiState.value.totalCount)
    }

    // ── Spec: all correct → correctCount == totalCount ────────────────────────

    @Test
    fun `all correct answers gives correctCount equal to totalCount`() {
        val answers = questions.map { q ->
            UserAnswer(questionId = q.id, selectedOption = q.answer, correctAnswer = q.answer)
        }

        viewModel.initialise(questions, answers)

        val state = viewModel.uiState.value
        assertEquals(state.totalCount, state.correctCount)
    }

    // ── Spec: all wrong (or unanswered) → correctCount == 0 ──────────────────

    @Test
    fun `all wrong answers gives zero correctCount`() {
        val expectedCorrectCount = 0
        val answers = questions.map { q ->
            UserAnswer(questionId = q.id, selectedOption = null, correctAnswer = q.answer)
        }

        viewModel.initialise(questions, answers)

        assertEquals(expectedCorrectCount, viewModel.uiState.value.correctCount)
    }

    // ── Spec: breakdownRows contain a ScoreRow per question ──────────────────

    @Test
    fun `breakdownRows size matches question list size`() {
        val answers = questions.map { q ->
            UserAnswer(questionId = q.id, selectedOption = q.answer, correctAnswer = q.answer)
        }

        viewModel.initialise(questions, answers)

        assertEquals(questions.size, viewModel.uiState.value.breakdownRows.size)
    }

    // ── Spec: ScoreRow.answer is null for missing answers ─────────────────────

    @Test
    fun `breakdownRow has null answer when answer is missing for that question index`() {
        // Only supply answers for the first question
        val answers = listOf(
            UserAnswer(questionId = questions[0].id, selectedOption = questions[0].answer, correctAnswer = questions[0].answer),
        )

        viewModel.initialise(questions, answers)

        val rows = viewModel.uiState.value.breakdownRows
        // Second question has no answer → null
        assertEquals(null, rows[1].answer)
        assertFalse(rows[1].isCorrect)
    }

    // ── Spec: initialise is idempotent ────────────────────────────────────────

    @Test
    fun `second initialise call is ignored after first`() {
        val answersFirst = questions.map { q ->
            UserAnswer(questionId = q.id, selectedOption = q.answer, correctAnswer = q.answer)
        }
        val answersSecond = questions.map { q ->
            UserAnswer(questionId = q.id, selectedOption = null, correctAnswer = q.answer)
        }

        viewModel.initialise(questions, answersFirst)
        viewModel.initialise(questions, answersSecond) // should be ignored

        assertEquals(questions.size, viewModel.uiState.value.correctCount)
    }
}
