---
name: solid-android
description: Apply SOLID, YAGNI, DRY, and KISS principles in Kotlin/Android — SRP, OCP, LSP, ISP, DIP, avoiding premature abstraction, identifying genuine duplication, and keeping code focused and minimal
argument-hint: "<class, design problem, or code to evaluate>"
user-invocable: true
allowed-tools: ["Read", "Write", "Edit", "Glob", "Grep"]
---

# SOLID Principles in Kotlin / Android

> **Project conventions used throughout this file:**
> - `Try<T>` — project-specific sealed `Result` wrapper from `:core` (`Try.Success` / `Try.Failure`)
> - `tryOf { }` — builder that catches exceptions and wraps them as `Try.Failure`
> - `updateState { }`, `emitEffect()`, `reduce()` — ViewModel convention extensions from `:core:presentation`

---

## S — Single Responsibility Principle

> A class should have only one reason to change.

### Violation

```kotlin
// BAD — handles UI state, analytics, navigation, AND storage
// Also: accessing `context` in a ViewModel is itself a DIP violation
class WordViewModel : ViewModel() {
    fun onWordReviewed(word: Word, correct: Boolean) {
        // Update UI state
        _state.update { it.copy(lastReviewed = word) }
        // Send analytics — belongs in a service
        FirebaseAnalytics.getInstance(context).logEvent("word_reviewed", bundleOf(
            "word_id" to word.id,
            "correct" to correct,
        ))
        // Navigate — belongs in an effect/event
        navController.navigate("result")
        // Save to prefs — belongs in a repository
        prefs.edit().putLong("last_review", System.currentTimeMillis()).apply()
    }
}
```

### Fix — Split Responsibilities

```kotlin
// ViewModel — manages UI state and delegates everything else
class WordViewModel(
    private val reviewWord: ReviewWordUseCase,
    private val analytics: IAnalyticsService,
) : ViewModel() {
    fun onWordReviewed(word: Word, correct: Boolean) {
        viewModelScope.launch {
            reviewWord(ReviewWordUseCase.Params(word, if (correct) 5 else 1))
                .reduce(
                    onSuccess = {
                        updateState { copy(lastReviewed = word) }
                        emitEffect(WordEffect.NavigateToResult)
                        analytics.track(AnalyticsEvent.WordReviewed(word.id, correct))
                    },
                    onFailure = { e -> updateState { copy(error = e.message) } },
                )
        }
    }
}

// Analytics service — manages tracking only
class FirebaseAnalyticsService(private val firebase: FirebaseAnalytics) : IAnalyticsService {
    override fun track(event: AnalyticsEvent) {
        when (event) {
            is AnalyticsEvent.WordReviewed -> firebase.logEvent("word_reviewed",
                bundleOf("word_id" to event.wordId, "correct" to event.correct))
        }
    }
}
```

### SRP Layer Split: DataSource vs Repository

A common violation is collapsing remote + local concerns into one class:

```kotlin
// BAD — one class talks to both Room and Ktor
class WordRepository(private val db: AppDatabase, private val api: WordApi) {
    suspend fun sync() {
        val remote = api.fetchWords()          // network concern
        db.wordDao().insertAll(remote.toEntity()) // storage concern
    }
}

// GOOD — each source has one job; Repository orchestrates
class WordRemoteDataSource(private val api: WordApi) : IWordRemoteDataSource {
    override suspend fun fetchWords(): Try<List<WordDto>> = tryOf { api.fetchWords() }
}

class WordLocalDataSource(private val dao: WordDao) : IWordLocalDataSource {
    override suspend fun insertAll(words: List<WordEntity>): Try<Unit> = tryOf { dao.insertAll(words) }
    override fun observeAll(): Flow<List<WordEntity>> = dao.observeAll()
}

class WordRepositoryImpl(
    private val remote: IWordRemoteDataSource,
    private val local: IWordLocalDataSource,
) : IWordRepository {
    override suspend fun syncWithRemote(): Try<Unit> = tryOf {
        val words = remote.fetchWords().getOrThrow()
        local.insertAll(words.map { it.toEntity() }).getOrThrow()
    }
}
```

---

## O — Open/Closed Principle

> Open for extension, closed for modification.

### Violation

```kotlin
// BAD — every new notification type requires modifying this class
class NotificationSender {
    fun send(type: String, wordId: Int) {
        when (type) {
            "push" -> sendPushNotification(wordId)
            "email" -> sendEmail(wordId)
            // Adding "sms" requires modifying this class
        }
    }
}
```

