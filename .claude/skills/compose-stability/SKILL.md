---
name: compose-stability
description: Diagnose and fix Compose stability — @Stable, @Immutable, ImmutableList, strong skipping mode, compiler metrics, and lambda stability for zero-waste recomposition
argument-hint: "<composable, data class, or state class to analyse>"
user-invocable: true
allowed-tools: ["Read", "Write", "Edit", "Glob", "Grep", "Bash"]
---

# Compose Stability

Compose skips recomposition of a composable when all its inputs are **stable** and **equal** to their previous values. An unstable parameter forces full recomposition of that subtree every time the parent recomposes.

## Stability Contract

A type is **stable** if:
1. `equals()` is always consistent for the same data
2. Public properties are read-only (`val`) or observable via Compose snapshot state
3. All public property types are also stable

**Stable by default:**
- Primitives: `Boolean`, `Int`, `Long`, `Float`, `Double`, `Char`
- `String`
- Lambda types: `() -> Unit`, `(T) -> R`
- Compose `State<T>` and `MutableState<T>`
- Types annotated `@Stable` or `@Immutable`

**Unstable by default:**
- `List<T>`, `Map<K,V>`, `Set<T>` — mutable implementations possible at runtime
- Any class with `var` properties
- **Interfaces** — always unstable regardless of implementation
- Classes from external modules the Compose compiler cannot inspect
- Classes from non-Kotlin modules (Java classes)

---

## @Immutable vs @Stable

### When are annotations actually needed?

