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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.quizzer.app.viewmodel.ScoreViewModel

private const val SCREEN_HORIZONTAL_PADDING_DP = 16
private const val SCREEN_VERTICAL_PADDING_DP = 8
private const val SECTION_SPACING_DP = 8
private const val CARD_INNER_PADDING_DP = 12
private const val CARD_ROW_SPACING_DP = 4

/**
 * Route-level composable for the Score screen.
 *
 * Initialises [ScoreViewModel] with the results once questions and answers are available,
 * wires the "New Quiz" navigation callback, and intercepts the back button (AC6).
 */
@Composable
fun ScoreRoute(
    questions: List<QuizQuestion>,
    answers: List<UserAnswer>,
    onNavigateToNewQuiz: () -> Unit,
    viewModel: ScoreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(questions, answers) {
        if (questions.isNotEmpty()) {
            viewModel.initialise(questions, answers)
        }
    }

    // AC6: back press from score screen goes to text input, not back into the quiz.
    BackHandler { onNavigateToNewQuiz() }

    ScoreScreen(
        uiState = uiState,
        onNewQuizClicked = onNavigateToNewQuiz,
    )
}

/**
 * Stateless score screen composable.
 *
 * Shows the headline score ("X / Y") and a [LazyColumn] breakdown of every question,
 * each row with the user's answer, the correct answer, explanation, and source reference.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreScreen(
    uiState: ScoreUiState,
    onNewQuizClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scoreLabel = stringResource(
        R.string.score_result,
        uiState.correctCount,
        uiState.totalCount,
    )
    val titleLabel = stringResource(R.string.score_title)
    val newQuizLabel = stringResource(R.string.score_new_quiz_button)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(titleLabel) })
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
        ) {
            Text(
                text = scoreLabel,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = scoreLabel },
            )

            Spacer(modifier = Modifier.height(SECTION_SPACING_DP.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp),
            ) {
                items(uiState.breakdownRows, key = { it.question.id }) { row ->
                    ScoreBreakdownCard(row = row)
                }
            }

            Spacer(modifier = Modifier.height(SECTION_SPACING_DP.dp))

            Button(
                onClick = onNewQuizClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = newQuizLabel },
            ) {
                Text(newQuizLabel)
            }

            Spacer(modifier = Modifier.height(SCREEN_VERTICAL_PADDING_DP.dp))
        }
    }
}

/**
 * A card showing one question's breakdown row.
 *
 * Highlights correct rows in [MaterialTheme.colorScheme.primaryContainer] and incorrect
 * rows in [MaterialTheme.colorScheme.errorContainer] (AC3).
 */
@Composable
private fun ScoreBreakdownCard(
    row: ScoreRow,
    modifier: Modifier = Modifier,
) {
    val noAnswerLabel = stringResource(R.string.score_no_answer)
    val yourAnswerLabel = stringResource(R.string.score_your_answer_label)
    val correctAnswerLabel = stringResource(R.string.score_correct_answer_label)
    val explanationLabel = stringResource(R.string.quiz_explanation_label)
    val sourceLabel = stringResource(R.string.quiz_source_reference_label)

    val userAnswerText = row.answer?.selectedOption ?: noAnswerLabel
    val containerColor = if (row.isCorrect) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    // Build a combined content description for screen readers.
    val cardDesc = buildString {
        append(row.question.question)
        append(". ")
        append("$yourAnswerLabel: $userAnswerText. ")
        append("$correctAnswerLabel: ${row.question.answer}. ")
        append("$explanationLabel: ${row.question.explanation}. ")
        append("$sourceLabel: ${row.question.sourceReference}")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardDesc },
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CARD_INNER_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(CARD_ROW_SPACING_DP.dp),
        ) {
            Text(
                text = row.question.question,
                style = MaterialTheme.typography.bodyLarge,
            )

            LabeledRow(label = yourAnswerLabel, value = userAnswerText)
            LabeledRow(label = correctAnswerLabel, value = row.question.answer)
            LabeledRow(label = explanationLabel, value = row.question.explanation)
            LabeledRow(
                label = sourceLabel,
                value = row.question.sourceReference,
            )
        }
    }
}

@Composable
private fun LabeledRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun ScoreScreenPreview() {
    QuizzerTheme {
        val questions = com.quizzer.app.data.FakeQuizGeneratorRepository.FAKE_QUESTIONS
        val answers = questions.mapIndexed { i, q ->
            UserAnswer(
                questionId = q.id,
                selectedOption = if (i % 2 == 0) q.answer else null,
                correctAnswer = q.answer,
            )
        }
        ScoreScreen(
            uiState = ScoreUiState(questions = questions, answers = answers),
            onNewQuizClicked = {},
        )
    }
}