### Fix — Polymorphism / Strategy

```kotlin
// Interface — contract (closed for modification)
interface INotificationChannel {
    fun send(notification: ReviewReminder)
}

// Implementations — extend without touching base contract
class PushNotificationChannel(private val fcm: FirebaseMessaging) : INotificationChannel {
    override fun send(notification: ReviewReminder) { /* push logic */ }
}

class EmailNotificationChannel(private val client: EmailClient) : INotificationChannel {
    override fun send(notification: ReviewReminder) { /* email logic */ }
}

// New channel — added without modifying any existing code
class SmsNotificationChannel(private val sms: SmsClient) : INotificationChannel {
    override fun send(notification: ReviewReminder) { /* sms logic */ }
}

// Orchestrator — open for extension via DI
class NotificationService(
    private val channels: List<INotificationChannel>,
) {
    fun sendAll(notification: ReviewReminder) = channels.forEach { it.send(notification) }
}
```

### Sealed Interfaces for Exhaustive Extension

Use `sealed` when the set of variants is **closed** (owned by you) and exhaustiveness is enforced at call sites:

```kotlin
sealed interface SyncResult {
    data class Success(val count: Int) : SyncResult
    data class Partial(val synced: Int, val failed: Int) : SyncResult
    data object NoNetwork : SyncResult
    data object UpToDate : SyncResult
}

// Adding a new variant forces an update at ALL when() call sites — compile-time safety
fun handle(result: SyncResult) = when (result) {
    is SyncResult.Success  -> showSuccess(result.count)
    is SyncResult.Partial  -> showPartial(result.synced, result.failed)
    SyncResult.NoNetwork   -> showOffline()
    SyncResult.UpToDate    -> { /* nothing */ }
}
```

> Use `sealed` (closed, exhaustive) for domain results. Use `interface` (open, extensible) for notification channels, formatters, strategies.

---

## L — Liskov Substitution Principle

> Subtypes must be substitutable for their base types without altering program correctness.

### Violation — Unexpected Throw

```kotlin
// BAD — CachingWordRepository breaks contract by throwing when offline
class CachingWordRepository(
    private val remote: IWordRepository,
    private val cache: IWordLocalDataSource,
) : IWordRepository {
    override suspend fun syncWithRemote(): Try<Unit> {
        if (!networkMonitor.isConnected) throw IllegalStateException("No network")
        // ^ BREAKS LSP — callers of IWordRepository.syncWithRemote() don't expect exceptions
        return remote.syncWithRemote()
    }
}
```

### Fix — Honor the Contract

```kotlin
class CachingWordRepository(
    private val remote: IWordRepository,
    private val cache: IWordLocalDataSource,
    private val networkMonitor: INetworkMonitor,
) : IWordRepository {
    override suspend fun syncWithRemote(): Try<Unit> = tryOf {
        if (!networkMonitor.isConnected) return Try.Failure(NoNetworkException())
        remote.syncWithRemote().getOrThrow()
    }
    // Contract honored: always returns Try<Unit>, never throws
}
```

### Violation — Silent No-Op Subtype

```kotlin
// BAD — ReadOnlyWordList claims to implement IWordWriter but silently discards writes.
// Callers believe saves succeed; data is silently lost.
class ReadOnlyWordList : IWordWriter {
    override suspend fun save(word: Word): Try<Word> = Try.Success(word) // no-op — word not actually saved
    override suspend fun delete(id: Int): Try<Unit> = Try.Success(Unit)  // no-op — word not actually deleted
}
```

### Fix — Use a Narrower Interface

```kotlin
// If a type can only read, don't make it implement IWordWriter.
// Redesign so the contract matches the capability.
class ReadOnlyWordList(private val reader: IWordReader) // only depends on what it can honor
```

### LSP in Compose

```kotlin
// BAD — ignores the onClick lambda entirely; callers wiring up actions get silent failures
@Composable
fun SubmitButton(text: String, onClick: () -> Unit) {
    Button(onClick = {}) {   // onClick contract broken — caller's lambda is discarded
        Text(text)
    }
}

// GOOD — honor the full contract; use enabled to model disabled state, not a no-op lambda
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier) {
        Text(text)
    }
}
```

---

## I — Interface Segregation Principle

> Clients should not be forced to depend on interfaces they don't use.

