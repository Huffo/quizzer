---
name: compose-ui
description: 'Build Jetpack Compose screens and components for this project. Use when creating or editing any Composable, screen layout, navigation, or theming. Enforces Material 3, LazyColumn for lists, mandatory contentDescription/semantics, and @StringRes string usage.'
---

# Compose UI

## When to Use
- Creating a new screen or Composable component
- Editing existing UI layout or theming
- Adding navigation between screens
- Ensuring accessibility compliance on interactive elements

## Conventions
- All strings shown to users must come from `@StringRes` / `stringResource(R.string.…)` — no hardcoded English literals.
- Every interactive element (`Button`, `IconButton`, custom clickable) **must** have a `contentDescription` or a `semantics { … }` block.
- Use `LazyColumn` (not `Column` + `forEach`) whenever rendering a list that could grow.
- Follow Material 3 (`androidx.compose.material3`) — do not mix with Material 2.
- No magic numbers in `Dp`, `sp`, or padding values; extract to named constants or the theme's spacing tokens.

## Procedure

### 1. New Screen
1. Create `ui/<FeatureName>Screen.kt`.
2. Accept only a `ViewModel` (or state + callbacks) as parameters — no business logic inside Composables.
3. Use `Scaffold` as the top-level container with `TopAppBar` where navigation is needed.
4. Preview with `@Preview(showBackground = true)`.

### 2. Lists
Always use `LazyColumn`:
```kotlin
// key must use a stable unique ID — never question text (duplicates break diffing)
LazyColumn(modifier = Modifier.fillMaxSize()) {
    items(questions, key = { it.id }) { question ->
        QuizQuestionCard(question = question)
    }
}
```

> `QuizQuestion` must carry a stable `id: String` field (e.g. assigned by the parser). See the `domain-model-spec` for the full contract.

### 3. Accessibility
Every interactive element:
```kotlin
Button(
    onClick = onSubmit,
    modifier = Modifier.semantics { contentDescription = submitLabel },
) { … }
```
Or via `Modifier.semantics`:
```kotlin
Box(
    modifier = Modifier
        .clickable(onClick = onSelect)
        .semantics { contentDescription = optionLabel }
)
```
Test findability in Compose tests with `onNodeWithContentDescription(…)`.

### 4. Theming
- Wrap the root in `QuizzerTheme { … }` (defined in `ui/theme/`).
- Use `MaterialTheme.colorScheme`, `MaterialTheme.typography`, and `MaterialTheme.shapes` — never hardcode colours or text sizes.

### 5. State Hoisting
- Hoist state to the nearest common ancestor or ViewModel.
- Composables should be stateless where possible; pass lambdas for events.

## Key Dependencies
```kotlin
// build.gradle.kts (app)
implementation("androidx.compose.material3:material3:<version>")
implementation("androidx.compose.ui:ui:<version>")
implementation("androidx.compose.ui:ui-tooling-preview:<version>")
debugImplementation("androidx.compose.ui:ui-tooling:<version>")
```
