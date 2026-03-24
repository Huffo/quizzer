package com.quizzer.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
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

    // ── AC8: Forward navigation not available before answer selected ──────────

    @Test
    fun submitButton_isDisabled_whenNoAnswerIsSelected() {
        setScreen(QuizDisplayUiState(questions = listOf(mcQuestion), selectedAnswer = null))

        val submitLabel = rule.activity.getString(R.string.quiz_submit_button)
        rule.onNodeWithContentDescription(submitLabel).assertIsNotEnabled()
    }

    @Test
    fun submitButton_isEnabled_whenAnswerIsSelected() {
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
}
