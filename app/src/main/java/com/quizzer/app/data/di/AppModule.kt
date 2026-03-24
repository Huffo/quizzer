package com.quizzer.app.data.di

import com.quizzer.app.data.TextChunker
import com.quizzer.app.data.impl.TextChunkerImpl
import com.quizzer.app.domain.PromptBuilder
import com.quizzer.app.data.impl.PromptBuilderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module for bindings that are identical across all build variants.
 * [QuizGeneratorRepository] and [PdfParser] bindings live in the variant-specific RepositoryModule.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindTextChunker(impl: TextChunkerImpl): TextChunker

    @Binds
    @Singleton
    abstract fun bindPromptBuilder(impl: PromptBuilderImpl): PromptBuilder

    companion object {
        @Provides
        @Singleton
        @Named("IoDispatcher")
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    }
}
