package com.quizzer.app.viewmodel

import app.cash.turbine.test
import com.quizzer.app.data.FakeQuizGeneratorRepository
import com.quizzer.app.model.QuizQuestion
import com.quizzer.app.model.UserAnswer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizDisplayViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: QuizDisplayViewModel

    private val questions: List<QuizQuestion> = FakeQuizGeneratorRepository.FAKE_QUESTIONS

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = QuizDisplayViewModel()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Spec: exposes correct currentIndex and totalCount ─────────────────────

    @Test
    fun `after start, currentIndex is 0`() = runTest {
        val expectedIndex = 0
        viewModel.start(questions)
        assertEquals(expectedIndex, viewModel.uiState.value.currentIndex)
    }

    @Test
    fun `after start, totalCount matches question list size`() = runTest {
        viewModel.start(questions)
        assertEquals(questions.size, viewModel.uiState.value.totalCount)
    }

    // ── Spec: currentQuestion matches the question at currentIndex ────────────

    @Test
    fun `currentQuestion matches question at currentIndex after start`() = runTest {
        viewModel.start(questions)
        assertEquals(questions[0], viewModel.uiState.value.currentQuestion)
    }

    // ── AC8: canAdvance gated on selection ────────────────────────────────────

    @Test
    fun `canAdvance is false before any option is selected`() = runTest {
        viewModel.start(questions)
        assertFalse(viewModel.uiState.value.canAdvance)
    }

    @Test
    fun `canAdvance is true after an option is selected`() = runTest {
        viewModel.start(questions)
        val firstOption = questions[0].options[0]
        viewModel.onAnswerSelected(firstOption)
        assertTrue(viewModel.uiState.value.canAdvance)
    }

    @Test
    fun `canAdvance resets to false after advancing to the next question`() = runTest {
        viewModel.start(questions)
        viewModel.onAnswerSelected(questions[0].options[0])
        viewModel.onSubmitClicked()
        assertFalse(viewModel.uiState.value.canAdvance)
    }

    // ── AC9: currentIndex increments after submitting an answer ───────────────

    @Test
    fun `onSubmitClicked advances currentIndex and clears selectedAnswer`() = runTest {
        val expectedNextIndex = 1
        viewModel.start(questions)
        viewModel.onAnswerSelected(questions[0].options[0])
        viewModel.onSubmitClicked()

        val state = viewModel.uiState.value
        assertEquals(expectedNextIndex, state.currentIndex)
        assertNull(state.selectedAnswer)
    }

    @Test
    fun `currentQuestion changes to next question after submitting first answer`() = runTest {
        viewModel.start(questions)
        viewModel.onAnswerSelected(questions[0].options[0])
        viewModel.onSubmitClicked()
        assertEquals(questions[1], viewModel.uiState.value.currentQuestion)
    }

    // ── Navigation to score after last answer ─────────────────────────────────

    @Test
    fun `navigateToScore emits collected answers after last question is submitted`() = runTest {
        viewModel.start(questions)

        viewModel.navigateToScore.test {
            questions.forEach { question ->
                viewModel.onAnswerSelected(question.options[0])
                viewModel.onSubmitClicked()
            }

            val answers: List<UserAnswer> = awaitItem()
            assertEquals(questions.size, answers.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navigateToScore answer correctness reflects selected vs correct option`() = runTest {
        viewModel.start(questions)
        val firstQuestion = questions[0]
        val correctOption = firstQuestion.answer

        viewModel.navigateToScore.test {
            // Answer first question correctly, remaining with first option.
            viewModel.onAnswerSelected(correctOption)
            viewModel.onSubmitClicked()

            questions.drop(1).forEach { question ->
                viewModel.onAnswerSelected(question.options[0])
                viewModel.onSubmitClicked()
            }

            val answers = awaitItem()
            assertTrue(answers[0].isCorrect)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── start() is idempotent ─────────────────────────────────────────────────

    @Test
    fun `start is idempotent - second call does not reset state`() = runTest {
        viewModel.start(questions)
        viewModel.onAnswerSelected(questions[0].options[0])

        // Second call with different order should be ignored.
        viewModel.start(questions.reversed())

        // selectedAnswer should still be the one set before the second start() call.
        val selectedAfter = viewModel.uiState.value.selectedAnswer
        assertEquals(questions[0].options[0], selectedAfter)
    }
}
