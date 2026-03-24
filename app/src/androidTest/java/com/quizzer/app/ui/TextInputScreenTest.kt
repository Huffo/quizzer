package com.quizzer.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.quizzer.app.R
import com.quizzer.app.model.QuestionType
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [TextInputScreen].
 *
 * Uses [createAndroidComposeRule] so string resources can be resolved
 * from [R.string] via [ComponentActivity.getString].
 *
 * Tests run against the stateless [TextInputScreen] composable directly
 * — no Hilt wiring required.
 */
class TextInputScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun setScreen(uiState: TextInputUiState) {
        rule.setContent {
            TextInputScreen(
                uiState = uiState,
                onPdfSelected = {},
                onQuestionCountChanged = {},
                onQuestionTypeToggled = {},
                onGenerateClicked = {},
            )
        }
    }

    // ── Generate button state ──────────────────────────────────────────────────

    @Test
    fun generateButton_isDisabled_whenNoPdfIsLoaded() {
        setScreen(TextInputUiState())

        val generateLabel = rule.activity.getString(R.string.text_input_generate_button)
        rule.onNodeWithContentDescription(generateLabel).assertIsNotEnabled()
    }

    @Test
    fun generateButton_isDisabled_whenPdfYieldsTooFewWords() {
        val shortText = "word ".repeat(50).trim()
        setScreen(
            TextInputUiState(
                pdfState = PdfState.Ready(text = shortText, wordCount = 50),
            ),
        )

        val generateLabel = rule.activity.getString(R.string.text_input_generate_button)
        rule.onNodeWithContentDescription(generateLabel).assertIsNotEnabled()
    }

    @Test
    fun generateButton_isEnabled_whenValidPdfIsLoaded() {
        setScreen(
            TextInputUiState(
                pdfState = PdfState.Ready(
                    text = "word ".repeat(110).trim(),
                    wordCount = 110,
                ),
            ),
        )

        val generateLabel = rule.activity.getString(R.string.text_input_generate_button)
        rule.onNodeWithContentDescription(generateLabel).assertIsEnabled()
    }

    // ── Word count label ───────────────────────────────────────────────────────

    @Test
    fun wordCountLabel_isShown_afterSuccessfulParse() {
        val wordCount = 110
        setScreen(
            TextInputUiState(
                pdfState = PdfState.Ready(
                    text = "word ".repeat(wordCount).trim(),
                    wordCount = wordCount,
                ),
            ),
        )

        val expected = rule.activity.getString(R.string.text_input_word_count, wordCount)
        rule.onNodeWithText(expected).assertExists()
    }

    @Test
    fun tooShortError_isShown_whenWordCountBelowMinimum() {
        setScreen(
            TextInputUiState(
                pdfState = PdfState.Ready(text = "word ".repeat(50).trim(), wordCount = 50),
            ),
        )

        val errorText = rule.activity.getString(R.string.text_input_too_short_error)
        rule.onNodeWithText(errorText).assertExists()
    }

    @Test
    fun passwordError_isShown_whenPdfIsPasswordProtected() {
        setScreen(
            TextInputUiState(
                pdfState = PdfState.ParseError(
                    error = com.quizzer.app.model.PdfError.PasswordProtected,
                ),
            ),
        )

        val errorText = rule.activity.getString(R.string.text_input_pdf_error_password)
        rule.onNodeWithText(errorText).assertExists()
    }

    // ── Question types ─────────────────────────────────────────────────────────

    @Test
    fun bothQuestionTypeCheckboxes_arePresent() {
        setScreen(TextInputUiState())

        val mcLabel = rule.activity.getString(R.string.text_input_type_multiple_choice)
        val tfLabel = rule.activity.getString(R.string.text_input_type_true_false)
        rule.onNodeWithText(mcLabel).assertExists()
        rule.onNodeWithText(tfLabel).assertExists()
    }

    @Test
    fun pickPdfButton_isPresent_andHasCorrectContentDescription() {
        setScreen(TextInputUiState())

        val pickLabel = rule.activity.getString(R.string.text_input_pick_pdf_button)
        rule.onNodeWithContentDescription(pickLabel).assertExists()
    }
}
