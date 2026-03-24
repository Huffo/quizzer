package com.quizzer.app.data.di

import com.quizzer.app.data.impl.AiCoreQuizGeneratorRepository
import com.quizzer.app.data.impl.PdfBoxPdfParser
import com.quizzer.app.domain.PdfParser
import com.quizzer.app.domain.QuizGeneratorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Release-variant bindings: wires repository interfaces to their real implementations.
 * The debug source set provides its own [RepositoryModule] that binds the Fake test doubles.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQuizGeneratorRepository(
        impl: AiCoreQuizGeneratorRepository,
    ): QuizGeneratorRepository

    @Binds
    @Singleton
    abstract fun bindPdfParser(impl: PdfBoxPdfParser): PdfParser
}