The Compose compiler **infers stability automatically** for classes it can inspect. You only need explicit annotations when:
- The class is in a **module without the Compose compiler plugin** (the compiler can't see inside it)
- The class holds `var` properties backed by snapshot state
- You want to assert stability for an **interface** type

If a `data class` with only stable `val` fields is in the same Compose module, the compiler will mark it stable without any annotation.

### @Immutable — Deep Immutability Promise

Use when **all** properties are `val` and all property types are themselves immutable:

```kotlin
@Immutable
data class ThemeColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
)

@Immutable
data class UserProfile(
    val id: String,
    val name: String,
    val avatarUrl: String,
)
```

### @Stable — Stability Contract Promise

Use when the type is in a non-Compose module, or holds mutable snapshot state:

```kotlin
// Needed: class lives in a :domain module without Compose compiler
@Stable
data class WordListState(
    val words: ImmutableList<Word>,
    val isLoading: Boolean,
    val error: String?,
)

// Needed: holds MutableState
@Stable
class ThemeManager(
    val current: MutableState<Theme> = mutableStateOf(Theme.Light)
)
```

**Warning:** `@Stable` and `@Immutable` are promises to the Compose compiler. Breaking them causes silent bugs — recompositions skip when they should run.

---

## Fix: Unstable Collections

The most common stability issue — `List<T>` is considered unstable.

```toml
# gradle/libs.versions.toml
kotlinx-collections-immutable = "0.3.8"
```

```kotlin
// build.gradle.kts
implementation(libs.kotlinx.collections.immutable)
```

```kotlin
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList

// Before — unstable, forces recomposition on every parent recompose
data class WordState(
    val words: List<Word>,          // UNSTABLE
    val tags: Map<String, Int>,     // UNSTABLE
)

// After — compiler infers stable (no annotation needed if in a Compose module)
data class WordState(
    val words: ImmutableList<Word>       = persistentListOf(),   // STABLE
    val tags: ImmutableMap<String, Int>  = persistentMapOf(),    // STABLE
)

// ViewModel — convert before updating state
updateState { copy(words = newWords.toImmutableList()) }
```

---

## Fix: Unstable Lambdas

Lambdas that capture changing variables are unstable. Use function references or `remember`:

```kotlin
// Problem — new lambda created on every recomposition when word changes
WordCard(
    word    = word,
    onClick = { viewModel.select(word) },   // captures word — new lambda each recompose
)

// Solution 1 — function reference (stable when viewModel is stable)
WordCard(
    word    = word,
    onClick = viewModel::select,            // works if select(word: Word)
)

// Solution 2 — remember with key
val onClick = remember(word.id) { { viewModel.select(word) } }
WordCard(word = word, onClick = onClick)

// Solution 3 — pass ID instead of full object
WordCard(
    wordId  = word.id,
    onClick = viewModel::selectById,        // stable function reference
)
```

---

## Fix: Interfaces Are Always Unstable

Interfaces have no concrete type the compiler can inspect, so any composable that takes an interface parameter cannot be skipped.

```kotlin
// Problem — interface param forces recomposition even if impl is @Stable
interface WordClickHandler { fun onClick(word: Word) }

@Composable
fun WordCard(handler: WordClickHandler) { … }   // UNSTABLE: interface

// Fix 1 — replace interface param with lambda
@Composable
fun WordCard(onClick: (Word) -> Unit) { … }     // lambdas are stable

// Fix 2 — use a @Stable concrete class
@Stable
class WordClickHandlerImpl(private val vm: WordViewModel) : WordClickHandler {
    override fun onClick(word: Word) = vm.select(word)
}

// Fix 3 — enable strong skipping (reference equality check handles it)
```

---

## Fix: Multi-Module Stability (KMP)

Types defined in modules **without the Compose compiler plugin** (e.g., a `:domain` module) are opaque — the compiler cannot verify their properties.

```
:domain          ← pure Kotlin module, no Compose plugin → types appear unstable
:feature:words   ← Compose module, uses domain types
```

Three options:

```kotlin
// Option A — annotate at definition site if you own the module
// In :domain module, add @Immutable/@Stable to expose the contract
@Immutable
data class Word(val id: String, val text: String)

// Option B — map to a UI model in the presentation layer (preferred for clean architecture)
@Immutable
data class WordUiModel(val id: String, val text: String)  // stable, Compose-owned type

// Option C — stability-config.conf for types you don't own (see section below)
```

---

## Strong Skipping Mode

Enables skipping for composables with unstable parameters when all parameter instances are **reference-equal**. Also automatically remembers lambdas.

```kotlin
// Kotlin < 2.0 — opt in explicitly
composeCompiler {
    enableStrongSkippingMode = true
}

// Kotlin 2.0+ — strong skipping is ON by default; no flag needed
// (Compose Compiler 1.5.8+ bundled with Kotlin 2.0)
```

With strong skipping:
- All composable functions become restartable
- Unstable parameters compared by reference equality — if the same instance, skip
- Lambdas are automatically remembered — no new lambda allocation per recompose

---

## `@NonRestartableComposable` for Leaf Nodes

Pure leaf composables that read no state and have no child composables can skip the restart machinery entirely:

```kotlin
@NonRestartableComposable
@Composable
fun WordChip(text: String, modifier: Modifier = Modifier) {
    // No state reads — parent recompose will re-invoke directly; no independent restart needed
    Text(text = text, modifier = modifier)
}
```

Use only for true leaves — nodes that don't call other restartable composables.

---

## Compose Compiler Metrics & Reports

Enable to find stability issues without guessing:

```kotlin
// build.gradle.kts
composeCompiler {
    reportsDestination  = layout.buildDirectory.dir("compose_compiler")
    metricsDestination  = layout.buildDirectory.dir("compose_compiler")
}
```

```bash
./gradlew :composeApp:assembleDebug
# Reports in: build/compose_compiler/
```

**What to look for in `*-composables.txt`:**

```
restartable skippable fun WordCard(        ← ideal: skippable
  stable word: Word
  stable onClick: Function0<Unit>
)
restartable fun WordListScreen(            ← cannot skip — investigate params
  unstable state: WordListState            ← this is the culprit
)
```

**What to look for in `*-classes.txt`:**

```
stable   WordListState                   ← good
unstable WordListState                   ← fix the fields, not just the annotation
  unstable words: List<Word>             ← replace with ImmutableList
```

---

## Stability for External / Java Classes

For classes you don't own (e.g., from third-party libraries):

```kotlin
// stability-config.conf (in module root)
// Tell Compose compiler these types are stable
com.google.firebase.auth.FirebaseUser
java.time.LocalDate
java.util.UUID
```

```kotlin
// build.gradle.kts
composeCompiler {
    stabilityConfigurationFiles.add(
        rootProject.layout.projectDirectory.file("stability-config.conf")
    )
}
```

---

## Stability Diagnostic Checklist

1. Enable compiler reports in `build.gradle.kts`
2. Run `./gradlew :composeApp:assembleDebug`
3. Search `*-composables.txt` for `restartable` without `skippable` — investigate each
4. Search `*-classes.txt` for `unstable` — fix the **fields**, not just the annotation
5. Replace `List<T>` → `ImmutableList<T>` in all state/UI data classes
6. Replace `Map<K,V>` → `ImmutableMap<K,V>` in all state/UI data classes
7. Map domain types to `@Immutable` UI models in the presentation layer
8. Replace interface params with lambdas or `@Stable` concrete classes
9. Annotate classes in non-Compose modules with `@Stable`/`@Immutable` (or use stability-config.conf for third-party)
10. Enable strong skipping mode (Kotlin < 2.0 only)
11. Mark true leaf composables with `@NonRestartableComposable`
12. Verify fixes in Layout Inspector → Recomposition counts (Live Updates mode)