package com.quizzer.app.data.di

import com.quizzer.app.data.FakeQuizGeneratorRepository
import com.quizzer.app.domain.QuizGeneratorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Debug-variant binding: wires [QuizGeneratorRepository] to [FakeQuizGeneratorRepository]
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
}
