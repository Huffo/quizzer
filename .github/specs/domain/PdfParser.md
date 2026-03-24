# PdfParser

## Purpose
Extracts the text layer from a user-supplied PDF file for use as quiz source material, surfacing typed errors for all known failure modes.

## Contract

```kotlin
/**
 * Extracts plain text from a PDF at the given [uri].
 *
 * Called on a background dispatcher; never on the main thread.
 * Returns a [PdfResult] — never throws.
 */
interface PdfParser {

    suspend fun extractText(uri: Uri): PdfResult
}

sealed class PdfResult {

    /** Successful extraction. [text] is the full extracted text; may be short or empty. */
    data class Success(val text: String) : PdfResult()

    /** A typed failure. */
    data class Failure(val error: PdfError) : PdfResult()
}

sealed class PdfError : Exception() {

    /** The PDF is password-protected and cannot be read without a password. */
    data object PasswordProtected : PdfError()

    /** The PDF file is corrupt or not a valid PDF. */
    data class ParseFailure(override val cause: Throwable? = null) : PdfError()

    /** The requested URI could not be opened (file deleted, permission revoked, etc.). */
    data class FileNotAccessible(override val cause: Throwable? = null) : PdfError()

    /** Any other unhandled error. */
    data class Unknown(override val cause: Throwable) : PdfError()
}
```

## Invariants
- `extractText` never throws; all failures are returned as `PdfResult.Failure`.
- `PdfResult.Success.text` may be blank if the PDF has no text layer (scanned/image-only) — the caller is responsible for checking word count.
- The implementation is fully on-device; no network calls are made.
- Parsing runs on a `CoroutineDispatcher` injected at construction, not on `Dispatchers.Main`.

## Error Cases

| Error | When emitted |
|---|---|
| `PdfError.PasswordProtected` | PDF requires a password to open |
| `PdfError.ParseFailure` | File is not a valid PDF or is structurally corrupt |
| `PdfError.FileNotAccessible` | URI cannot be opened (deleted, permission revoked) |
| `PdfError.Unknown` | Any other unhandled exception |

## Note on Scanned PDFs
A PDF with no text layer (image-only / scanned) will return `PdfResult.Success("")` or very short text. This is not a parser error — the word-count validation in `TextInputViewModel` handles it by showing the "too short" message. OCR support is explicitly out of scope for V1.

## Stability Guarantee
**Stable.** `PdfParser`, `PdfResult`, and `PdfError` must not change without updating `TextInputViewModel`, its tests, and `FakePdfParser`.

## Fake Implementation Stub

```kotlin
class FakePdfParser(
    private val response: PdfResult = PdfResult.Success(FAKE_TEXT),
) : PdfParser {

    var lastUri: Uri? = null
        private set

    override suspend fun extractText(uri: Uri): PdfResult {
        lastUri = uri
        return response
    }

    companion object {
        const val FAKE_TEXT: String = "Fake extracted text. ".repeat(20) // 60 words
    }
}
```

## Required Test Coverage
- [ ] `PdfParserTest`: valid PDF URI returns `PdfResult.Success` with non-blank text
- [ ] `PdfParserTest`: password-protected PDF returns `PdfResult.Failure(PdfError.PasswordProtected)`
- [ ] `PdfParserTest`: corrupt file returns `PdfResult.Failure(PdfError.ParseFailure)`
- [ ] `PdfParserTest`: inaccessible URI returns `PdfResult.Failure(PdfError.FileNotAccessible)`
- [ ] `PdfParserTest`: image-only PDF returns `PdfResult.Success` with blank/short text (not an error)
- [ ] `FakePdfParserTest`: records `lastUri` on each call
- [ ] `TextInputViewModelTest`: `PdfResult.Success` with short text \u2192 Generate disabled
- [ ] `TextInputViewModelTest`: `PdfResult.Failure` \u2192 error state with correct `PdfError` subtype
