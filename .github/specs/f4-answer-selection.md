# F4: Answer Selection

## Context
The user selects one answer per question and submits it immediately. Feedback (correct/incorrect) is shown after submission. An unanswered question (no selection before submission) counts as wrong. Once submitted, the answer cannot be changed.

## Acceptance Criteria
- [ ] AC1: The user can select one option per question; selecting a new option deselects the previous one.
- [ ] AC2: A "Submit Answer" action is available once an option is selected.
- [ ] AC3: If the user submits without selecting an option, the question is marked wrong and the correct answer is revealed.
- [ ] AC4: After submission, the selected option is highlighted as correct or incorrect.
- [ ] AC5: The correct answer is always revealed after submission, regardless of whether the user was right or wrong.
- [ ] AC6: The explanation is shown after submission.
- [ ] AC7: Once submitted, no option can be selected or changed.
- [ ] AC8: After viewing the result, a "Next" action advances to the following question (or to the score screen if it was the last question).

## Scenarios

### Scenario 1: Correct answer selected
**Given** the user is on question 3 and selects the correct option  
**When** they submit  
**Then** their selection is highlighted as correct  
**And** the explanation is shown  
**And** the source reference remains visible  
**And** "Next" is available

### Scenario 2: Wrong answer selected
**Given** the user selects an incorrect option  
**When** they submit  
**Then** their selection is highlighted as wrong  
**And** the correct option is highlighted  
**And** the explanation is shown  
**And** "Next" is available

### Scenario 3: Submit with no selection
**Given** the user has not selected any option  
**When** they submit (e.g. via a visible "Submit" button that is always tappable)  
**Then** the question is recorded as wrong  
**And** the correct answer is revealed  
**And** the explanation is shown

### Scenario 4: Last question submitted
**Given** the user submits the answer to the last question  
**When** they tap "Next"  
**Then** they are navigated to the score screen (F5)

## Out of Scope
- Changing a submitted answer.
- Skipping a question to answer later.
- Timer-based auto-submission.

## Open Questions

> ⚠️ RE Review: **Submit with no selection mechanics (Scenario 3).** For this to work, "Submit" must be tappable even with no option selected. This contradicts a common UX pattern where Submit is disabled until a selection is made. The spec intentionally allows no-selection submission (counts as wrong). This must be explicitly confirmed to avoid an implementer defaulting to the "disabled until selected" pattern. **Confirm the Submit button is always enabled.**

## Required Test Coverage
- [ ] `QuizViewModelTest`: selecting an option updates selected answer state
- [ ] `QuizViewModelTest`: selecting a second option replaces the first
- [ ] `QuizViewModelTest`: submit with selection records answer and emits feedback state
- [ ] `QuizViewModelTest`: submit with no selection records wrong answer, reveals correct answer
- [ ] `QuizViewModelTest`: options are not selectable after submission
- [ ] `QuizViewModelTest`: "Next" on last question transitions to score state
- [ ] `QuizScreenTest` (Compose): correct answer highlighted after correct submission
- [ ] `QuizScreenTest` (Compose): wrong answer and correct answer both highlighted after wrong submission
- [ ] `QuizScreenTest` (Compose): explanation visible after submission
