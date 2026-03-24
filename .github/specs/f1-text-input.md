# F1: Text Input

## Context
The entry point of the app. The user selects a PDF file and configures the quiz before generation begins. The app extracts text from the PDF on-device and validates it before enabling generation. Getting this right is critical — an unparseable or too-short document is the most common failure mode.

## Acceptance Criteria
- [ ] AC1: The user can pick a PDF file from the device file system using the system file picker.
- [ ] AC2: Once a PDF is picked, the extracted text word count is shown (e.g. "2 340 words extracted").
- [ ] AC3: The user can set the desired number of questions (1–20, default 5).
- [ ] AC4: The user can choose which question types to include: Multiple Choice only, True/False only, or both.
- [ ] AC5: If the extracted text is fewer than 100 words, the Generate button is disabled and an inline message reads "Enter at least 100 words to generate a quiz".
- [ ] AC6: If the PDF cannot be parsed (corrupt file, password-protected, no text layer), an inline error message is shown and the Generate button remains disabled.
- [ ] AC7: Tapping Generate with valid extracted text navigates to the generation loading state.
- [ ] AC8: The user can pick a different PDF at any time, replacing the current extraction.

## Scenarios

### Scenario 1: Valid PDF, default config
**Given** the user picks a PDF with at least 100 words of extractable text  
**When** they leave question count and type at defaults  
**Then** the extracted word count is shown  
**And** the Generate button is enabled  
**And** tapping it triggers quiz generation with `QuizConfig(questionCount = 5)` and both question types

### Scenario 2: Custom question count
**Given** the user has a valid PDF loaded  
**When** they set question count to 10  
**Then** generation is triggered with `QuizConfig(questionCount = 10)`

### Scenario 3: PDF with too little text
**Given** the user picks a PDF that yields fewer than 100 words  
**When** parsing completes  
**Then** the Generate button is disabled  
**And** the inline message reads "Enter at least 100 words to generate a quiz"

### Scenario 4: Unparseable PDF
**Given** the user picks a password-protected or corrupt PDF  
**When** the parser fails  
**Then** an inline error indicates the file could not be read  
**And** the Generate button remains disabled  
**And** the user can pick a different file

### Scenario 5: Re-pick file
**Given** the user has already loaded a PDF  
**When** they pick a different file  
**Then** the previous extraction is discarded and the new file is parsed

### Scenario 6: Parsing in progress
**Given** the user has picked a large PDF  
**When** parsing is running  
**Then** a loading indicator is shown in place of the word count  
**And** the Generate button is disabled until parsing completes

## Out of Scope
- Manual text entry or clipboard paste.
- Importing from URLs, cloud storage, or other file formats (EPUB, DOCX, etc.).
- Saving or persisting the extracted text across app sessions.
- **Image-based questions**: Generating quiz questions from images embedded in a PDF is out of scope. Gemini Nano's multimodal image API is in unstable alpha and would require structural changes across the entire domain model, prompt pipeline, and UI.
- **Scanned PDFs (image-only)**: PDFs with no text layer (scanned documents) are not supported. OCR is a separate pipeline requiring either MLKit or Tesseract and belongs in a future feature.

## Decisions

- **Minimum text length**: 100 words of extracted text. ✅ Resolved.
- **Last question type deselection**: The last active question type cannot be deselected — the UI prevents it. ✅ Resolved.
- **Image questions**: Denied for V1 — out of scope. ✅ Resolved.

## Required Test Coverage
- [ ] `TextInputViewModelTest`: Generate button disabled when no PDF is loaded
- [ ] `TextInputViewModelTest`: Generate button disabled when extracted text is below 100 words
- [ ] `TextInputViewModelTest`: Generate button enabled when extracted text meets minimum
- [ ] `TextInputViewModelTest`: parse error → error state shown, Generate disabled
- [ ] `TextInputViewModelTest`: picking a new PDF replaces the previous extraction
- [ ] `TextInputViewModelTest`: `questionCount` defaults to `QuizConfig.DEFAULT_QUESTION_COUNT`
- [ ] `TextInputViewModelTest`: setting `questionCount` to 0 or 21 is rejected/clamped
- [ ] `PdfParserTest`: valid PDF returns extracted text
- [ ] `PdfParserTest`: password-protected PDF returns `PdfError.PasswordProtected`
- [ ] `PdfParserTest`: corrupt file returns `PdfError.ParseFailure`
- [ ] `PdfParserTest`: image-only PDF (no text layer) returns text below 100 words or empty
- [ ] `TextInputScreenTest` (Compose): Generate button has correct enabled/disabled state in semantics
- [ ] `TextInputScreenTest` (Compose): word count label updates after successful parse
