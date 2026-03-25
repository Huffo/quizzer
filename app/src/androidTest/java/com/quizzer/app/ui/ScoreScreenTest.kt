package com.quizzer.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.quizzer.app.R
import com.quizzer.app.data.FakeQuizGeneratorRepository
import com.quizzer.app.model.UserAnswer
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue

/**
 * Compose UI tests for [ScoreScreen].
 *
 * Tests run against the stateless [ScoreScreen] composable directly —
 * no Hilt wiring required.
 */
class ScoreScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val questions = FakeQuizGeneratorRepository.FAKE_QUESTIONS

    private fun allCorrectAnswers() = questions.map { q ->
        UserAnswer(questionId = q.id, selectedOption = q.answer, correctAnswer = q.answer)
    }

    private fun allUnanswered() = questions.map { q ->
        UserAnswer(questionId = q.id, selectedOption = null, correctAnswer = q.answer)
    }

    private fun setScreen(uiState: ScoreUiState, onNewQuiz: () -> Unit = {}) {
        rule.setContent {
            ScoreScreen(
                uiState = uiState,
                onNewQuizClicked = onNewQuiz,
            )
        }
    }

    // ── AC1: Score label shows "X / Y" format ─────────────────────────────────

    @Test
    fun scoreLabel_displaysCorrectCountOutOfTotal_whenAllCorrect() {
        val totalCount = questions.size
        val correctCount = totalCount
        val expectedLabel = rule.activity.getString(R.string.score_result, correctCount, totalCount)

        setScreen(ScoreUiState(questions = questions, answers = allCorrectAnswers()))

        rule.onNodeWithContentDescription(expectedLabel).assertIsDisplayed()
    }

    @Test
    fun scoreLabel_displaysZeroCorrect_whenAllUnanswered() {
        val totalCount = questions.size
        val expectedLabel = rule.activity.getString(R.string.score_result, 0, totalCount)

        setScreen(ScoreUiState(questions = questions, answers = allUnanswered()))

        rule.onNodeWithContentDescription(expectedLabel).assertIsDisplayed()
    }

    // ── AC2: Question breakdown shows question text ────────────────────────────

    @Test
    fun breakdownCard_displaysQuestionText_forEachQuestion() {
        setScreen(ScoreUiState(questions = questions, answers = allCorrectAnswers()))

        questions.forEach { q ->
            rule.onNodeWithText(q.question).assertIsDisplayed()
        }
    }

    // ── AC2: Breakdown shows correct answer ───────────────────────────────────

    @Test
    fun breakdownCard_displaysCorrectAnswer_forEachQuestion() {
        setScreen(ScoreUiState(questions = questions, answers = allCorrectAnswers()))

        questions.forEach { q ->
            rule.onNodeWithText(q.answer).assertIsDisplayed()
        }
    }

    // ── AC2: Breakdown shows explanation ──────────────────────────────────────

    @Test
    fun breakdownCard_displaysExplanation_forEachQuestion() {
        setScreen(ScoreUiState(questions = questions, answers = allCorrectAnswers()))

        questions.forEach { q ->
            rule.onNodeWithText(q.explanation).assertIsDisplayed()
        }
    }

    // ── AC2 / AC7: Breakdown shows source reference ───────────────────────────

    @Test
    fun breakdownCard_displaysSourceReference_forEachQuestion() {
        setScreen(ScoreUiState(questions = questions, answers = allCorrectAnswers()))

        questions.forEach { q ->
            rule.onNodeWithText(q.sourceReference).assertIsDisplayed()
        }
    }

    // ── AC4: Unanswered question shows "No answer" ────────────────────────────

    @Test
    fun breakdownCard_showsNoAnswer_whenQuestionWasSkipped() {
        val noAnswerLabel = rule.activity.getString(R.string.score_no_answer)

        setScreen(ScoreUiState(questions = questions, answers = allUnanswered()))

        rule.onNodeWithText(noAnswerLabel).assertIsDisplayed()
    }

    // ── AC5: "New Quiz" button is displayed ───────────────────────────────────

    @Test
    fun newQuizButton_isDisplayed() {
        val newQuizLabel = rule.activity.getString(R.string.score_new_quiz_button)

        setScreen(ScoreUiState(questions = questions, answers = allCorrectAnswers()))

        rule.onNodeWithContentDescription(newQuizLabel).assertIsDisplayed()
    }

    // ── AC5: "New Quiz" button triggers the callback ──────────────────────────

    @Test
    fun newQuizButton_invokesCallback_whenTapped() {
        val newQuizLabel = rule.activity.getString(R.string.score_new_quiz_button)
        var callbackInvoked = false

        setScreen(
            uiState = ScoreUiState(questions = questions, answers = allCorrectAnswers()),
            onNewQuiz = { callbackInvoked = true },
        )

        rule.onNodeWithContentDescription(newQuizLabel).performClick()
        assertTrue(callbackInvoked)
    }
}
