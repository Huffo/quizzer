---
name: android-testing
description: 'Write Android unit and instrumented tests for this project. Use when creating tests for ViewModels, repositories, use-cases, TextChunker, PromptBuilder, or any Composable. Covers JUnit 4/5, Turbine for Flow/StateFlow assertions, Compose test rules, and QuizGeneratorRepository mocking.'
---

# Android Testing

## When to Use
- Adding or editing any function, class, or Composable that needs test coverage
- Writing ViewModel state-transition tests
- Testing `Flow`/`StateFlow` emissions
- Writing Compose UI tests for screens or components
- Mocking `QuizGeneratorRepository` for isolation

## Conventions
- Every function must have at least one corresponding test.
- Unit tests → `src/test/`; Compose/instrumented tests → `src/androidTest/`
- Test class name mirrors the class under test: `TextChunkerTest`, `QuizViewModelTest`, etc.
- Use **JUnit 5** (`@org.junit.jupiter.api.Test`) for all unit tests in `src/test/`.
- Use **JUnit 4** (`@Test`) for instrumented/Compose tests in `src/androidTest/` — Android's test runner requires JUnit 4.
- No magic numbers in tests — use clearly named local `val`s.

## Procedure

### 1. Unit Tests (JUnit + Turbine)
1. Create `<ClassName>Test.kt` in `src/test/java/com/quizzer/app/`.
2. Instantiate the class under test; inject fakes/mocks via constructor.
3. Use `kotlinx.coroutines.test.runTest` for suspend functions.
4. Use Turbine's `flow.test { }` block for `Flow`/`StateFlow`:
   ```kotlin
   viewModel.uiState.test {
       assertThat(awaitItem()).isEqualTo(UiState.Idle)
       viewModel.generateQuiz(inputText)
       assertThat(awaitItem()).isEqualTo(UiState.Loading)
       // …
       cancelAndIgnoreRemainingEvents()
   }
   ```

### 2. ViewModel Tests
1. Use `FakeQuizGeneratorRepository` (always available, see Gemini Nano skill).
2. Provide a `TestCoroutineDispatcher` or `UnconfinedTestDispatcher` via the ViewModel constructor.
3. Assert both happy-path and error-path (`Result.Failure`) state transitions.

### 3. Compose UI Tests
1. Use `createComposeRule()` as the test rule.
2. Set content with `rule.setContent { QuizScreen(viewModel = fakeViewModel) }`.
3. Assert nodes by semantic roles or `contentDescription` — never by raw text strings (use string resource IDs instead).
4. Every interactive element must be findable via `onNodeWithContentDescription`.

### 4. Repository / Use-Case Tests
1. Code against the `QuizGeneratorRepository` interface only.
2. Provide a `FakeQuizGeneratorRepository` that returns predictable `Result.Success` or `Result.Failure`.
3. Test chunking logic in `TextChunkerTest` with fixed seed (`Random(seed = 42)`).

## Key Dependencies
```kotlin
// build.gradle.kts (app) — use version catalog (gradle/libs.versions.toml)
testImplementation(libs.junit.jupiter)
testRuntimeOnly(libs.junit.platform.launcher)
testImplementation(libs.kotlinx.coroutines.test)
testImplementation(libs.turbine)
androidTestImplementation(libs.androidx.compose.ui.test.junit4)

// plugins block
alias(libs.plugins.android.junit5)
```
