package com.quizzer.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = QuizzerPrimary,
    onPrimary = QuizzerOnPrimary,
    primaryContainer = QuizzerPrimaryContainer,
    onPrimaryContainer = QuizzerOnPrimaryContainer,
    secondary = QuizzerSecondary,
    onSecondary = QuizzerOnSecondary,
    background = QuizzerBackground,
    onBackground = QuizzerOnBackground,
    surface = QuizzerSurface,
    onSurface = QuizzerOnSurface,
    error = QuizzerError,
    onError = QuizzerOnError,
)

private val DarkColorScheme = darkColorScheme(
    primary = QuizzerPrimaryContainer,
    onPrimary = QuizzerOnPrimaryContainer,
)

@Composable
fun QuizzerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = QuizzerTypography,
        content = content,
    )
}
