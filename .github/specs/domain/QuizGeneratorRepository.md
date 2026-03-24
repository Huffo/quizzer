# QuizGeneratorRepository

## Purpose
The single entry point for generating a quiz from user-supplied text; abstracts all AI model interaction so callers (ViewModels) never depend on AICore directly.

## Contract

```kotlin
interface QuizGeneratorRepository {

    /**
     * Generates a quiz from the provided [text] using the given [config].
     *
     * The implementation selects a random excerpt from [text], constructs a prompt,
     * calls the on-device model, and parses the response.
     *
     * Questions are grounded solely in [text]. If the model cannot produce
     * [config.questionCount] verifiable questions, it returns as many as it can
     * (partial success) — this is NOT an error.
     *
     * @param text  The full user-supplied source text. Must not be blank.
     * @param config Quiz generation options; defaults to [QuizConfig].
     * @return [Result.Success] with a non-empty [List] of [QuizQuestion],
     *         or [Result.Failure] wrapping a [QuizError].
     */
    suspend fun generateQuiz(
        text: String,
        config: QuizConfig = QuizConfig(),
    ): Result<List<QuizQuestion>>
}
```

## Invariants
- `text` must not be blank; if it is, the implementation returns `Result.Failure(QuizError.InputTooShort)` without calling the model.
- A `Result.Success` value always contains at least **one** `QuizQuestion`; an empty list is never returned as success.
- Every `QuizQuestion` in a success result satisfies all invariants defined in [QuizQuestion.md](./QuizQuestion.md).
- The implementation never makes network calls.
- The implementation always checks model availability before calling the model; unavailability returns `Result.Failure(QuizError.ModelUnavailable)`.

## Error Cases

| Error | When emitted |
|---|---|
| `QuizError.InputTooShort` | `text` is blank or below `TextChunker.MIN_INPUT_LENGTH` |
| `QuizError.ModelUnavailable` | `checkAvailability() != AVAILABLE` |
| `QuizError.ParseFailure` | Model response cannot be parsed into a valid `List<QuizQuestion>` |
| `QuizError.Unknown` | Any other unhandled exception |

## Stability Guarantee
**Stable.** The signature of `generateQuiz` must not change without updating all ViewModels and fakes in lockstep.

## Fake Implementation Stub

```kotlin
class FakeQuizGeneratorRepository(
    private val response: Result<List<QuizQuestion>> = Result.Success(FAKE_QUESTIONS),
) : QuizGeneratorRepository {

    var lastText: String? = null
        private set

    var lastConfig: QuizConfig? = null
        private set

    override suspend fun generateQuiz(
        text: String,
        config: QuizConfig,
    ): Result<List<QuizQuestion>> {
        lastText = text
        lastConfig = config
        return response
    }
}
```

`lastText` and `lastConfig` allow tests to assert what arguments were passed.

## Required Test Coverage
- [ ] `FakeQuizGeneratorRepositoryTest`: records `lastText` and `lastConfig` on each call
- [ ] `FakeQuizGeneratorRepositoryTest`: returns the configured response (success and failure variants)
- [ ] `QuizViewModelTest`: calls repository with the correct `text` and `config`
- [ ] `QuizViewModelTest`: `Result.Failure(QuizError.ModelUnavailable)` transitions UI to error state
- [ ] `QuizViewModelTest`: partial success (fewer questions than requested) transitions UI to result state with count note
- [ ] `AiCoreQuizGeneratorRepositoryTest`: blank input returns `QuizError.InputTooShort` without calling the model
