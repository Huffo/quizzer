package com.quizzer.app.data.impl

import android.content.Context
import android.net.Uri
import com.quizzer.app.domain.PdfParser
import com.quizzer.app.model.PdfError
import com.quizzer.app.model.PdfResult
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

/**
 * Production [PdfParser] backed by pdfbox-android.
 *
 * Parses the text layer of the PDF only — image-only (scanned) PDFs will yield
 * blank or very short text, which the caller handles via word-count validation.
 * OCR is explicitly out of scope for V1.
 */
class PdfBoxPdfParser @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher,
) : PdfParser {

    override suspend fun extractText(uri: Uri): PdfResult = withContext(ioDispatcher) {
        PDFBoxResourceLoader.init(context)

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext PdfResult.Failure(PdfError.FileNotAccessible())

            inputStream.use { stream ->
                val document = try {
                    PDDocument.load(stream)
                } catch (e: Exception) {
                    return@use PdfResult.Failure(PdfError.ParseFailure(cause = e))
                }

                document.use { doc ->
                    if (doc.isEncrypted) {
                        return@use PdfResult.Failure(PdfError.PasswordProtected)
                    }
                    val text = PDFTextStripper().getText(doc)
                    PdfResult.Success(text)
                }
            }
        } catch (e: IOException) {
            PdfResult.Failure(PdfError.FileNotAccessible(cause = e))
        } catch (e: Exception) {
            PdfResult.Failure(PdfError.Unknown(cause = e))
        }
    }
}
