---
name: feature-spec
description: 'Write and review feature specifications before any implementation. Use when starting a new feature, user story, or behaviour change. Produces a BDD-style spec with Given/When/Then scenarios, acceptance criteria, and edge cases. Copilot acts as a requirements engineer: it reviews the spec for completeness, ambiguity, and consistency before any code is written.'
argument-hint: 'Feature name or short description'
---

# Feature Specification

## When to Use
- Starting any new feature, screen, or behaviour
- Clarifying ambiguous requirements before writing code
- Ensuring edge cases and error paths are captured upfront
- Reviewing an existing spec for gaps or contradictions

## Procedure

### 1. Draft the Spec
Create `.github/specs/<feature-name>.md` using the template below.

```markdown
# <Feature Name>

## Context
One paragraph explaining why this feature exists and what problem it solves.

## Acceptance Criteria
- [ ] AC1: …
- [ ] AC2: …

## Scenarios

### Scenario 1: <Happy path title>
**Given** …
**When** …
**Then** …

### Scenario 2: <Error / edge case title>
**Given** …
**When** …
**Then** …

## Out of Scope
- List anything explicitly NOT covered by this spec.

## Open Questions
- Any unresolved decisions that need a stakeholder answer.
```

### 2. Requirements Engineering Review
After drafting, **act as a requirements engineer** and review the spec against these checks before proceeding to implementation:

| Check | Question to ask |
|-------|----------------|
| **Completeness** | Are all happy paths, error paths, and edge cases covered? Is there a scenario for every acceptance criterion? |
| **Unambiguity** | Does every term have a single, clear meaning? Could two developers interpret any statement differently? |
| **Consistency** | Do any scenarios contradict each other or the existing domain model? |
| **Testability** | Can every acceptance criterion be verified by an automated test? If not, rewrite it so it can. |
| **Scope creep** | Does anything in the spec belong in a different feature? Move it to Out of Scope. |
| **No implementation details** | The spec must describe *what*, not *how*. Remove any Kotlin, class names, or architecture decisions. |

Flag every issue found with a `> ⚠️ RE Review:` blockquote inline in the spec. Do not proceed to implementation until all flags are resolved.

### 3. Map Scenarios to Tests
Once the spec is approved, list the test cases that must exist before the feature is considered done:

```markdown
## Required Test Coverage
- [ ] `QuizViewModelTest`: …
- [ ] `TextChunkerTest`: …
- [ ] `QuizScreenTest` (Compose): …
```

### 4. Implementation Gate
**No code may be written for a feature until:**
1. The spec file exists in `.github/specs/`.
2. All `> ⚠️ RE Review:` flags have been resolved.
3. The "Required Test Coverage" section is populated.
