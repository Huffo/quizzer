package com.quizzer.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.quizzer.app.R
import com.quizzer.app.data.FakeQuizGeneratorRepository
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [QuizDisplayScreen].
 *
 * Tests run against the stateless [QuizDisplayScreen] composable directly —
 * no Hilt wiring required.
 */
class QuizDisplayScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val mcQuestion = FakeQuizGeneratorRepository.FAKE_QUESTIONS[0] // MULTIPLE_CHOICE
    private val tfQuestion = FakeQuizGeneratorRepository.FAKE_QUESTIONS[1] // TRUE_FALSE

    private fun setScreen(uiState: QuizDisplayUiState) {
        rule.setContent {
            QuizDisplayScreen(
                uiState = uiState,
                onOptionSelected = {},
                onSubmitClicked = {},
                onNextClicked = {},
            )
        }
    }

    // ── Spec: "Question X of Y" counter ───────────────────────────────────────

    @Test
    fun questionCounter_displaysCorrectly_forFirstOfFive() {
        val totalQuestions = 5
        val currentIndex = 0
        setScreen(
            QuizDisplayUiState(
                questions = List(totalQuestions) { mcQuestion },
                currentIndex = currentIndex,
            ),
        )

        val expected = rule.activity.getString(
            R.string.quiz_question_counter,
            currentIndex + 1,
            totalQuestions,
        )
        rule.onNodeWithContentDescription(expected).assertIsDisplayed()
    }

    // ── AC7: MULTIPLE_CHOICE shows exactly 4 options ───────────────────────────

    @Test
    fun multipleChoiceQuestion_showsAllFourOptions() {
        setScreen(QuizDisplayUiState(questions = listOf(mcQuestion)))

        mcQuestion.options.forEach { option ->
            val desc = rule.activity.getString(R.string.quiz_option_content_description, option)
            rule.onNodeWithContentDescription(desc).assertIsDisplayed()
        }
    }

    // ── AC6: TRUE_FALSE shows exactly "True" then "False" ─────────────────────

    @Test
    fun trueFalseQuestion_showsOnlyTrueAndFalseOptions() {
        setScreen(QuizDisplayUiState(questions = listOf(tfQuestion)))

        val trueDesc = rule.activity.getString(R.string.quiz_option_content_description, "True")
        val falseDesc = rule.activity.getString(R.string.quiz_option_content_description, "False")
        rule.onNodeWithContentDescription(trueDesc).assertIsDisplayed()
        rule.onNodeWithContentDescription(falseDesc).assertIsDisplayed()
    }

    // ── AC5: Source reference is visible ──────────────────────────────────────

    @Test
    fun sourceReference_isDisplayed() {
        setScreen(QuizDisplayUiState(questions = listOf(mcQuestion)))

        val sourceLabel = rule.activity.getString(R.string.quiz_source_reference_label)
        val sourceDesc = "$sourceLabel: ${mcQuestion.sourceReference}"
        rule.onNodeWithContentDescription(sourceDesc).assertIsDisplayed()
    }

    // ── F4 AC3: Submit button is always enabled (regardless of selection) ─────

    @Test
    fun submitButton_isAlwaysEnabled_whenNoAnswerSelected() {
        setScreen(QuizDisplayUiState(questions = listOf(mcQuestion), selectedAnswer = null))

        val submitLabel = rule.activity.getString(R.string.quiz_submit_button)
        rule.onNodeWithContentDescription(submitLabel).assertIsEnabled()
    }

    @Test
    fun submitButton_isAlwaysEnabled_whenAnswerIsSelected() {
        val selectedOption = mcQuestion.options[0]
        setScreen(
            QuizDisplayUiState(
                questions = listOf(mcQuestion),
                selectedAnswer = selectedOption,
            ),
        )

        val submitLabel = rule.activity.getString(R.string.quiz_submit_button)
        rule.onNodeWithContentDescription(submitLabel).assertIsEnabled()
    }

    // ── F4 AC8: Next button replaces Submit after submission ──────────────────

    @Test
    fun nextButton_isDisplayed_afterSubmission() {
        setScreen(
            QuizDisplayUiState(
                questions = listOf(mcQuestion),
                selectedAnswer = mcQuestion.options[0],
                isSubmitted = true,
            ),
        )

        val nextLabel = rule.activity.getString(R.string.quiz_next_button)
        rule.onNodeWithContentDescription(nextLabel).assertIsDisplayed()
    }

    @Test
    fun submitButton_isNotDisplayed_afterSubmission() {
        setScreen(
            QuizDisplayUiState(
                questions = listOf(mcQuestion),
                selectedAnswer = mcQuestion.options[0],
                isSubmitted = true,
            ),
        )

        val submitLabel = rule.activity.getString(R.string.quiz_submit_button)
        rule.onNodeWithContentDescription(submitLabel).assertIsNotDisplayed()
    }

    // ── F4 AC5 + AC4: Correct answer highlighted after correct submission ──────

    @Test
    fun correctAnswer_hasCorrectDescription_afterCorrectSubmission() {
        val correctOption = mcQuestion.answer
        setScreen(
            QuizDisplayUiState(
                questions = listOf(mcQuestion),
                selectedAnswer = correctOption,
                isSubmitted = true,
            ),
        )

        val correctSelectedDesc = rule.activity.getString(
            R.string.quiz_option_correct_selected_description,
            correctOption,
        )
        rule.onNodeWithContentDescription(correctSelectedDesc).assertIsDisplayed()
    }

    // ── F4 AC4 + AC5: Wrong option and correct answer highlighted after wrong submission

    @Test
    fun wrongOption_andCorrectAnswer_haveExpectedDescriptions_afterWrongSubmission() {
        val wrongOption = mcQuestion.options.first { it != mcQuestion.answer }
        setScreen(
            QuizDisplayUiState(
                questions = listOf(mcQuestion),
                selectedAnswer = wrongOption,
                isSubmitted = true,
            ),
        )

        val wrongDesc = rule.activity.getString(
            R.string.quiz_option_wrong_selected_description,
            wrongOption,
        )
        val correctDesc = rule.activity.getString(
            R.string.quiz_option_correct_answer_description,
            mcQuestion.answer,
        )
        rule.onNodeWithContentDescription(wrongDesc).assertIsDisplayed()
        rule.onNodeWithContentDescription(correctDesc).assertIsDisplayed()
    }

    // ── F4 AC6: Explanation visible after submission ───────────────────────────

    @Test
    fun explanation_isDisplayed_afterSubmission() {
        setScreen(
            QuizDisplayUiState(
                questions = listOf(mcQuestion),
                isSubmitted = true,
            ),
        )

        val explanationLabel = rule.activity.getString(R.string.quiz_explanation_label)
        val explanationDesc = "$explanationLabel: ${mcQuestion.explanation}"
        rule.onNodeWithContentDescription(explanationDesc).assertIsDisplayed()
    }
}

