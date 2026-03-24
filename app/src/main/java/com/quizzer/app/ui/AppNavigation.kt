package com.quizzer.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quizzer.app.model.QuizConfig
import kotlinx.serialization.Serializable

/**
 * Root navigation host for the app.
 *
 * Each feature must add its [Screen] route and composable destination here.
 * Nav graph as of F1: TextInput (start).
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
                onNavigateToGeneration = { _: QuizConfig ->
                    // TODO(F2): navController.navigate(Screen.Generation(config))
                },
            )
        }
    }
}

/** Type-safe navigation route definitions. Each entry must be @Serializable. */
private sealed interface Screen {

    @Serializable
    data object TextInput : Screen
}
