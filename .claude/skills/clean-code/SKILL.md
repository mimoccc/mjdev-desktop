---
name: clean-code
description: Enforce clean Kotlin: naming conventions, null safety (no !!), value classes, scope functions, sealed interfaces, expression bodies, immutability, and single-responsibility — applied during code review or when writing new Kotlin for Android/KMP
argument-hint: "<code area or description>"
user-invocable: true
allowed-tools: ["Read", "Write", "Edit", "Glob", "Grep", "Bash"]
---

# Clean Code — Kotlin / Android / KMP

## Project Convention: `Try<T>`

`Try<T>` is this project's typed error wrapper (Arrow's `Either<AppError, T>`).
- `Success(value)` — operation succeeded
- `Failure(error: AppError)` — operation failed, never throws
- Use `.fold {}`, `.map {}`, `.getOrElse {}` — never unwrap with `!!`

```kotlin
typealias Try<T> = Either<AppError, T>

// Usage
suspend fun findWord(id: WordId): Try<Word>  // returns Failure if not found
```

---

## Naming

| Construct | Convention | Example |
|---|---|---|
| Class, Interface, Object, Enum | `PascalCase` | `UserRepository` |
| Function, property, variable | `camelCase` | `fetchUserProfile()` |
| `const val`, top-level immutable | `SCREAMING_SNAKE_CASE` | `MAX_RETRY_COUNT` |
| Package | lowercase, no underscores | `com.example.feature.auth` |
| `@Composable` function | `PascalCase` (they are types) | `WordCard()` |
| Enum entries | `SCREAMING_SNAKE_CASE` | `ReviewQuality.HARD` |

### Meaningful Names
- Name reveals intent: `dueWords` not `list`, `isExpired` not `flag`
- No abbreviations: `userRepository` not `usrRepo`
- Booleans: `isLoading`, `hasError`, `canSubmit`
- Functions are verbs: `loadWords()`, `deleteUser()`, `calculateScore()`
- Avoid redundant context: inside `UserRepository`, use `findById()` not `findUserById()`

```kotlin
// Good
val dueWords = words.filter { it.isDue() }
fun calculateNextReviewDate(word: Word, quality: Int): LocalDate

// Bad
val list2 = wrds.filter { it.d }
fun calc(w: Word, q: Int): LocalDate
```

### Test Naming
Test names communicate intent — use backtick names in JUnit5:

```kotlin
@Test fun `given expired card, when charging, then returns Failure`()
@Test fun `calculateScore returns zero when no words reviewed`()
@Test fun `isDue returns false when reviewed within the last hour`()
```

---

## Functions

### One Responsibility
If you need "and" to describe a function, split it.

```kotlin
// Bad — validates AND charges AND logs AND notifies
fun processPayment(card: Card, amount: Int) { ... }

// Good — composed from named responsibilities
fun processPayment(card: Card, amount: Int): Try<Receipt> = tryOf {
    validate(card)
    val receipt = charge(card, amount)
    logPayment(receipt)
    notifyUser(receipt)
    receipt
}
```

### Expression Bodies
Prefer expression bodies for single-expression functions — removes ceremony.

```kotlin
// Bad
fun isDue(): Boolean {
    return nextReviewDate <= LocalDate.now()
}

// Good
fun isDue(): Boolean = nextReviewDate <= LocalDate.now()
fun dueCount(): Int = words.count { it.isDue() }
fun label(bucket: Int): String = when (bucket) {
    0       -> "New"
    in 1..2 -> "Learning"
    else    -> "Mastered"
}
```

### Parameter Count
- Prefer fewer than 3 parameters. More than 3: use a data class.
- Named arguments for multi-param calls.
- Default parameters over overloads.

```kotlin
// Bad
fun createWord(original: String, translated: String, notes: String, tags: List<String>, difficulty: Int)

// Good
data class CreateWordParams(
    val original: String,
    val translated: String,
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val difficulty: Int = 1,
)
fun createWord(params: CreateWordParams): Try<Word>
```

---

## Immutability

Prefer `val` over `var`. Prefer immutable collections. Mutate by replacing, not by modifying in place.

```kotlin
// Bad
var words = mutableListOf<Word>()
words.add(newWord)

// Good
val words: List<Word> = emptyList()
val updated = words + newWord   // replace, don't mutate

// Bad — mutable data class property
data class User(var name: String)

// Good — immutable + copy()
data class User(val name: String)
val renamed = user.copy(name = "Ali")
```

---

## Value Classes — Eliminate Primitive Obsession

Use `@JvmInline value class` to make IDs and domain primitives type-safe at compile time.

```kotlin
// Bad — silent bug: arguments in wrong order, compiler won't catch it
fun createUser(id: Int, tenantId: Int, age: Int)

// Good — compiler enforces correct types
@JvmInline value class UserId(val value: Int)
@JvmInline value class TenantId(val value: Int)
@JvmInline value class Age(val value: Int)

fun createUser(id: UserId, tenantId: TenantId, age: Age)
```

---

## Null Safety

Design APIs to return non-null types. Use sealed types or `Try<T>` for absence/failure.

```kotlin
// Bad — caller must always check null
suspend fun findWord(id: WordId): Word?

// Good — absence represented explicitly
suspend fun findWord(id: WordId): Try<Word>   // Failure = not found
// or
sealed interface FindResult {
    data class Found(val word: Word) : FindResult
    data object NotFound : FindResult
}
```

