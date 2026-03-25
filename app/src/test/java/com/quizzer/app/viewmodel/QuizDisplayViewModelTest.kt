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

    // ── F4 AC3: Submit button always enabled (isSubmitted tracks state) ───────

    @Test
    fun `isSubmitted is false before submitting`() = runTest {
        viewModel.start(questions)
        assertFalse(viewModel.uiState.value.isSubmitted)
    }

    @Test
    fun `isSubmitted is true after onSubmitClicked`() = runTest {
        viewModel.start(questions)
        viewModel.onSubmitClicked()
        assertTrue(viewModel.uiState.value.isSubmitted)
    }

    @Test
    fun `isSubmitted resets to false after onNextClicked advances to next question`() = runTest {
        viewModel.start(questions)
        viewModel.onSubmitClicked()
        viewModel.onNextClicked()
        assertFalse(viewModel.uiState.value.isSubmitted)
    }

    // ── F4 AC1: Option selection (radio-button behaviour) ─────────────────────

    @Test
    fun `selectedAnswer updates when option is selected`() = runTest {
        val firstOption = questions[0].options[0]
        viewModel.start(questions)
        viewModel.onAnswerSelected(firstOption)
        assertEquals(firstOption, viewModel.uiState.value.selectedAnswer)
    }

    @Test
    fun `selecting a second option replaces the first`() = runTest {
        val firstOption = questions[0].options[0]
        val secondOption = questions[0].options[1]
        viewModel.start(questions)
        viewModel.onAnswerSelected(firstOption)
        viewModel.onAnswerSelected(secondOption)
        assertEquals(secondOption, viewModel.uiState.value.selectedAnswer)
    }

    // ── F4 AC7: Options are locked after submission ───────────────────────────

    @Test
    fun `answer selection is ignored after submission`() = runTest {
        val firstOption = questions[0].options[0]
        val secondOption = questions[0].options[1]
        viewModel.start(questions)
        viewModel.onAnswerSelected(firstOption)
        viewModel.onSubmitClicked()
        // Attempt to change selection after submission — should be ignored.
        viewModel.onAnswerSelected(secondOption)
        assertEquals(firstOption, viewModel.uiState.value.selectedAnswer)
    }

    // ── F4 AC3: Submit with no selection records a wrong answer ───────────────

    @Test
    fun `submit with no selection records a wrong answer`() = runTest {
        val singleQuestion = listOf(questions[0])
        viewModel.start(singleQuestion)

        viewModel.navigateToScore.test {
            viewModel.onSubmitClicked() // no selection
            viewModel.onNextClicked()   // last question → fires event

            val answers = awaitItem()
            assertEquals(1, answers.size)
            assertNull(answers[0].selectedOption)
            assertFalse(answers[0].isCorrect)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── F4 AC8: onNextClicked advances currentIndex ───────────────────────────

    @Test
    fun `onNextClicked advances currentIndex and clears selectedAnswer after submission`() = runTest {
        val expectedNextIndex = 1
        viewModel.start(questions)
        viewModel.onAnswerSelected(questions[0].options[0])
        viewModel.onSubmitClicked()
        viewModel.onNextClicked()

        val state = viewModel.uiState.value
        assertEquals(expectedNextIndex, state.currentIndex)
        assertNull(state.selectedAnswer)
    }

    @Test
    fun `currentQuestion changes to next question after submit and next`() = runTest {
        viewModel.start(questions)
        viewModel.onAnswerSelected(questions[0].options[0])
        viewModel.onSubmitClicked()
        viewModel.onNextClicked()
        assertEquals(questions[1], viewModel.uiState.value.currentQuestion)
    }

    // ── Navigation to score after last answer ─────────────────────────────────

    @Test
    fun `navigateToScore emits collected answers after last question is submitted`() = runTest {
        viewModel.start(questions)

        viewModel.navigateToScore.test {
            questions.forEachIndexed { index, question ->
                viewModel.onAnswerSelected(question.options[0])
                viewModel.onSubmitClicked()
                viewModel.onNextClicked() // last question triggers the event
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
            // Answer first question correctly.
            viewModel.onAnswerSelected(correctOption)
            viewModel.onSubmitClicked()
            viewModel.onNextClicked()

            // Answer remaining questions with first option (may or may not be correct).
            questions.drop(1).forEach { question ->
                viewModel.onAnswerSelected(question.options[0])
                viewModel.onSubmitClicked()
                viewModel.onNextClicked()
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

