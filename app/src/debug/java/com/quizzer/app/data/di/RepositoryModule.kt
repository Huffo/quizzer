package com.quizzer.app.data.di

import com.quizzer.app.data.FakePdfParser
import com.quizzer.app.data.FakeQuizGeneratorRepository
import com.quizzer.app.domain.PdfParser
import com.quizzer.app.domain.QuizGeneratorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Debug-variant bindings: wires repository interfaces to their Fake test doubles
 * so the app runs on emulators and CI where AICore is unavailable.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQuizGeneratorRepository(
        impl: FakeQuizGeneratorRepository,
    ): QuizGeneratorRepository

    @Binds
    @Singleton
    abstract fun bindPdfParser(impl: FakePdfParser): PdfParser
}
