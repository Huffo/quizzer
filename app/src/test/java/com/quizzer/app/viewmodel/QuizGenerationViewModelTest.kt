package com.quizzer.app.viewmodel

import app.cash.turbine.test
import com.quizzer.app.data.FakeQuizGeneratorRepository
import com.quizzer.app.model.QuizConfig
import com.quizzer.app.model.QuizError
import com.quizzer.app.model.QuizQuestion
import com.quizzer.app.model.QuestionType
import com.quizzer.app.model.Result
import com.quizzer.app.ui.QuizGenerationUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizGenerationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeQuizGeneratorRepository
    private lateinit var viewModel: QuizGenerationViewModel

    private val defaultConfig = QuizConfig(
        questionCount = 2,
        questionTypes = QuestionType.entries.toSet(),
    )
    private val sampleText = "Sample ".repeat(120).trim()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuizGeneratorRepository()
        viewModel = QuizGenerationViewModel(fakeRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── State transitions ──────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() {
        assertInstanceOf(QuizGenerationUiState.Loading::class.java, viewModel.uiState.value)
    }

    @Test
    fun `generateQuiz emits Loading then Success on repository success`() = runTest {
        fakeRepository.response = Result.Success(FakeQuizGeneratorRepository.FAKE_QUESTIONS)

        viewModel.uiState.test {
            assertInstanceOf(QuizGenerationUiState.Loading::class.java, awaitItem())
            viewModel.generateQuiz(sampleText, defaultConfig)
            val result = awaitItem()
            assertInstanceOf(QuizGenerationUiState.Success::class.java, result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `generateQuiz emits PartialSuccess when result count is less than requested`() = runTest {
        val requestedCount = 5
        val partialQuestions = FakeQuizGeneratorRepository.FAKE_QUESTIONS.take(2)
        fakeRepository.response = Result.Success(partialQuestions)

        val config = QuizConfig(questionCount = requestedCount)
        viewModel.uiState.test {
            awaitItem() // consume Loading
            viewModel.generateQuiz(sampleText, config)
            val result = awaitItem()
            assertInstanceOf(QuizGenerationUiState.PartialSuccess::class.java, result)
            val partial = result as QuizGenerationUiState.PartialSuccess
            assertEquals(partialQuestions.size, partial.questions.size)
            assertEquals(requestedCount, partial.requestedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `generateQuiz emits Error(ModelUnavailable) on QuizError_ModelUnavailable`() = runTest {
        val error = QuizError.ModelUnavailable(statusMessage = "NOT_SUPPORTED")
        fakeRepository.response = Result.Failure(error)

        viewModel.uiState.test {
            awaitItem() // consume Loading
            viewModel.generateQuiz(sampleText, defaultConfig)
            val result = awaitItem()
            assertInstanceOf(QuizGenerationUiState.Error::class.java, result)
            assertEquals(error, (result as QuizGenerationUiState.Error).error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `generateQuiz emits Error(ParseFailure) on QuizError_ParseFailure`() = runTest {
        val error = QuizError.ParseFailure(raw = "not json")
        fakeRepository.response = Result.Failure(error)

        viewModel.uiState.test {
            awaitItem() // consume Loading
            viewModel.generateQuiz(sampleText, defaultConfig)
            val result = awaitItem()
            assertInstanceOf(QuizGenerationUiState.Error::class.java, result)
            assertEquals(error, (result as QuizGenerationUiState.Error).error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `second generateQuiz call while loading is ignored`() = runTest {
        // Use a fake that suspends indefinitely so generation stays in Loading.
        var callCount = 0
        val blockingRepository = object : com.quizzer.app.domain.QuizGeneratorRepository {
            override suspend fun generateQuiz(
                text: String,
                config: QuizConfig,
            ): Result<List<QuizQuestion>> {
                callCount++
                kotlinx.coroutines.awaitCancellation()
            }
        }
        val vm = QuizGenerationViewModel(blockingRepository)

        vm.uiState.test {
            awaitItem() // Initial Loading
            vm.generateQuiz(sampleText, defaultConfig) // first call — starts job
            vm.generateQuiz(sampleText, defaultConfig) // second call — must be ignored
            expectNoEvents()
            assertEquals(1, callCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cancelGeneration resets state to Loading`() = runTest {
        fakeRepository.response = Result.Failure(QuizError.ModelUnavailable("unavailable"))

        viewModel.uiState.test {
            awaitItem() // initial Loading
            viewModel.generateQuiz(sampleText, defaultConfig)
            awaitItem() // Error state

            viewModel.cancelGeneration()
            assertInstanceOf(QuizGenerationUiState.Loading::class.java, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Success contents ───────────────────────────────────────────────────────

    @Test
    fun `Success state contains the correct questions`() = runTest {
        val expected = FakeQuizGeneratorRepository.FAKE_QUESTIONS
        fakeRepository.response = Result.Success(expected)

        val config = QuizConfig(questionCount = expected.size)
        viewModel.uiState.test {
            awaitItem()
            viewModel.generateQuiz(sampleText, config)
            val result = awaitItem() as QuizGenerationUiState.Success
            assertEquals(expected, result.questions)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
