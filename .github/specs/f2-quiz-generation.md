# F2: Quiz Generation

## Context
After the user submits valid input, the app requests a quiz from the on-device model. This feature covers the loading experience and all outcomes: success, partial success, and every error state. The user must always know what happened and what they can do next.

## Acceptance Criteria
- [ ] AC1: A loading indicator is shown immediately after the user triggers generation.
- [ ] AC2: On success, the user is navigated to the first question of the quiz.
- [ ] AC3: If fewer questions were generated than requested, a subtle note is shown on the first question screen (not a blocking error).
- [ ] AC4: If the model is unavailable, a clear error message is shown with a "Go back" action.
- [ ] AC5: If the model output cannot be parsed, a clear error message is shown with a "Go back" action.
- [ ] AC6: While generation is in progress, the user cannot trigger a second generation.
- [ ] AC7: The loading screen is not dismissible by back-press; back cancels the generation and returns to text input.

## Scenarios

### Scenario 1: Successful generation (exact count)
**Given** the user has submitted valid text requesting 5 questions  
**When** the model returns 5 valid questions  
**Then** the loading indicator disappears  
**And** the user sees question 1 of 5

### Scenario 2: Partial success
**Given** the user requested 5 questions  
**When** the model returns only 3 verifiable questions  
**Then** the user is navigated to question 1  
**And** a note reads "3 questions generated from your text" (not an error state)

### Scenario 3: Model unavailable
**Given** the on-device model is not downloaded  
**When** generation is triggered  
**Then** the loading indicator is replaced by an error message describing model unavailability  
**And** a "Go back" button returns the user to text input with their text preserved

### Scenario 4: Parse failure
**Given** the model returns a malformed response  
**When** parsing fails  
**Then** an error message is shown  
**And** a "Go back" button returns the user to text input with their text preserved

### Scenario 5: Back press during loading
**Given** generation is in progress  
**When** the user presses back  
**Then** generation is cancelled  
**And** the user is returned to text input with their text preserved

## Out of Scope
- Retry logic (user goes back and resubmits manually).
- Progress indication beyond a generic loading spinner (e.g. "Generating question 2 of 5…").

## Decisions

- **Text preservation on error/back**: The `TextInput` screen stays in the back stack. Navigating to `Generation` pushes on top of it. Pressing back or tapping "Go back" pops the `Generation` screen, returning to `TextInput` with its `TextInputViewModel` still alive. No explicit state restoration is required. ✅ Resolved (Option A).

## Required Test Coverage
- [ ] `QuizGeneratorViewModelTest`: emits `Loading` state immediately on `generateQuiz()` call
- [ ] `QuizGeneratorViewModelTest`: emits `Success` state with questions on repository success
- [ ] `QuizGeneratorViewModelTest`: emits `PartialSuccess` state when result count < requested count
- [ ] `QuizGeneratorViewModelTest`: emits `Error(ModelUnavailable)` on `QuizError.ModelUnavailable`
- [ ] `QuizGeneratorViewModelTest`: emits `Error(ParseFailure)` on `QuizError.ParseFailure`
- [ ] `QuizGeneratorViewModelTest`: second `generateQuiz()` call while loading is ignored
- [ ] `QuizGeneratorViewModelTest`: `cancel()` while loading returns to `Idle` state
