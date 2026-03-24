# QuizError — Error Type Hierarchy

## Purpose
Defines all typed failure cases that `QuizGeneratorRepository` and its dependencies surface as `Result.Failure`, so callers can handle every error path exhaustively.

## Contract

```kotlin
sealed class QuizError : Exception() {

    /** AICore reports the model is not downloaded or not supported on this device. */
    data class ModelUnavailable(val status: AvailabilityStatus) : QuizError()

    /** The model returned a response that could not be parsed as a valid quiz JSON array. */
    data class ParseFailure(val raw: String, override val cause: Throwable?) : QuizError()

    /** The input text is blank or too short for TextChunker to produce a meaningful excerpt. */
    data object InputTooShort : QuizError()

    /** An unexpected error occurred during generation. */
    data class Unknown(override val cause: Throwable) : QuizError()
}
```

## Invariants
- Every `Result.Failure` produced by any layer of the app wraps a `QuizError` subtype — never a raw `Exception`.
- `ModelUnavailable` is only emitted when `checkAvailability()` returns a non-`AVAILABLE` status.
- `ParseFailure.raw` contains the unmodified model output to aid debugging; never truncate it.
- `InputTooShort` is emitted before any AICore call is made — it is a pre-condition failure.

## Error Cases

| Subtype | Emitted when |
|---|---|
| `ModelUnavailable` | `checkAvailability() != AVAILABLE` |
| `ParseFailure` | Kotlinx Serialization throws on model output |
| `InputTooShort` | Input text is blank or below `TextChunker.MIN_INPUT_LENGTH` |
| `Unknown` | Any other unhandled exception |

## Stability Guarantee
**Evolving** — new subtypes may be added as additional failure modes are discovered. All call sites must use an exhaustive `when` expression; add an `else` branch only where a future-proof fallback is genuinely appropriate.

## Required Test Coverage
- [ ] `QuizErrorTest`: each subtype can be instantiated and compared for equality
- [ ] `AiCoreQuizGeneratorRepositoryTest`: `ModelUnavailable` emitted when availability check fails
- [ ] `AiCoreQuizGeneratorRepositoryTest`: `ParseFailure` emitted when model returns malformed JSON
- [ ] `TextChunkerTest`: `InputTooShort` emitted for blank / very short input