### Safe Navigation Rules
- `?.` and `?:` over `!!`
- `!!` only when non-null is guaranteed by invariant — add a comment explaining why
- `requireNotNull(value) { "reason" }` for intentional assertions at system boundaries

```kotlin
// Good
val name = user?.profile?.displayName ?: "Anonymous"

// Acceptable — with justification
val view = binding.root  // binding is always non-null after onCreateView

// Never
val name = user!!.profile!!.displayName
```

---

## Kotlin Idioms

### Scope Functions

| Function | Object ref | Returns | Use for |
|---|---|---|---|
| `let` | `it` | lambda result | Null-safe transform, scoped variable |
| `run` | `this` | lambda result | Configuration + compute result |
| `with` | `this` | lambda result | Group operations on a non-null receiver |
| `apply` | `this` | receiver | Builder-style initialisation |
| `also` | `it` | receiver | Side effects (logging, validation) |

```kotlin
// let — safe-call chain + transform
user?.let { sendWelcomeEmail(it.email) }

// apply — builder-style setup
val intent = Intent(context, MainActivity::class.java).apply {
    putExtra("userId", userId)
    flags = Intent.FLAG_ACTIVITY_NEW_TASK
}

// also — side effect without breaking chain
fetchWords()
    .also { logger.log("Fetched ${it.size} words") }
    .map { it.toDomain() }
```

> Avoid `runCatching {}` — use `Try<T>` / `Either` so errors are typed and explicit.

### Sealed Interfaces (prefer over sealed classes)

```kotlin
sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data class Failure(val reason: AuthError) : AuthResult
    data object Cancelled : AuthResult
}

// Exhaustive — no else needed
when (result) {
    is AuthResult.Success   -> onSuccess(result.user)
    is AuthResult.Failure   -> showError(result.reason)
    AuthResult.Cancelled    -> onCancelled()
}
```

### Extension Functions

Extend existing types with domain behaviour without inheritance.

```kotlin
// Domain extensions
fun Word.isDue(): Boolean = nextReviewDate <= LocalDate.now()
fun List<Word>.dueCount(): Int = count { it.isDue() }

// Formatting
fun LocalDate.toDisplayString(): String =
    format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
```

### `companion object` — Factory Methods and Constants

```kotlin
class AuthToken private constructor(val value: String) {
    companion object {
        fun from(raw: String): Try<AuthToken> =
            if (raw.isBlank()) Failure(AuthError.InvalidToken) else Success(AuthToken(raw))
        val Empty = AuthToken("")
    }
}
```

### `typealias` for Readability

```kotlin
typealias WordId = Int              // signals intent at call sites
typealias ReviewHistory = List<ReviewEntry>
typealias OnWordSelected = (Word) -> Unit
```

### When Expressions

Prefer `when` over long if-else chains.

```kotlin
val label = when (bucket) {
    0         -> "New"
    in 1..2   -> "Learning"
    in 3..4   -> "Familiar"
    else      -> "Mastered"
}

when {
    isLoading       -> showLoading()
    hasError        -> showError()
    items.isEmpty() -> showEmpty()
    else            -> showContent()
}
```

---

## Constants & Magic Values

No magic numbers or strings in business logic.

```kotlin
object ReviewQuality {
    const val AGAIN = 0
    const val HARD  = 2
    const val GOOD  = 4
    const val EASY  = 5
}

object Timeouts {
    val networkRequest = 30.seconds
    val cacheExpiry    = 24.hours
}
```

---

## Comments

- Comments explain **why**, not **what** — code explains itself.
- Delete commented-out code; version control remembers it.
- KDoc on public API in shared modules.

```kotlin
// Bad — the what is obvious
// Filter words by due date
val due = words.filter { it.isDue() }

// Good — explains the why (SRS algorithm detail)
// Words reviewed within the last hour are excluded to prevent over-drilling
val due = words.filter { it.isDue() && it.lastReviewed.isBefore(oneHourAgo) }
```

---

## Anti-Patterns

| Anti-Pattern | Prefer Instead |
|---|---|
| `!!` | Safe calls, Elvis, `requireNotNull` |
| `try-catch` for control flow | `Try<T>`, `.fold {}` |
| `var` in data classes | `val` + `copy()` |
| Long parameter lists | Data class params |
| Deeply nested lambdas | Coroutines / named functions |
| `Any` / unchecked casts | Generics, sealed types |
| Empty `catch {}` | Always handle or rethrow |
| Commented-out code | Delete it |
| `runCatching {}` | `Try<T>` / `Either` — typed errors |
| Primitive IDs (`Int`, `String`) | `@JvmInline value class` |
| `LiveData` in new code | `StateFlow` / `SharedFlow` |
| Mocks in tests | Fakes (`FakeXxxRepository`) |

---

## Code Review Checklist

Run through this when reviewing or writing Kotlin:

- [ ] No `!!` without a justification comment
- [ ] No `var` in data classes — use `val` + `copy()`
- [ ] No magic literals — use named constants or `value class`
- [ ] Functions under ~20 lines, single responsibility
- [ ] All failure paths return `Try<T>` or a sealed type — no `null` leaking out
- [ ] No `try-catch` for control flow
- [ ] Booleans named `is*`, `has*`, or `can*`
- [ ] IDs are `value class`, not raw `Int`/`String`
- [ ] No commented-out code
- [ ] Test names describe the scenario: `given_when_then` or `should_X_when_Y`
- [ ] KDoc present on all public API in shared modules