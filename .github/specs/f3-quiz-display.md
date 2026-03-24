# F3: Quiz Display

## Context
Each question is shown one at a time. The user must see the question text, all answer options, and the source reference before answering. Navigation between questions follows the answer-submission flow defined in F4.

## Acceptance Criteria
- [ ] AC1: Only one question is visible at a time.
- [ ] AC2: The current question number and total are displayed (e.g. "Question 2 of 5").
- [ ] AC3: The question text is displayed prominently.
- [ ] AC4: All answer options are displayed as selectable items.
- [ ] AC5: The source reference is displayed below the options so the user knows where to verify.
- [ ] AC6: For True/False questions, exactly two options are shown: "True" and "False", in that order.
- [ ] AC7: For Multiple Choice questions, exactly four options are shown.
- [ ] AC8: The user cannot navigate forward without submitting an answer (see F4).
- [ ] AC9: The user cannot navigate backward once an answer has been submitted for a question.

## Scenarios

### Scenario 1: Multiple choice question displayed
**Given** the quiz has loaded and question 1 is a multiple choice question  
**When** the quiz display screen is shown  
**Then** the question text is visible  
**And** 4 options are displayed  
**And** the source reference is visible  
**And** "Question 1 of N" is shown

### Scenario 2: True/False question displayed
**Given** question 2 is a true/false question  
**When** the user reaches question 2  
**Then** exactly 2 options are shown: "True" then "False"  
**And** no other options appear

### Scenario 3: Attempting to advance without selecting
**Given** the user has not selected an answer  
**When** they attempt to move to the next question  
**Then** they cannot proceed  
**And** a prompt indicates they must select an answer first

### Scenario 4: Back navigation blocked after submission
**Given** the user has submitted an answer for question 2 and moved to question 3  
**When** they press back  
**Then** they are not returned to question 2

## Out of Scope
- Scrollable question list (all questions on one screen).
- Ability to skip a question and return to it later.
- Editing or changing a submitted answer.

## Required Test Coverage
- [ ] `QuizDisplayViewModelTest`: exposes correct `currentIndex` and `totalCount`
- [ ] `QuizDisplayViewModelTest`: `currentQuestion` matches the question at `currentIndex`
- [ ] `QuizScreenTest` (Compose): "Question X of Y" label rendered correctly
- [ ] `QuizScreenTest` (Compose): TRUE_FALSE question shows exactly ["True", "False"]
- [ ] `QuizScreenTest` (Compose): MULTIPLE_CHOICE question shows exactly 4 options
- [ ] `QuizScreenTest` (Compose): source reference is visible on screen
- [ ] `QuizScreenTest` (Compose): forward navigation is not available before answer selected
