# F5: Scoring

## Context
After the last question is submitted, the user sees their total score and a per-question breakdown. Unanswered questions count as wrong. There is no retry. The user can return to text input to start a new quiz.

## Acceptance Criteria
- [ ] AC1: The score screen shows the number of correct answers out of total questions (e.g. "3 / 5").
- [ ] AC2: Every question is listed in the breakdown, showing: the question text, the user's answer (or "No answer" if skipped), the correct answer, and the explanation.
- [ ] AC3: Each question in the breakdown is visually marked as correct or incorrect.
- [ ] AC4: Unanswered questions are marked as incorrect in the breakdown.
- [ ] AC5: A "New Quiz" button returns the user to the text input screen with a blank input field.
- [ ] AC6: The back button on the score screen also returns to text input (not back into the quiz).
- [ ] AC7: The source reference is visible per question in the breakdown.

## Scenarios

### Scenario 1: All correct
**Given** the user answered all 5 questions correctly  
**When** the score screen is shown  
**Then** the score reads "5 / 5"  
**And** all questions are marked correct in the breakdown

### Scenario 2: Mixed results
**Given** the user got 2 correct, 2 wrong, and 1 unanswered  
**When** the score screen is shown  
**Then** the score reads "2 / 5"  
**And** the unanswered question is listed with "No answer" and marked incorrect  
**And** the correct answer and explanation are shown for every question

### Scenario 3: All wrong (including unanswered)
**Given** the user selected wrong answers or submitted without selecting for all 5  
**When** the score screen is shown  
**Then** the score reads "0 / 5"

### Scenario 4: Partial quiz (fewer questions generated)
**Given** only 3 questions were generated (partial success from F2)  
**When** all 3 are submitted  
**Then** the score is out of 3 (e.g. "2 / 3"), not out of the originally requested count

### Scenario 5: New Quiz
**Given** the user is on the score screen  
**When** they tap "New Quiz"  
**Then** they are navigated to text input  
**And** the text field is blank  
**And** the previous quiz state is cleared

### Scenario 6: Back from score screen
**Given** the user is on the score screen  
**When** they press the system back button  
**Then** they are navigated to text input (not back to the last question)

## Out of Scope
- Retrying the same quiz.
- Saving or sharing scores.
- Leaderboards or history.
- Generating a new quiz from the same text automatically.

## Required Test Coverage
- [ ] `ScoreViewModelTest`: correct count equals number of matching `userAnswer == correctAnswer`
- [ ] `ScoreViewModelTest`: unanswered questions (no selection submitted) counted as wrong
- [ ] `ScoreViewModelTest`: score denominator equals actual question count, not requested count
- [ ] `ScoreViewModelTest`: "New Quiz" action clears quiz state
- [ ] `ScoreScreenTest` (Compose): score label shows "X / Y" format
- [ ] `ScoreScreenTest` (Compose): each question row shows question text, user answer, correct answer, explanation, source reference
- [ ] `ScoreScreenTest` (Compose): unanswered question shows "No answer"
- [ ] `ScoreScreenTest` (Compose): "New Quiz" button navigates to text input
