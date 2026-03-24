# Quizzer — Copilot Instructions

## Project Overview
Android app that accepts large text input and uses **Gemini Nano** (on-device AI) to generate a short, randomised quiz from excerpts of that text.

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM (ViewModel + StateFlow)
- **AI**: Gemini Nano via the Android `com.google.ai.edge.aicore` (AICore) API
- **Target Device**: Pixel 10; **Min SDK**: 34 (Android 14); **Target SDK**: 36 (Android 16)
- **Build**: Gradle Kotlin DSL (`build.gradle.kts`)
- **DI**: Hilt
- **Testing**: JUnit 5 (unit tests), JUnit 4 (instrumented tests), Turbine (Flow testing), Espresso / Compose test rules

## Project Structure
```
app/
  src/
    main/
      java/com/quizzer/app/
        ui/          # Composables and screen-level UI
        viewmodel/   # ViewModels
        domain/      # Use-cases and QuizGenerator interface
        data/        # AICore repository impl, text chunking
        model/       # Data classes (QuizQuestion, QuizResult, …)
      res/
      AndroidManifest.xml
    test/            # Unit tests
    androidTest/     # Instrumented / Compose UI tests
```

## Key Conventions
- All public APIs use `suspend` functions or return `Flow`; never block the main thread.
- Gemini Nano is accessed through a `QuizGeneratorRepository` interface — always code against the interface so the mock/test double can be swapped in.
- Text chunking lives in `data/TextChunker.kt`; random excerpt selection is seeded from `kotlin.random.Random` unless a seed is explicitly passed (for tests).
- Prompt construction is isolated in `domain/PromptBuilder.kt`.
- `TextChunker` and `PromptBuilder` are interfaces injected via Hilt — never implement them as `object` singletons.
- Use Hilt for all dependency injection; see the `hilt-di` skill for module setup and build-variant bindings.
- Errors surface as a sealed `Result<T>` type — never swallow exceptions silently.
- Use `@StringRes` / string resources for all user-visible strings; no hardcoded English strings in Kotlin.

## Gemini Nano Integration Notes
- AICore requires an on-device model download; always guard calls with an availability check (`GenerativeModel.checkAvailability()`).
- Provide a `FakeQuizGeneratorRepository` for emulator/CI builds where AICore is unavailable.
- The prompt must fit within Gemini Nano's context window (~4 096 tokens); `TextChunker` should cap chunks accordingly.
- Quiz output format: JSON array of `{ question, options: [A,B,C,D], answer }` — validate/parse with Kotlinx Serialization.

## Code Style
- Follow [Google's Kotlin style guide](https://developer.android.com/kotlin/style-guide) at all times.
- No magic numbers — every numeric or string literal that carries meaning must be extracted into a named constant (`const val` or a companion object).
- Write a unit test for every function. Tests live in `src/test/` (unit) or `src/androidTest/` (instrumented); use JUnit 4/5 and Turbine for Flow assertions.

## Specification-Driven Development
- Every feature starts with a spec in `.github/specs/` — use the `feature-spec` skill.
- Every new domain model or interface starts with a contract in `.github/specs/domain/` — use the `domain-model-spec` skill.
- Copilot must act as a requirements engineer when reviewing specs: check for completeness, unambiguity, consistency, testability, and scope. Flag issues with `> ⚠️ RE Review:` before any code is written.
- No implementation is written until the spec exists and all review flags are resolved.

## What Copilot Should Prioritise
1. Keep the `QuizGeneratorRepository` interface stable; changes ripple across ViewModel and tests.
2. Prefer `LazyColumn` for rendering quiz questions (list can grow).
3. Accessibility: every interactive Composable must have a `contentDescription` or `semantics` block.
4. Do not add network permissions or remote API calls — this app is intentionally fully on-device.