### Violation — Fat Repository Interface

```kotlin
// BAD — one fat interface; most clients only need a small subset
interface IWordRepository {
    fun observeWords(): Flow<List<Word>>
    suspend fun findById(id: Int): Try<Word>
    suspend fun save(word: Word): Try<Word>
    suspend fun delete(id: Int): Try<Unit>
    suspend fun syncWithRemote(): Try<Unit>
    suspend fun importFromCsv(uri: Uri): Try<Int>
    suspend fun exportToCsv(): Try<Uri>
    suspend fun getStats(): Try<WordStats>
    suspend fun clearAll(): Try<Unit>
}
```

### Fix — Role Interfaces

```kotlin
interface IWordReader {
    fun observeWords(): Flow<List<Word>>
    suspend fun findById(id: Int): Try<Word>
}

interface IWordWriter {
    suspend fun save(word: Word): Try<Word>
    suspend fun delete(id: Int): Try<Unit>
}

interface IWordSync {
    suspend fun syncWithRemote(): Try<Unit>
}

interface IWordImportExport {
    suspend fun importFromCsv(uri: Uri): Try<Int>
    suspend fun exportToCsv(): Try<Uri>
}

// Implementation combines all roles
class WordRepositoryImpl(...) : IWordReader, IWordWriter, IWordSync, IWordImportExport

// Use cases depend only on what they need
class GetDueWordsUseCase(private val reader: IWordReader)
class SyncUseCase(private val sync: IWordSync)
class ImportWordsUseCase(private val importer: IWordImportExport)
```

### ISP for DataSources

Apply the same split at the data layer:

```kotlin
interface IWordLocalDataSource {
    fun observeAll(): Flow<List<WordEntity>>
    suspend fun findById(id: Int): Try<WordEntity>
    suspend fun insertAll(words: List<WordEntity>): Try<Unit>
    suspend fun deleteById(id: Int): Try<Unit>
}

interface IWordRemoteDataSource {
    suspend fun fetchWords(): Try<List<WordDto>>
    suspend fun postResult(wordId: Int, score: Int): Try<Unit>
}

// Sync use case only needs remote; offline-first reader only needs local
class SyncUseCase(
    private val remote: IWordRemoteDataSource,
    private val local: IWordLocalDataSource,
)
```

### ISP for ViewModel State

Large feature screens often have a single monolithic state interface. Break it by UI concern:

```kotlin
// BAD — SettingsViewModel forced to implement analytics and appearance together
interface ISettingsViewModel {
    val notificationsEnabled: StateFlow<Boolean>
    val selectedTheme: StateFlow<Theme>
    val analyticsOptIn: StateFlow<Boolean>
    fun onNotificationToggled(enabled: Boolean)
    fun onThemeSelected(theme: Theme)
    fun onAnalyticsToggled(enabled: Boolean)
}

// GOOD — composables consume only the slice they render
interface INotificationSettings {
    val notificationsEnabled: StateFlow<Boolean>
    fun onNotificationToggled(enabled: Boolean)
}

interface IThemeSettings {
    val selectedTheme: StateFlow<Theme>
    fun onThemeSelected(theme: Theme)
}

// ViewModel satisfies all; composables depend on the slice
class SettingsViewModel : INotificationSettings, IThemeSettings, ...

@Composable
fun NotificationSection(settings: INotificationSettings) { ... }

@Composable
fun ThemeSection(settings: IThemeSettings) { ... }
```

---

## D — Dependency Inversion Principle

> High-level modules should not depend on low-level modules. Both should depend on abstractions.

### Violation

```kotlin
// BAD — ViewModel directly depends on concrete Room DAO
class WordListViewModel(
    private val dao: WordDao,   // concrete — Room-specific, untestable
) : ViewModel() {
    fun load() {
        viewModelScope.launch {
            val words = dao.getAllWords().map { it.toDomain() }
            updateState { copy(words = words) }
        }
    }
}
```

### Fix — Depend on Abstraction

```kotlin
// Domain interface — abstraction (in :domain, no framework imports)
interface IWordReader {
    fun observeWords(): Flow<List<Word>>
}

// ViewModel — depends on use case, not Room
class WordListViewModel(
    private val getWords: GetDueWordsUseCase,
) : ViewModel()

// Use case — depends on repository interface
class GetDueWordsUseCase(
    private val repository: IWordReader,    // depends on interface, not impl
) : FlowUseCase<Unit, List<Word>>

// Repository impl — Room is an implementation detail, hidden here
class WordRepositoryImpl(
    private val dao: WordDao,
) : IWordReader, IWordWriter, IWordSync, IWordImportExport
```

