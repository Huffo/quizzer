package com.quizzer.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.quizzer.app.model.QuestionType
import com.quizzer.app.model.QuizConfig
import com.quizzer.app.model.UserAnswer
import com.quizzer.app.viewmodel.QuizDisplayViewModel
import com.quizzer.app.viewmodel.QuizGenerationViewModel
import com.quizzer.app.viewmodel.TextInputViewModel
import kotlinx.serialization.Serializable

/**
 * Root navigation host for the app.
 *
 * Nav graph as of F5: TextInput → Generation → Quiz → Score.
 * Large data (PDF text, question list, answer list) is never placed in nav args;
 * instead it is read from the originating screen's [ViewModel] still alive in the
 * back stack.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.TextInput,
    ) {
        composable<Screen.TextInput> {
            TextInputRoute(
                onNavigateToGeneration = { config ->
                    navController.navigate(
                        Screen.Generation(
                            questionCount = config.questionCount,
                            questionTypes = config.questionTypes.joinToString(",") { it.name },
                        ),
                    )
                },
            )
        }

        composable<Screen.Generation> { entry ->
            val args = entry.toRoute<Screen.Generation>()
            val config = QuizConfig(
                questionCount = args.questionCount,
                questionTypes = args.questionTypes
                    .split(",")
                    .mapNotNull { runCatching { QuestionType.valueOf(it) }.getOrNull() }
                    .toSet()
                    .ifEmpty { QuestionType.entries.toSet() },
            )

            // Read the extracted text from the TextInput ViewModel still in the back stack.
            val textInputEntry = navController.getBackStackEntry<Screen.TextInput>()
            val textInputViewModel: TextInputViewModel = hiltViewModel(textInputEntry)
            val textInputState by textInputViewModel.uiState.collectAsStateWithLifecycle()
            val text = (textInputState.pdfState as? PdfState.Ready)?.text ?: ""

            QuizGenerationRoute(
                text = text,
                config = config,
                onNavigateToQuiz = { _, _ ->
                    navController.navigate(Screen.Quiz)
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Screen.Quiz> {
            // Read the question list from the Generation ViewModel still in the back stack.
            val generationEntry = navController.getBackStackEntry<Screen.Generation>()
            val generationVm: QuizGenerationViewModel = hiltViewModel(generationEntry)
            val genState by generationVm.uiState.collectAsStateWithLifecycle()

            val questions = when (val s = genState) {
                is QuizGenerationUiState.Success -> s.questions
                is QuizGenerationUiState.PartialSuccess -> s.questions
                else -> emptyList()
            }
            val partialCount = (genState as? QuizGenerationUiState.PartialSuccess)?.requestedCount

            QuizDisplayRoute(
                questions = questions,
                partialCount = partialCount,
                onNavigateToScore = { _ ->
                    navController.navigate(Screen.Score)
                },
            )
        }

        composable<Screen.Score> {
            // Read questions from the Generation ViewModel and answers from the Quiz
            // ViewModel — both still alive in the back stack.
            val generationEntry = navController.getBackStackEntry<Screen.Generation>()
            val generationVm: QuizGenerationViewModel = hiltViewModel(generationEntry)
            val genState by generationVm.uiState.collectAsStateWithLifecycle()
            val questions = when (val s = genState) {
                is QuizGenerationUiState.Success -> s.questions
                is QuizGenerationUiState.PartialSuccess -> s.questions
                else -> emptyList()
            }

            val quizEntry = navController.getBackStackEntry<Screen.Quiz>()
            val quizDisplayVm: QuizDisplayViewModel = hiltViewModel(quizEntry)

            // The answers were passed via the navigateToScore channel, but ScoreViewModel
            // reads them directly from the quiz ViewModel's collected answers exposed
            // through the navigation event.  We need to pass them as state.  Since the
            // QuizDisplayViewModel no longer exposes the collected list directly, we
            // re-derive it from the quizDisplayVm's existing navigateToScore channel by
            // holding the answers in the ScoreViewModel (initialised via answersFromQuiz).
            val answersFromQuiz = quizDisplayVm.collectedAnswers

            ScoreRoute(
                questions = questions,
                answers = answersFromQuiz,
                onNavigateToNewQuiz = {
                    navController.navigate(Screen.TextInput) {
                        popUpTo(Screen.TextInput) { inclusive = true }
                    }
                },
            )
        }
    }
}

/** Type-safe navigation route definitions. Each entry must be @Serializable. */
private sealed interface Screen {

    @Serializable
    data object TextInput : Screen

    /**
     * Quiz generation screen.
     *
     * [questionTypes] is a comma-joined list of [QuestionType] names.
     * The full extracted text is NOT in nav args; it is read from the
     * [TextInputViewModel] still alive in the back stack.
     */
    @Serializable
    data class Generation(
        val questionCount: Int,
        val questionTypes: String,
    ) : Screen

    /**
     * Quiz display screen.
     *
     * Questions are NOT passed as nav args; they are read from the
     * [QuizGenerationViewModel] still alive in the [Generation] back-stack entry.
     */
    @Serializable
    data object Quiz : Screen

    /**
     * Score screen.
     *
     * Questions and answers are NOT passed as nav args; they are read from the
     * [QuizGenerationViewModel] and [QuizDisplayViewModel] still alive in the back stack.
     */
    @Serializable
    data object Score : Screen
}

