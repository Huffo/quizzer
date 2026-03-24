---
name: hilt-di
description: 'Wire Hilt dependency injection for this project. Use when adding a new repository, use-case, ViewModel, TextChunker, or PromptBuilder that needs injection. Covers @HiltViewModel, @Inject constructors, @Binds modules, and swapping FakeQuizGeneratorRepository via debug build-variant binding.'
argument-hint: 'Class or interface to wire up'
---

# Hilt Dependency Injection

## When to Use
- Adding a new injectable class (repository, use-case, `TextChunker`, `PromptBuilder`)
- Wiring a ViewModel to its dependencies
- Binding an interface to its implementation in a Hilt module
- Providing `FakeQuizGeneratorRepository` in debug builds or tests

## Conventions
- All ViewModels use `@HiltViewModel` + `@Inject constructor` — never create them manually.
- All injectable classes use `@Inject constructor` — never instantiate them with `SomeClass()`.
- Interface bindings live in `abstract @Module @InstallIn(…)` classes using `@Binds`.
- Singletons use `@Singleton`; ViewModel-scoped dependencies use `@ViewModelScoped`.
- `TextChunker` and `PromptBuilder` are interfaces — bind `TextChunkerImpl` / `PromptBuilderImpl`.
- The `Application` class must be annotated `@HiltAndroidApp`.

## Procedure

### 1. Make a Class Injectable
```kotlin
class TextChunkerImpl @Inject constructor() : TextChunker {
    override fun randomExcerpt(text: String, seed: Long): String { … }
}
```

### 2. Bind Interface → Implementation
Create or update `data/di/DataModule.kt`:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds @Singleton
    abstract fun bindTextChunker(impl: TextChunkerImpl): TextChunker

    @Binds @Singleton
    abstract fun bindPromptBuilder(impl: PromptBuilderImpl): PromptBuilder

    @Binds @Singleton
    abstract fun bindQuizGeneratorRepository(
        impl: AiCoreQuizGeneratorRepository,
    ): QuizGeneratorRepository
}
```

### 3. Wire a ViewModel
```kotlin
@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuizGeneratorRepository,
) : ViewModel() { … }
```

### 4. Swap in a Fake for Debug Builds
In `app/src/debug/java/…/di/DebugDataModule.kt`:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DebugDataModule {

    @Binds @Singleton
    abstract fun bindQuizGeneratorRepository(
        impl: FakeQuizGeneratorRepository,
    ): QuizGeneratorRepository
}
```
Exclude the release module from debug: annotate `DataModule` with
`@Module(…)` and declare separate source sets, or use `replaces` on the debug module.

### 5. Application Class
```kotlin
@HiltAndroidApp
class QuizzerApplication : Application()
```
Register in `AndroidManifest.xml`:
```xml
<application android:name=".QuizzerApplication" … />
```

## Key Dependencies
```kotlin
// build.gradle.kts (app) — prefer version catalog
implementation(libs.hilt.android)
ksp(libs.hilt.compiler)

// plugins block
alias(libs.plugins.hilt)
alias(libs.plugins.ksp)
```
