package com.quizzer.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzer.app.R
import com.quizzer.app.model.QuizQuestion
import com.quizzer.app.model.UserAnswer
import com.quizzer.app.ui.theme.QuizzerTheme
import com.quizzer.app.viewmodel.QuizDisplayViewModel

private const val SCREEN_HORIZONTAL_PADDING_DP = 16
private const val SCREEN_VERTICAL_PADDING_DP = 8
private const val SECTION_SPACING_DP = 8
private const val EXPLANATION_SPACING_DP = 4
private const val OPTION_ROW_VERTICAL_PADDING_DP = 8
private const val RADIO_TEXT_SPACER_DP = 8

/**
 * Route-level composable: owns the [QuizDisplayViewModel], fires [start] on first entry,
 * and wires navigation side-effects.
 *
 * Back presses are blocked once the user has moved past the first question (AC9).
 */
@Composable
fun QuizDisplayRoute(
    questions: List<QuizQuestion>,
    partialCount: Int?,
    onNavigateToScore: (List<UserAnswer>) -> Unit,
    viewModel: QuizDisplayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Initialise quiz once questions are available (idempotent on recomposition).
    LaunchedEffect(questions) {
        if (questions.isNotEmpty()) {
            viewModel.start(questions, partialCount)
        }
    }

    // Collect the one-shot navigation event to the score screen.
    LaunchedEffect(Unit) {
        viewModel.navigateToScore.collect { answers ->
            onNavigateToScore(answers)
        }
    }

    // AC9: Block back navigation once the user has submitted at least the first answer
    // and advanced to a later question.
    BackHandler(enabled = uiState.currentIndex > 0) { /* intentionally consume */ }

    QuizDisplayScreen(
        uiState = uiState,
        onOptionSelected = viewModel::onAnswerSelected,
        onSubmitClicked = viewModel::onSubmitClicked,
        onNextClicked = viewModel::onNextClicked,
    )
}

/**
 * Stateless quiz display composable.
 *
 * Renders a single question at a time. Before submission: shows the question,
 * selectable options, and an always-enabled Submit button (AC3). After submission:
 * options are locked with correct/incorrect colour feedback (AC4, AC5), the
 * explanation is shown (AC6), and the Submit button is replaced by Next (AC8).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizDisplayScreen(
    uiState: QuizDisplayUiState,
    onOptionSelected: (String) -> Unit,
    onSubmitClicked: () -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Nothing to show until the quiz has been started.
    val question = uiState.currentQuestion ?: return

    val counterLabel = stringResource(
        R.string.quiz_question_counter,
        uiState.currentIndex + 1,
        uiState.totalCount,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = counterLabel,
                        modifier = Modifier.semantics { contentDescription = counterLabel },
                    )
                },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    horizontal = SCREEN_HORIZONTAL_PADDING_DP.dp,
                    vertical = SCREEN_VERTICAL_PADDING_DP.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp),
        ) {
            // Question text displayed prominently.
            Text(
                text = question.question,
                style = MaterialTheme.typography.headlineSmall,
            )

            // Options: selectable before submission, locked with colour feedback after (AC4, AC5, AC7).
            Column(modifier = Modifier.selectableGroup()) {
                question.options.forEach { option ->
                    OptionRow(
                        option = option,
                        isSelected = option == uiState.selectedAnswer,
                        isSubmitted = uiState.isSubmitted,
                        isCorrectAnswer = option == question.answer,
                        onSelect = { onOptionSelected(option) },
                    )
                }
            }

            // Explanation shown only after the user submits (AC6).
            if (uiState.isSubmitted) {
                val explanationLabel = stringResource(R.string.quiz_explanation_label)
                val explanationDesc = "$explanationLabel: ${question.explanation}"
                Column(
                    verticalArrangement = Arrangement.spacedBy(EXPLANATION_SPACING_DP.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = explanationDesc },
                ) {
                    Text(
                        text = explanationLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = question.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // Source reference below the options / explanation.
            val sourceLabel = stringResource(R.string.quiz_source_reference_label)
            val sourceDesc = "$sourceLabel: ${question.sourceReference}"
            Text(
                text = sourceDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { contentDescription = sourceDesc },
            )

            Spacer(modifier = Modifier.weight(1f))

            // Before submission: Submit (always enabled, AC3).
            // After submission: Next to advance or go to score (AC8).
            if (!uiState.isSubmitted) {
                val submitLabel = stringResource(R.string.quiz_submit_button)
                Button(
                    onClick = onSubmitClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = submitLabel },
                ) {
                    Text(submitLabel)
                }
            } else {
                val nextLabel = stringResource(R.string.quiz_next_button)
                Button(
                    onClick = onNextClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = nextLabel },
                ) {
                    Text(nextLabel)
                }
            }

            Spacer(modifier = Modifier.height(SCREEN_VERTICAL_PADDING_DP.dp))
        }
    }
}

@Composable
private fun OptionRow(
    option: String,
    isSelected: Boolean,
    isSubmitted: Boolean,
    isCorrectAnswer: Boolean,
    onSelect: () -> Unit,
) {
    // Post-submission content descriptions convey feedback to screen readers (AC4, AC5).
    val optionDesc = when {
        isSubmitted && isCorrectAnswer && isSelected ->
            stringResource(R.string.quiz_option_correct_selected_description, option)
        isSubmitted && isCorrectAnswer ->
            stringResource(R.string.quiz_option_correct_answer_description, option)
        isSubmitted && isSelected ->
            stringResource(R.string.quiz_option_wrong_selected_description, option)
        else ->
            stringResource(R.string.quiz_option_content_description, option)
    }

    // Background colour highlights correct/incorrect options after submission (AC4, AC5).
    val containerColor: Color = when {
        isSubmitted && isCorrectAnswer -> MaterialTheme.colorScheme.primaryContainer
        isSubmitted && isSelected -> MaterialTheme.colorScheme.errorContainer
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                role = Role.RadioButton,
                enabled = !isSubmitted,
            )
            .background(color = containerColor, shape = MaterialTheme.shapes.small)
            .padding(vertical = OPTION_ROW_VERTICAL_PADDING_DP.dp)
            .semantics { contentDescription = optionDesc },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null, // click handled by the parent Row's selectable modifier
        )
        Spacer(modifier = Modifier.width(RADIO_TEXT_SPACER_DP.dp))
        Text(
            text = option,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun QuizDisplayScreenPreview() {
    QuizzerTheme {
        QuizDisplayScreen(
            uiState = QuizDisplayUiState(
                questions = listOf(
                    com.quizzer.app.model.QuizQuestion(
                        id = "preview-q1",
                        type = com.quizzer.app.model.QuestionType.MULTIPLE_CHOICE,
                        question = "What is the primary topic discussed in the text?",
                        options = listOf("A) Option A", "B) Option B", "C) Option C", "D) Option D"),
                        answer = "A) Option A",
                        explanation = "The text explicitly mentions Option A as the main topic.",
                        sourceReference = "Introduction",
                    ),
                ),
                currentIndex = 0,
                selectedAnswer = null,
            ),
            onOptionSelected = {},
            onSubmitClicked = {},
            onNextClicked = {},
        )
    }
}