### DIP in DI Modules (Koin)

```kotlin
val dataModule = module {
    // Bind interface → implementation; call sites never import the impl class
    single<IWordReader> { WordRepositoryImpl(get()) }
    single<IWordWriter> { get<WordRepositoryImpl>() }   // same instance, different role
    single<IWordLocalDataSource> { WordLocalDataSourceImpl(get()) }
    single<IWordRemoteDataSource> { WordRemoteDataSourceImpl(get()) }
    single<INetworkMonitor> { AndroidNetworkMonitor(androidContext()) }
}
```

### DIP in DI Modules (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds @Singleton
    abstract fun bindWordReader(impl: WordRepositoryImpl): IWordReader

    @Binds @Singleton
    abstract fun bindWordWriter(impl: WordRepositoryImpl): IWordWriter

    @Binds @Singleton
    abstract fun bindNetworkMonitor(impl: AndroidNetworkMonitor): INetworkMonitor
}
```

---

## SOLID in Testing

SOLID principles directly enable testability. The two most impactful:

**DIP → fakes are possible.** If a ViewModel depends on `IWordReader`, you can inject `FakeWordReader` in tests. Without DIP there is nothing to swap.

**ISP → fakes are small.** A fake for a role interface implements 2 methods, not 9:

```kotlin
// Small fake — only what GetDueWordsUseCase needs
class FakeWordReader : IWordReader {
    var words: List<Word> = emptyList()
    var error: Throwable? = null

    override fun observeWords(): Flow<List<Word>> =
        if (error != null) flow { throw error!! } else flowOf(words)

    override suspend fun findById(id: Int): Try<Word> =
        words.find { it.id == id }?.let { Try.Success(it) } ?: Try.Failure(NotFoundException())
}

// If you had used the fat IWordRepository, FakeWordRepository would need 9 stub methods
// — most of which throw UnsupportedOperationException and add noise to every test file.
```

**SRP → tests have one reason to fail.** A class with a single responsibility has a predictable test surface; failures point directly at the broken concern.

---

## SOLID Checklist

| Principle | Ask yourself |
|---|---|
| **SRP** | Can you name this class with a single-noun role? Would a change to analytics/storage/navigation force a change here? |
| **OCP** | Can you add new behavior (new channel, new result type) without touching existing classes? |
| **LSP** | Does every implementation honor the full contract — same preconditions, same postconditions, no extra throws? |
| **ISP** | Does each client import only the methods it uses? Could you split this interface by role? |
| **DIP** | Do ViewModel and UseCase import only interfaces? Is `import androidx.room.*` or `import io.ktor.*` absent from `:domain`? |

---

## Anti-Patterns to Avoid

- **God ViewModel** — one ViewModel with 20+ methods driving an entire feature (SRP violation)
- **God Activity / Fragment** — business logic, navigation, and analytics crammed into `onCreate` (SRP violation)
- **`Context` or `android.*` in a UseCase** — the domain layer must be pure Kotlin (DIP violation)
- **Concrete dependencies in domain** — `import androidx.room.*` or `import io.ktor.*` in a use case (DIP violation)
- **Repository calling another repository** — orchestration belongs in a UseCase, not the data layer (SRP violation)
- **Checking `instanceof` / `is Type`** — use polymorphism or sealed types instead (OCP violation)
- **Throwing from overrides when the base contract doesn't throw** — breaks substitutability (LSP violation)
- **Silent no-op overrides** — implementing an interface method as a no-op to satisfy the compiler (LSP violation)
- **`IEverything` interface** — one interface covering an entire repository or screen (ISP violation)

---

## YAGNI — You Aren't Gonna Need It

Don't build for hypothetical future requirements. Build for what is needed now.

### Violations

```kotlin
// BAD — "flexible" config nobody asked for
class WordRepositoryImpl(
    private val local: IWordLocalDataSource,
    private val remote: IWordRemoteDataSource,
    private val strategy: SyncStrategy = SyncStrategy.DEFAULT,   // YAGNI — only DEFAULT exists
    private val retryPolicy: RetryPolicy = RetryPolicy.NONE,     // YAGNI — never configured
    private val cacheExpiry: Duration = 1.hours,                 // YAGNI — never read
    private val logger: ILogger? = null,                         // YAGNI — logging already in place
) : IWordRepository
```

```kotlin
// BAD — base class for one implementation
abstract class BaseNetworkDataSource {
    abstract val baseUrl: String
    abstract fun authenticate(request: HttpRequest): HttpRequest
    open fun handleError(e: Throwable): Nothing = throw e
    open fun transformResponse(response: HttpResponse): HttpResponse = response
}

