package com.quizzer.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quizzer.app.R
import com.quizzer.app.model.PdfError
import com.quizzer.app.model.QuestionType
import com.quizzer.app.model.QuizConfig
import com.quizzer.app.ui.theme.QuizzerTheme
import com.quizzer.app.viewmodel.TextInputViewModel
import kotlin.math.roundToInt

private const val MIME_TYPE_PDF = "application/pdf"

/**
 * Route-level composable: owns the [TextInputViewModel] and wires navigation.
 * Delegates all rendering to the stateless [TextInputScreen].
 */
@Composable
fun TextInputRoute(
    onNavigateToGeneration: (QuizConfig) -> Unit,
    viewModel: TextInputViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigateToGeneration.collect(onNavigateToGeneration)
    }

    TextInputScreen(
        uiState = uiState,
        onPdfSelected = viewModel::onPdfSelected,
        onQuestionCountChanged = viewModel::onQuestionCountChanged,
        onQuestionTypeToggled = viewModel::onQuestionTypeToggled,
        onGenerateClicked = viewModel::onGenerateClicked,
    )
}

/**
 * Stateless Text Input screen.
 *
 * Receives all state and events as parameters so it can be tested independently
 * from [TextInputViewModel] using [createComposeRule].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputScreen(
    uiState: TextInputUiState,
    onPdfSelected: (Uri) -> Unit,
    onQuestionCountChanged: (Int) -> Unit,
    onQuestionTypeToggled: (QuestionType) -> Unit,
    onGenerateClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pickPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(onPdfSelected) }

    val screenDesc = stringResource(R.string.text_input_screen_content_description)
    val pickPdfDesc = stringResource(R.string.text_input_pick_pdf_button)
    val generateDesc = stringResource(R.string.text_input_generate_button)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
        modifier = modifier.semantics { contentDescription = screenDesc },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { pickPdfLauncher.launch(arrayOf(MIME_TYPE_PDF)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = pickPdfDesc },
            ) {
                Text(stringResource(R.string.text_input_pick_pdf_button))
            }

            PdfStateSection(uiState.pdfState)

            // Shown when the PDF parsed but yielded fewer than the minimum words.
            if (uiState.pdfState is PdfState.Ready && !uiState.isGenerateEnabled) {
                Text(
                    text = stringResource(R.string.text_input_too_short_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            HorizontalDivider()

            QuestionCountSection(
                questionCount = uiState.questionCount,
                onQuestionCountChanged = onQuestionCountChanged,
            )

            QuestionTypesSection(
                selectedTypes = uiState.selectedTypes,
                onQuestionTypeToggled = onQuestionTypeToggled,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onGenerateClicked,
                enabled = uiState.isGenerateEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = generateDesc },
            ) {
                Text(stringResource(R.string.text_input_generate_button))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PdfStateSection(pdfState: PdfState) {
    when (pdfState) {
        PdfState.Idle -> Text(
            text = stringResource(R.string.text_input_no_pdf_selected),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        PdfState.Parsing -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Text(
                text = stringResource(R.string.text_input_pdf_parsing),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        is PdfState.Ready -> Text(
            text = stringResource(R.string.text_input_word_count, pdfState.wordCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        is PdfState.ParseError -> {
            val errorText = when (pdfState.error) {
                PdfError.PasswordProtected ->
                    stringResource(R.string.text_input_pdf_error_password)
                is PdfError.ParseFailure ->
                    stringResource(R.string.text_input_pdf_error_corrupt)
                is PdfError.FileNotAccessible ->
                    stringResource(R.string.text_input_pdf_error_generic)
                is PdfError.Unknown ->
                    stringResource(R.string.text_input_pdf_error_generic)
            }
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun QuestionCountSection(
    questionCount: Int,
    onQuestionCountChanged: (Int) -> Unit,
) {
    val label = stringResource(R.string.text_input_question_count_label)
    val semanticLabel = stringResource(R.string.text_input_question_count_with_value, questionCount)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(text = questionCount.toString(), style = MaterialTheme.typography.bodyLarge)
        }
        Slider(
            value = questionCount.toFloat(),
            onValueChange = { onQuestionCountChanged(it.roundToInt()) },
            valueRange = QuizConfig.MIN_QUESTION_COUNT.toFloat()..QuizConfig.MAX_QUESTION_COUNT.toFloat(),
            steps = QuizConfig.MAX_QUESTION_COUNT - QuizConfig.MIN_QUESTION_COUNT - 1,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = semanticLabel },
        )
    }
}

@Composable
private fun QuestionTypesSection(
    selectedTypes: Set<QuestionType>,
    onQuestionTypeToggled: (QuestionType) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.text_input_question_types_label),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        QuestionType.entries.forEach { type ->
            val typeLabel = when (type) {
                QuestionType.MULTIPLE_CHOICE ->
                    stringResource(R.string.text_input_type_multiple_choice)
                QuestionType.TRUE_FALSE ->
                    stringResource(R.string.text_input_type_true_false)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClickLabel = typeLabel,
                        onClick = { onQuestionTypeToggled(type) },
                    )
                    .semantics(mergeDescendants = true) {},
            ) {
                Checkbox(
                    checked = type in selectedTypes,
                    onCheckedChange = null, // click handled by the Row
                )
                Text(
                    text = typeLabel,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TextInputScreenIdlePreview() {
    QuizzerTheme {
        TextInputScreen(
            uiState = TextInputUiState(),
            onPdfSelected = {},
            onQuestionCountChanged = {},
            onQuestionTypeToggled = {},
            onGenerateClicked = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextInputScreenReadyPreview() {
    QuizzerTheme {
        TextInputScreen(
            uiState = TextInputUiState(
                pdfState = PdfState.Ready(text = "", wordCount = 1_234),
            ),
            onPdfSelected = {},
            onQuestionCountChanged = {},
            onQuestionTypeToggled = {},
            onGenerateClicked = {},
        )
    }
}
