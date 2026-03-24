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
import com.quizzer.app.viewmodel.TextInputViewModel
import kotlinx.serialization.Serializable

/**
 * Root navigation host for the app.
 *
 * Nav graph as of F2: TextInput → Generation.
 * Text is retrieved from [TextInputViewModel] via the TextInput back-stack entry
 * rather than encoded in nav args (PDF text can be hundreds of KB).
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
                    // TODO(F3): navController.navigate(Screen.Quiz(…))
                },
                onNavigateBack = { navController.popBackStack() },
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
}

