# Quizzer

Android app for **Pixel 10 / Android 16**. Paste any large text and Quizzer uses **Gemini Nano** (on-device AI) to generate a randomised multiple-choice quiz from random excerpts of that text. Everything runs fully on-device — no network calls, no data leaves the phone.

## Target Device
- **Device**: Pixel 10 (or any Android 16 device with Gemini Nano system support)
- **Min SDK**: 34 (Android 14)
- **Target SDK**: 36 (Android 16)

## Prerequisites
- Android Studio Meerkat or later
- JDK 21
- Android SDK 36
- A Pixel 10 (or compatible device) with the Gemini Nano model downloaded via AICore

> **Emulator / CI**: AICore is unavailable on emulators. The app automatically falls back to `FakeQuizGeneratorRepository`, which returns a canned quiz so all screens remain testable without hardware.

## Architecture

MVVM with a strict layered separation:

```
ui/ ──> ViewModel ──> domain (use-cases + interfaces) ──> data (AICore impl)
```

| Layer | Contents |
|---|---|
| `ui/` | Jetpack Compose screens, Material 3 components |
| `viewmodel/` | `@HiltViewModel` classes, `StateFlow` state |
| `domain/` | `QuizGeneratorRepository` interface, `PromptBuilder` interface, use-cases |
| `data/` | AICore implementation, `TextChunker`, Hilt modules |
| `model/` | `QuizQuestion`, `QuizResult`, `QuizError` |

## Development Workflow

This project is **specification-driven** — no implementation is written without a spec:

1. New feature → create a spec in `.github/specs/` using the `feature-spec` Copilot skill.
2. New domain model or interface → create a contract in `.github/specs/domain/` using the `domain-model-spec` skill.
3. All specs are reviewed against completeness, unambiguity, consistency, and testability before any code is written.

Available Copilot skills in `.github/skills/`:

| Skill | Use when |
|---|---|
| `feature-spec` | Starting any new feature |
| `domain-model-spec` | Defining a model, interface, or error type |
| `android-testing` | Writing any test |
| `compose-ui` | Creating or editing any Composable |
| `gemini-nano-integration` | Touching AICore, prompts, or JSON parsing |
| `hilt-di` | Wiring up dependency injection |

## Build

```bash
./gradlew assembleDebug
```

## Tests

```bash
# Unit tests (JUnit 5)
./gradlew test

# Lint
./gradlew lint

# Instrumented tests (requires connected Pixel 10)
./gradlew connectedAndroidTest
```

CI runs unit tests and lint automatically on every push and pull request via GitHub Actions.

## License

TBD
