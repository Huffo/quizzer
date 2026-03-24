# QuizQuestion

## Purpose
Represents a single quiz question generated from a user-supplied text, together with its answer options, correct answer, explanation, and a reference back to the source section so the user can verify and read more.

## Contract

```kotlin
data class QuizQuestion(
    val id: String,              // stable UUID assigned at parse time; used as LazyColumn key
    val type: QuestionType,      // MULTIPLE_CHOICE or TRUE_FALSE
    val question: String,        // non-blank; answerable solely from the supplied text
    val options: List<String>,   // 4 items for MULTIPLE_CHOICE; ["True","False"] for TRUE_FALSE
    val answer: String,          // must be one of options (full text, e.g. "A) Paris")
    val explanation: String,     // non-blank; explains why the answer is correct, citing source text
    val sourceReference: String, // chapter/section title from the source text
)

enum class QuestionType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
}

data class QuizConfig(
    val questionCount: Int = DEFAULT_QUESTION_COUNT,
) {
    companion object {
        const val DEFAULT_QUESTION_COUNT = 5
        const val MAX_QUESTION_COUNT = 20
    }
}
```

## Invariants
- `id` is a non-blank UUID string assigned at parse time; never reused across quiz generations.
- `options.size == 4` when `type == MULTIPLE_CHOICE`.
- `options == listOf("True", "False")` (exactly, in that order) when `type == TRUE_FALSE`.
- `answer` is exactly equal to one element of `options`.
- `question`, `answer`, and `explanation` must be derivable solely from the supplied input text — no external knowledge or inference beyond what the text states.
- `sourceReference` is non-blank. It matches a section/chapter heading found in the source text if one exists; otherwise it is the opening sentence of the nearest paragraph.
- `QuizConfig.questionCount` is in the range `1..MAX_QUESTION_COUNT` inclusive.
- The repository may return fewer questions than `questionCount` requested if the source text does not contain enough verifiable content — this is a partial success, not an error.

## Error Cases
`QuizQuestion` is a pure value type — it carries no error state. Violations of the above invariants (e.g. malformed model output) surface as `QuizError.ParseFailure` in the repository layer.

## Decisions

- **`sourceReference` with no headings**: use the opening sentence of the nearest paragraph. Degrades gracefully and remains human-readable. ✅ Resolved.
- **Fewer questions than requested**: partial success — the repository returns however many verifiable questions were produced. The ViewModel surfaces a count note in the UI when `result.size < config.questionCount`. No error is raised. ✅ Resolved.

## Stability Guarantee
- `QuestionType` is **Evolving** — new subtypes may be added. All `when` expressions on `QuestionType` must be exhaustive (use `else` only where a genuine fallback exists).
- All other fields are **Stable**.

## Required Test Coverage
- [ ] `QuizQuestionTest`: `answer` not in `options` is detectable (for parse validation layer)
- [ ] `QuizQuestionTest`: `options.size == 4` when `type == MULTIPLE_CHOICE`
- [ ] `QuizQuestionTest`: `options == listOf("True","False")` when `type == TRUE_FALSE`
- [ ] `QuizQuestionTest`: data class equality and `copy()` work correctly
- [ ] `QuizConfigTest`: default `questionCount` equals `DEFAULT_QUESTION_COUNT` (5)
- [ ] `QuizConfigTest`: `questionCount` outside `1..MAX_QUESTION_COUNT` is rejected at the parse/validation boundary