class WordRemoteDataSourceImpl : BaseNetworkDataSource() {
    // Only one concrete class ever exists — abstraction was never needed
}
```

```kotlin
// BAD — interface exists purely for DI, tests use the concrete class anyway
interface IAnalyticsTracker {
    fun track(event: String)
}
class FirebaseAnalyticsTracker : IAnalyticsTracker { ... }
// Only one impl, tests just pass a no-op lambda — interface adds nothing

// GOOD — use the concrete class; extract interface only when a fake is needed in tests
class FirebaseAnalyticsTracker {
    fun track(event: String) { ... }
}
```

```kotlin
// BAD — passthrough UseCase with zero business logic
class GetWordsUseCase(private val repo: IWordRepository) {
    operator fun invoke(): Flow<List<Word>> = repo.getWords()
}
// GOOD — call repo.getWords() directly from ViewModel
// Add a UseCase only when it encodes real business logic (filtering, mapping, combining sources)
```

### YAGNI in KMP — expect/actual

Only use `expect/actual` when platform behaviour genuinely differs. If both actuals do the same thing, use a common API instead.

```kotlin
// BAD — expect/actual for identical behaviour
expect fun currentTimeMillis(): Long
actual fun currentTimeMillis(): Long = System.currentTimeMillis()              // Android
actual fun currentTimeMillis(): Long = kotlin.system.getTimeNanos() / 1_000_000  // iOS

// GOOD — one line, no platform split needed
val now = Clock.System.now().toEpochMilliseconds()
```

### YAGNI in Tests

```kotlin
// BAD — abstract BaseViewModelTest "all VMs will eventually need"
abstract class BaseViewModelTest {
    val testDispatcher = UnconfinedTestDispatcher()
    @BeforeEach fun setup() { Dispatchers.setMain(testDispatcher) }
    @AfterEach fun teardown() { Dispatchers.resetMain() }
}
// Only one ViewModel test class exists — just inline the setup there.

// GOOD — add the base class when 3+ test classes independently duplicate the same setup
```

### Fix

```kotlin
// GOOD — build exactly what's needed, nothing more
class WordRepositoryImpl(
    private val local: IWordLocalDataSource,
    private val remote: IWordRemoteDataSource,
) : IWordRepository
```

### YAGNI Checklist

- Is there a concrete, current requirement for this parameter/method/class?
- Are there at least two real callers, or is it "in case we need it"?
- Would removing it break anything real today?

If "no" to all three — delete it.

---

## DRY — Don't Repeat Yourself

> "Every piece of knowledge must have a single, unambiguous, authoritative representation within a system."

DRY is about **knowledge**, not code. Three similar-looking lines that represent different concepts are NOT duplication. Extract only when the same **decision** appears multiple times.

### Real Duplication — Extract

```kotlin
// BAD — same date formatting decision in three places
// WordListScreen.kt
val dateText = word.nextReviewDate.format(LocalDate.Format {
    dayOfMonth(); chars(" "); monthName(MonthNames.ENGLISH_ABBREVIATED); chars(" "); year()
})
// WordDetailScreen.kt — same
// NotificationBuilder.kt — same

// GOOD — one place encodes the formatting decision
fun LocalDate.toDisplayString(): String = format(LocalDate.Format {
    dayOfMonth(); chars(" "); monthName(MonthNames.ENGLISH_ABBREVIATED); chars(" "); year()
})
```

```kotlin
// BAD — same SRS calculation in ViewModel, UseCase, and Service
// GOOD — single SpacedRepetitionService owns the algorithm
class SpacedRepetitionService {
    fun calculateInterval(bucket: Int, quality: Int): Int = when {
        quality < 2 -> 1
        bucket == 0 -> 1
        bucket == 1 -> 3
        else        -> (bucket * 2.5).roundToInt()
    }
}
```

### Coincidental Duplication — Do NOT Extract

```kotlin
// These look the same but represent different business decisions.
// Extracting them creates false coupling.
fun UserDto.toDomain(): User   = User(id = id, name = name, email = email)
fun AuthorDto.toDomain(): Author = Author(id = id, name = name, bio = bio)

