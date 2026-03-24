---
name: domain-model-spec
description: 'Design and review domain models, interfaces, and contracts before implementation. Use when defining data classes, sealed Result types, repository interfaces, or use-case signatures. Copilot acts as a requirements engineer: it reviews the contract for correctness, completeness, and stability before any implementation is written.'
argument-hint: 'Model or interface name'
---

# Domain Model Specification

## When to Use
- Defining a new data class, sealed class, or enum
- Specifying a repository or use-case interface
- Reviewing an existing interface for contract gaps
- Ensuring the domain layer is stable before wiring ViewModels or data sources

## Core Rule
**Interface-first, implementation-second.** The contract is written and reviewed before any class that implements or depends on it is coded.

## Procedure

### 1. Draft the Contract
Create `.github/specs/domain/<ModelOrInterface>.md` using the template:

```markdown
# <Model / Interface Name>

## Purpose
One sentence: what does this type represent or do?

## Contract

### Data Class / Sealed Class
```kotlin
// Illustrative — property names, types, nullability, and invariants only.
// Do not include method bodies or implementation details.
data class QuizQuestion(
    val question: String,       // non-blank
    val options: List<String>,  // exactly 4 elements
    val answer: String,         // must be one of options
)
```

### Interface
```kotlin
interface QuizGeneratorRepository {
    /**
     * Generates a quiz from [text].
     * @return [Result.Success] with a non-empty list of [QuizQuestion],
     *         or [Result.Failure] with a typed exception.
     */
    suspend fun generateQuiz(text: String): Result<List<QuizQuestion>>
}
```

## Invariants
- List every rule that must always hold (e.g. "options always has exactly 4 elements").

## Error Cases
- List every `Result.Failure` subtype this contract can return and when.

## Stability Guarantee
- **Stable**: callers may depend on this without expecting breakage.
- **Evolving**: subject to change; callers must be updated in lockstep.
```

### 2. Requirements Engineering Review
**Act as a requirements engineer** and check the contract against:

| Check | Question to ask |
|-------|----------------|
| **Correctness** | Do the types, nullability, and invariants accurately model the domain? |
| **Completeness** | Are all error cases listed? Is every observable behaviour covered by the interface? |
| **Stability** | Will this contract remain valid as the feature set grows? Flag any properties likely to change. |
| **Minimality** | Does the interface expose only what callers need? Remove anything that leaks implementation details. |
| **Consistency** | Do types align with existing domain models (e.g. `Result<T>`, `QuizQuestion`)? No duplicate concepts. |
| **Testability** | Can a `Fake` implementation be written from this contract alone, with no knowledge of AICore? |

Flag every issue with a `> ⚠️ RE Review:` blockquote. Do not write any implementation until all flags are resolved.

### 3. Fake Implementation Stub
Once the contract is approved, immediately create a `Fake<Name>` alongside the interface:

```kotlin
class FakeQuizGeneratorRepository(
    private val response: Result<List<QuizQuestion>> = Result.Success(FAKE_QUESTIONS),
) : QuizGeneratorRepository {
    override suspend fun generateQuiz(text: String): Result<List<QuizQuestion>> = response
}
```

This stub must compile and be usable in tests before any real implementation is written.

### 4. Implementation Gate
**No implementation class may be written until:**
1. The spec file exists in `.github/specs/domain/`.
2. All `> ⚠️ RE Review:` flags are resolved.
3. The `Fake` stub compiles and has at least one passing test.
