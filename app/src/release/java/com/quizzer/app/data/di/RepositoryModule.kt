package com.quizzer.app.data.di

import com.quizzer.app.data.impl.AiCoreQuizGeneratorRepository
import com.quizzer.app.domain.QuizGeneratorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Release-variant binding: wires [QuizGeneratorRepository] to the real AICore implementation.
 * The debug source set provides its own [RepositoryModule] that binds [FakeQuizGeneratorRepository].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQuizGeneratorRepository(
        impl: AiCoreQuizGeneratorRepository,
    ): QuizGeneratorRepository
}
