# F1: Text Input

## Context
The entry point of the app. The user provides the source text and configures the quiz before generation begins. Getting this right is critical — insufficient or misconfigured input is the most common failure mode.

## Acceptance Criteria
- [ ] AC1: The user can paste or type a large body of text into an input field.
- [ ] AC2: The user can set the desired number of questions (1–20, default 5).
- [ ] AC3: The user can choose which question types to include: Multiple Choice only, True/False only, or both.
- [ ] AC4: If the text is too short, the Generate button is disabled and an inline message explains the minimum length.
- [ ] AC5: Tapping Generate with valid input navigates to the generation loading state.

## Scenarios

### Scenario 1: Valid input, default config
**Given** the user has pasted text that meets the minimum length  
**When** they leave question count and type at defaults  
**Then** the Generate button is enabled  
**And** tapping it triggers quiz generation with `QuizConfig(questionCount = 5)` and both question types

### Scenario 2: Custom question count
**Given** the user has valid text  
**When** they set question count to 10  
**Then** generation is triggered with `QuizConfig(questionCount = 10)`

### Scenario 3: Text too short
**Given** the user has typed text below the minimum length  
**When** they view the screen  
**Then** the Generate button is disabled  
**And** an inline message states the text is too short to generate a quiz

### Scenario 4: Text cleared after being valid
**Given** the Generate button was previously enabled  
**When** the user clears the text field  
**Then** the Generate button becomes disabled immediately

## Out of Scope
- Importing text from files, URLs, or clipboard auto-detection.
- Saving or persisting input text across app sessions.

## Open Questions

> ⚠️ RE Review: **Minimum text length threshold.** AC4 references a minimum but does not specify the value. `TextChunker.MIN_INPUT_LENGTH` is the implementation constant, but the spec must define the user-facing threshold (e.g. "at least 200 characters" or "at least 50 words") so the UI message can be written and tested without reading implementation code. **Decision needed.**

> ⚠️ RE Review: **Question type selector UX.** AC3 says the user can pick "Multiple Choice only, True/False only, or both" — but how is this surfaced? A toggle group with three options? Two checkboxes? This does not need an implementation answer, but the behaviour when the user deselects both types (making the selection invalid) needs a rule: disable Generate, or prevent the last type from being deselected?

## Required Test Coverage
- [ ] `TextInputViewModelTest`: Generate button disabled when text is blank
- [ ] `TextInputViewModelTest`: Generate button disabled when text is below minimum length
- [ ] `TextInputViewModelTest`: Generate button enabled when text meets minimum length
- [ ] `TextInputViewModelTest`: `questionCount` defaults to `QuizConfig.DEFAULT_QUESTION_COUNT`
- [ ] `TextInputViewModelTest`: setting `questionCount` to 0 or 21 is rejected/clamped
- [ ] `TextInputScreenTest` (Compose): Generate button has correct enabled/disabled state reflected in semantics
