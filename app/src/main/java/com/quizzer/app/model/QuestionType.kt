package com.quizzer.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class QuestionType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
}
