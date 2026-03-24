---
name: gemini-nano-integration
description: 'Integrate Gemini Nano (AICore) into the app. Use when implementing or modifying QuizGeneratorRepository, PromptBuilder, TextChunker, availability checks, FakeQuizGeneratorRepository, or JSON quiz output parsing. Covers on-device model availability guards, token-window limits, and Kotlinx Serialization.'
---

# Gemini Nano Integration

## When to Use
- Implementing or modifying the `QuizGeneratorRepository` AICore backend
- Building or adjusting `PromptBuilder` or `TextChunker`
- Adding the `FakeQuizGeneratorRepository` for emulator/CI
- Parsing or validating JSON quiz output
- Handling model unavailability gracefully

## Key Constraints
- **No network calls.** This app is fully on-device; never add internet permissions.
- **Context window**: Gemini Nano supports ~4 096 tokens. `TextChunker` must cap each chunk accordingly.
- **Always code against the `QuizGeneratorRepository` interface** — never call AICore directly from a ViewModel.

## Procedure

### 1. Availability Check
Always guard AICore calls before use:
```kotlin
val availability = generativeModel.checkAvailability()
if (availability != AvailabilityStatus.AVAILABLE) {
    return Result.Failure(ModelUnavailableException(availability))
}
```

### 2. TextChunker
- Interface at `data/TextChunker.kt`; implementation is `TextChunkerImpl`, injected via Hilt.
- Splits input into chunks capped at `MAX_CHUNK_TOKENS` (a `const val` in the companion object).
- Accepts an optional `seed: Long` parameter for deterministic selection in tests.
- Returns a random excerpt as a `String`.

```kotlin
interface TextChunker {
    fun randomExcerpt(text: String, seed: Long = Random.nextLong()): String

    companion object {
        const val MAX_CHUNK_TOKENS = 3_000 // leaves room for prompt overhead
    }
}

class TextChunkerImpl @Inject constructor() : TextChunker {
    override fun randomExcerpt(text: String, seed: Long): String { … }
}
```

### 3. PromptBuilder
- Interface at `domain/PromptBuilder.kt`; implementation is `PromptBuilderImpl`, injected via Hilt.
- Accepts the excerpt string and returns a fully-formed prompt `String`.
- Prompt must instruct Gemini Nano to return **only** a JSON array — no markdown fences.

```kotlin
interface PromptBuilder {
    fun build(excerpt: String): String
}

class PromptBuilderImpl @Inject constructor() : PromptBuilder {
    override fun build(excerpt: String): String =
        """
        Generate a 5-question multiple-choice quiz based on the following text.
        Return ONLY a JSON array with this structure (no markdown, no explanation):
        [{"question":"…","options":["A)…","B)…","C)…","D)…"],"answer":"A)…"}]
        
        Text:
        $excerpt
        """.trimIndent()
}
```

### 4. JSON Parsing
Use Kotlinx Serialization. Define:
```kotlin
@Serializable
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val answer: String,
)
```
Parse with:
```kotlin
val questions = Json { ignoreUnknownKeys = true }
    .decodeFromString<List<QuizQuestion>>(rawJson)
```
Wrap in `try/catch` and surface as `Result.Failure(ParseException(…))` on error.

### 5. FakeQuizGeneratorRepository
Provide this for emulator/CI where AICore is unavailable:
```kotlin
class FakeQuizGeneratorRepository(
    private val response: Result<List<QuizQuestion>> = Result.Success(FAKE_QUESTIONS),
) : QuizGeneratorRepository {
    override suspend fun generateQuiz(text: String): Result<List<QuizQuestion>> = response
}
```
Keep `FAKE_QUESTIONS` as a `const`/top-level property in the same file.

## Key Dependencies
```kotlin
// build.gradle.kts (app)
implementation("com.google.ai.edge.aicore:aicore:<version>")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:<version>")
```
