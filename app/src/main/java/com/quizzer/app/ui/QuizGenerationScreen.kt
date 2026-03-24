package com.quizzer.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.quizzer.app.model.QuizConfig
import com.quizzer.app.model.QuizError
import com.quizzer.app.model.QuizQuestion
import com.quizzer.app.ui.theme.QuizzerTheme
import com.quizzer.app.viewmodel.QuizGenerationViewModel

/**
 * Route-level composable: owns the [QuizGenerationViewModel], triggers generation on entry,
 * handles back-press cancellation, and wires onwards navigation.
 */
@Composable
fun QuizGenerationRoute(
    text: String,
    config: QuizConfig,
    onNavigateToQuiz: (List<QuizQuestion>, partialCount: Int?) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: QuizGenerationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Kick off generation once when the route is first composed.
    LaunchedEffect(Unit) {
        viewModel.generateQuiz(text, config)
    }

    // Back-press while loading cancels generation and pops (AC7).
    BackHandler(enabled = uiState is QuizGenerationUiState.Loading) {
        viewModel.cancelGeneration()
        onNavigateBack()
    }

    // Navigate onwards as soon as a result is ready.
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is QuizGenerationUiState.Success ->
                onNavigateToQuiz(state.questions, null)
            is QuizGenerationUiState.PartialSuccess ->
                onNavigateToQuiz(state.questions, state.requestedCount)
            else -> Unit
        }
    }

    QuizGenerationScreen(
        uiState = uiState,
        onGoBack = {
            viewModel.cancelGeneration()
            onNavigateBack()
        },
    )
}

/**
 * Stateless Quiz Generation screen.
 *
 * Shows either a loading spinner (AC1/AC6) or the appropriate error message (AC4/AC5).
 * Success/PartialSuccess states are handled by [QuizGenerationRoute] via navigation side-effect
 * and this screen will be popped before ever rendering them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizGenerationScreen(
    uiState: QuizGenerationUiState,
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenDesc = stringResource(R.string.app_name)
    val goBackLabel = stringResource(R.string.generation_go_back)

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        modifier = modifier.semantics { contentDescription = screenDesc },
    ) { paddingValues ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
        ) {
            when (uiState) {
                is QuizGenerationUiState.Loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.generation_loading),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                is QuizGenerationUiState.Error -> {
                    val message = when (uiState.error) {
                        is QuizError.ModelUnavailable ->
                            stringResource(R.string.generation_error_model_unavailable)
                        is QuizError.ParseFailure ->
                            stringResource(R.string.generation_error_parse_failure)
                        else ->
                            stringResource(R.string.generation_error_generic)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onGoBack,
                            modifier = Modifier.semantics { contentDescription = goBackLabel },
                        ) {
                            Text(stringResource(R.string.generation_go_back))
                        }
                    }
                }

                // Success/PartialSuccess are handled by navigation in the route;
                // keep the loader visible during the transition frame.
                is QuizGenerationUiState.Success,
                is QuizGenerationUiState.PartialSuccess,
                -> CircularProgressIndicator()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuizGenerationScreenLoadingPreview() {
    QuizzerTheme {
        QuizGenerationScreen(
            uiState = QuizGenerationUiState.Loading,
            onGoBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun QuizGenerationScreenErrorPreview() {
    QuizzerTheme {
        QuizGenerationScreen(
            uiState = QuizGenerationUiState.Error(
                QuizError.ModelUnavailable("Model not downloaded"),
            ),
            onGoBack = {},
        )
    }
}
