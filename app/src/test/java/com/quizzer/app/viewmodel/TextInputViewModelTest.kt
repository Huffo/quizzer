package com.quizzer.app.viewmodel

import android.net.Uri
import app.cash.turbine.test
import com.quizzer.app.data.FakePdfParser
import com.quizzer.app.model.PdfError
import com.quizzer.app.model.PdfResult
import com.quizzer.app.model.QuestionType
import com.quizzer.app.model.QuizConfig
import com.quizzer.app.ui.PdfState
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TextInputViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakePdfParser: FakePdfParser
    private lateinit var viewModel: TextInputViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePdfParser = FakePdfParser()
        viewModel = TextInputViewModel(fakePdfParser)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ──────────────────────────────────────────────────────────

    @Test
    fun `initial pdfState is Idle and Generate is disabled`() {
        val state = viewModel.uiState.value
        assertEquals(PdfState.Idle, state.pdfState)
        assertFalse(state.isGenerateEnabled)
    }

    @Test
    fun `questionCount defaults to QuizConfig DEFAULT_QUESTION_COUNT`() {
        assertEquals(QuizConfig.DEFAULT_QUESTION_COUNT, viewModel.uiState.value.questionCount)
    }

    @Test
    fun `both question types are selected by default`() {
        assertEquals(QuestionType.entries.toSet(), viewModel.uiState.value.selectedTypes)
    }

    // ── PDF selection ──────────────────────────────────────────────────────────

    @Test
    fun `Generate is disabled when extracted text is below 100 words`() = runTest {
        val shortText = "word ".repeat(50).trim()
        fakePdfParser.response = PdfResult.Success(shortText)

        viewModel.onPdfSelected(mockk(relaxed = true))

        val state = viewModel.uiState.value
        assertInstanceOf(PdfState.Ready::class.java, state.pdfState)
        assertFalse(state.isGenerateEnabled)
    }

    @Test
    fun `Generate is enabled when extracted text meets the 100-word minimum`() = runTest {
        fakePdfParser.response = PdfResult.Success(FakePdfParser.FAKE_TEXT)

        viewModel.onPdfSelected(mockk(relaxed = true))

        assertTrue(viewModel.uiState.value.isGenerateEnabled)
    }

    @Test
    fun `parse error sets ParseError state and keeps Generate disabled`() = runTest {
        fakePdfParser.response = PdfResult.Failure(PdfError.PasswordProtected)

        viewModel.onPdfSelected(mockk(relaxed = true))

        val state = viewModel.uiState.value
        assertInstanceOf(PdfState.ParseError::class.java, state.pdfState)
        assertFalse(state.isGenerateEnabled)
    }

    @Test
    fun `picking a new PDF replaces the previous extraction`() = runTest {
        val firstUri = mockk<Uri>(relaxed = true)
        val secondUri = mockk<Uri>(relaxed = true)
        fakePdfParser.response = PdfResult.Success(FakePdfParser.FAKE_TEXT)

        viewModel.onPdfSelected(firstUri)
        assertEquals(firstUri, fakePdfParser.lastUri)

        viewModel.onPdfSelected(secondUri)
        assertEquals(secondUri, fakePdfParser.lastUri)
        assertTrue(viewModel.uiState.value.isGenerateEnabled)
    }

    // ── Question count ─────────────────────────────────────────────────────────

    @Test
    fun `questionCount below minimum is clamped to MIN_QUESTION_COUNT`() {
        viewModel.onQuestionCountChanged(0)
        assertEquals(QuizConfig.MIN_QUESTION_COUNT, viewModel.uiState.value.questionCount)
    }

    @Test
    fun `questionCount above maximum is clamped to MAX_QUESTION_COUNT`() {
        viewModel.onQuestionCountChanged(21)
        assertEquals(QuizConfig.MAX_QUESTION_COUNT, viewModel.uiState.value.questionCount)
    }

    @Test
    fun `valid questionCount in range is stored as-is`() {
        val targetCount = 10
        viewModel.onQuestionCountChanged(targetCount)
        assertEquals(targetCount, viewModel.uiState.value.questionCount)
    }

    // ── Question type toggle ───────────────────────────────────────────────────

    @Test
    fun `toggling one type removes it from selectedTypes`() {
        viewModel.onQuestionTypeToggled(QuestionType.MULTIPLE_CHOICE)
        assertEquals(setOf(QuestionType.TRUE_FALSE), viewModel.uiState.value.selectedTypes)
    }

    @Test
    fun `the last selected type cannot be deselected`() {
        viewModel.onQuestionTypeToggled(QuestionType.MULTIPLE_CHOICE)
        assertEquals(setOf(QuestionType.TRUE_FALSE), viewModel.uiState.value.selectedTypes)

        viewModel.onQuestionTypeToggled(QuestionType.TRUE_FALSE)
        assertEquals(setOf(QuestionType.TRUE_FALSE), viewModel.uiState.value.selectedTypes)
    }

    @Test
    fun `toggling a deselected type adds it back to selectedTypes`() {
        viewModel.onQuestionTypeToggled(QuestionType.MULTIPLE_CHOICE) // remove
        viewModel.onQuestionTypeToggled(QuestionType.MULTIPLE_CHOICE) // re-add
        assertEquals(QuestionType.entries.toSet(), viewModel.uiState.value.selectedTypes)
    }

    // ── Generate ───────────────────────────────────────────────────────────────

    @Test
    fun `onGenerateClicked emits QuizConfig via navigateToGeneration`() = runTest {
        fakePdfParser.response = PdfResult.Success(FakePdfParser.FAKE_TEXT)
        val targetCount = 10
        viewModel.onPdfSelected(mockk(relaxed = true))
        viewModel.onQuestionCountChanged(targetCount)

        viewModel.navigateToGeneration.test {
            viewModel.onGenerateClicked()
            val config = awaitItem()
            assertEquals(targetCount, config.questionCount)
            assertEquals(QuestionType.entries.toSet(), config.questionTypes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onGenerateClicked emits nothing when Generate is disabled`() = runTest {
        viewModel.navigateToGeneration.test {
            viewModel.onGenerateClicked() // no PDF loaded
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
