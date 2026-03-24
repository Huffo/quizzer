package com.quizzer.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizzer.app.domain.PdfParser
import com.quizzer.app.model.PdfResult
import com.quizzer.app.model.QuestionType
import com.quizzer.app.model.QuizConfig
import com.quizzer.app.ui.PdfState
import com.quizzer.app.ui.TextInputUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TextInputViewModel @Inject constructor(
    private val pdfParser: PdfParser,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TextInputUiState())
    val uiState: StateFlow<TextInputUiState> = _uiState.asStateFlow()

    /**
     * One-shot event: emits the [QuizConfig] the user wants to use for generation.
     * Collect this in the UI to trigger navigation to the generation screen.
     */
    private val _navigateToGeneration = Channel<QuizConfig>(Channel.BUFFERED)
    val navigateToGeneration = _navigateToGeneration.receiveAsFlow()

    /** Called when the system file picker returns a PDF URI. */
    fun onPdfSelected(uri: Uri) {
        _uiState.update { it.copy(pdfState = PdfState.Parsing) }
        viewModelScope.launch {
            _uiState.update { state ->
                when (val result = pdfParser.extractText(uri)) {
                    is PdfResult.Success -> {
                        val wordCount = result.text.wordCount()
                        state.copy(pdfState = PdfState.Ready(result.text, wordCount))
                    }
                    is PdfResult.Failure -> state.copy(pdfState = PdfState.ParseError(result.error))
                }
            }
        }
    }

    /**
     * Updates the requested question count, clamping to the valid range
     * [[QuizConfig.MIN_QUESTION_COUNT]..[QuizConfig.MAX_QUESTION_COUNT]].
     */
    fun onQuestionCountChanged(count: Int) {
        _uiState.update { state ->
            state.copy(
                questionCount = count.coerceIn(
                    QuizConfig.MIN_QUESTION_COUNT,
                    QuizConfig.MAX_QUESTION_COUNT,
                ),
            )
        }
    }

    /**
     * Toggles [type] in the set of selected question types.
     * The last remaining type cannot be deselected.
     */
    fun onQuestionTypeToggled(type: QuestionType) {
        _uiState.update { state ->
            val current = state.selectedTypes
            if (type in current && current.size == 1) return@update state
            val updated = if (type in current) current - type else current + type
            state.copy(selectedTypes = updated)
        }
    }

    /** Emits a navigation event only when [TextInputUiState.isGenerateEnabled] is true. */
    fun onGenerateClicked() {
        val state = _uiState.value
        if (!state.isGenerateEnabled) return
        val config = QuizConfig(
            questionCount = state.questionCount,
            questionTypes = state.selectedTypes,
        )
        viewModelScope.launch { _navigateToGeneration.send(config) }
    }
}

/** Returns the number of whitespace-separated tokens in this string. */
private fun String.wordCount(): Int =
    trim().split(Regex("\\s+")).count { it.isNotEmpty() }