// If User gains a "role" field, Author must NOT — they are different concepts.
// A shared base class would be wrong.
```

### DRY in Compose — Extract vs Inline

```kotlin
// Extract when: same UI + same behavior used in 2+ unrelated screens
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

// Do NOT extract when: similar-looking but different semantics
// WordListScreen loading ≠ AuthScreen loading
// They may diverge (size, color, copy) — keep them inline
```

### DRY in Compose — Theme Tokens

```kotlin
// BAD — same color decision hardcoded in multiple composables
Text(color = Color(0xFF6200EE))
Icon(tint = Color(0xFF6200EE))

// GOOD — MaterialTheme is the single source of truth
Text(color = MaterialTheme.colorScheme.primary)
Icon(tint = MaterialTheme.colorScheme.primary)
```

---

## KISS — Keep It Simple

> Complexity is the enemy of reliability. The best code is the code that doesn't exist.

### Avoid Premature Abstraction

```kotlin
// BAD — abstract factory for one product
interface IWordParserFactory {
    fun create(format: String): IWordParser
}
class WordParserFactoryImpl : IWordParserFactory {
    override fun create(format: String): IWordParser = when (format) {
        "csv" -> CsvWordParser()
        else  -> throw IllegalArgumentException("Unknown format: $format")
    }
}
// CsvWordParser is the only parser that ever exists.

// GOOD
fun parseCsvWords(csv: String): List<WordDto> = csv.lines()
    .drop(1)  // header
    .filter { it.isNotBlank() }
    .map { line ->
        val parts = line.split(",")
        WordDto(original = parts[0].trim(), translated = parts[1].trim())
    }
```

### Flat Data Class vs Sealed Hierarchy

```kotlin
// Use a flat data class when states can overlap
// (e.g., showing stale data while loading, or error + retry button)
data class WordListState(
    val words: List<Word> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && words.isEmpty() && error == null
}

// Use sealed when states are truly mutually exclusive
// (e.g., multi-step wizard, auth flow, onboarding steps)
sealed interface OnboardingState {
    data object Welcome : OnboardingState
    data class SetGoal(val availableGoals: List<Goal>) : OnboardingState
    data object Complete : OnboardingState
}
// Sealed is wrong when you need "loading + previous data visible at the same time"
// — that requires two independent fields, not one sealed branch.
```

### Simple Data Flows

```kotlin
// BAD — event bus for local ViewModel→Screen communication
EventBus.post(WordDeletedEvent(wordId))

// BAD — complex state machine
enum class SyncState { IDLE, SYNCING, SUCCESS, FAILED, RETRYING }

// GOOD — direct method call (event sink)
viewModel.deleteWord(word)

// GOOD — simple boolean flag in state
data class SyncState(
    val isSyncing: Boolean = false,
    val lastSyncError: String? = null,
)
```

### Inline Logic vs Premature Abstraction

```kotlin
// BAD — generic transformer written "for future use", called exactly once
fun <T, R> Try<T>.flatMapWithLogging(tag: String, block: (T) -> Try<R>): Try<R> {
    Log.d(tag, "flatMapping")
    return flatMap(block)
}

// BAD — utility wrapper for something done in one place
fun <T> List<T>.toImmutableListSafe(): ImmutableList<T> = this.toImmutableList()

// GOOD — just write what you need where you need it
val words = rawWords.toImmutableList()
```

---

## Recognizing the Right Abstraction

| Signal | Action |
|---|---|
| Same logic copied ≥3 times | Extract to shared function/class |
| Same concept represented in 2 different ways | Canonicalize to one representation |
| "We might need X later" | Don't build X — add it when needed |
| Adding a parameter nobody currently uses | Remove it |
| A class with only one caller | Inline the class |
| An interface with only one impl AND tests don't need a fake | Remove the interface |
| A UseCase that only delegates with no logic | Inline it into the ViewModel |
| An `expect/actual` where both actuals are identical | Replace with a common-source API |

## When Abstraction IS Right

- Interface has 2+ implementations that currently exist
- Interface is needed for testing (enables fakes)
- Shared logic that encodes a genuine shared business rule
- The abstraction reduces cognitive load for readers
- The abstraction has a name that exists in the domain vocabulary