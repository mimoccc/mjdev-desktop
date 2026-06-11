---
name: clean-architecture
description: Structure Android/KMP projects in clean architecture layers — domain, data, presentation — with strict dependency inversion, layer contracts, and error propagation rules
argument-hint: "<feature or layer to design>"
user-invocable: true
allowed-tools: ["Read", "Write", "Edit", "Glob", "Grep"]
---

# Clean Architecture — Android / KMP

## Layer Overview

```
┌──────────────────────────────────────────┐
│         Presentation (UI)                │
│  Composables · ViewModels · Navigation   │
├──────────────────────────────────────────┤
│           Domain (Core)                  │
│  Use Cases · Models · Repo Interfaces    │
├──────────────────────────────────────────┤
│              Data                        │
│  Repo Impls · DataSources · DTOs/Entities│
├──────────────────────────────────────────┤
│         Framework / Platform             │
│  Room · Ktor · SQLDelight · Firebase     │
└──────────────────────────────────────────┘
       Dependencies point INWARD only
```

**The Dependency Rule**: source code dependencies can only point toward the center. Domain knows nothing about data, presentation, or any framework.

---

## Core Utilities (`:core:common`)

Define these shared types once. All layers depend on `:core:common`.

### Try\<T\>

```kotlin
// core/common/Try.kt
sealed class Try<out T> {
    data class Success<T>(val value: T) : Try<T>()
    data class Failure(val error: Throwable) : Try<Nothing>()

    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw error
    }

    fun <R> map(transform: (T) -> R): Try<R> = when (this) {
        is Success -> tryOf { transform(value) }
        is Failure -> this
    }

    fun reduce(onSuccess: (T) -> Unit, onFailure: (Throwable) -> Unit) = when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(error)
    }
}

suspend fun <T> tryOf(block: suspend () -> T): Try<T> = try {
    Try.Success(block())
} catch (e: Exception) {
    Try.Failure(e)
}

fun <T> tryOf(block: () -> T): Try<T> = try {
    Try.Success(block())
} catch (e: Exception) {
    Try.Failure(e)
}
```

### Use Case Base Classes

```kotlin
// core/common/usecase/UseCase.kt
interface UseCase<in P, out T> {
    suspend operator fun invoke(params: P): Try<T>
}

// core/common/usecase/FlowUseCase.kt
interface FlowUseCase<in P, out T> {
    operator fun invoke(params: P): Flow<T>
}
```

### BaseViewModel

```kotlin
// core/common/viewmodel/BaseViewModel.kt
abstract class BaseViewModel<S, E> : ViewModel() {
    private val _state: MutableStateFlow<S> by lazy { MutableStateFlow(initialState()) }
    val state: StateFlow<S> get() = _state.asStateFlow()

    private val _effects = MutableSharedFlow<E>(extraBufferCapacity = 16)
    val effects: SharedFlow<E> = _effects.asSharedFlow()

    abstract fun initialState(): S

    protected fun updateState(update: S.() -> S) {
        _state.update { it.update() }
    }

    protected fun emitEffect(effect: E) {
        viewModelScope.launch { _effects.emit(effect) }
    }
}

// Prefer this over collectAsStateWithLifecycle()
@Composable
fun <S, E> BaseViewModel<S, E>.collectState(): State<S> = state.collectAsState()
```

---

## Domain Layer — Pure Kotlin

No Android SDK, no Ktor, no Room, no Compose. Only pure Kotlin and `kotlinx` libraries.

### Domain Models

```kotlin
// domain/model/Word.kt
data class Word(
    val id: Int,
    val original: String,
    val translated: String,
    val bucket: Int,
    val nextReviewDate: LocalDate,
    val createdAt: Instant,
)

// Pass today as a parameter — keeps the function pure and testable
fun Word.isDue(today: LocalDate): Boolean = nextReviewDate <= today
```

### Repository Interfaces (in domain)

```kotlin
// domain/repository/IWordRepository.kt
interface IWordRepository {
    fun observeWords(): Flow<List<Word>>         // stream — never Flow<Try<T>>
    suspend fun findById(id: Int): Try<Word>     // one-shot — always Try<T>
    suspend fun save(word: Word): Try<Word>
    suspend fun delete(id: Int): Try<Unit>
    suspend fun syncWithRemote(): Try<Unit>
}
```

Rules:
- Interface lives in `domain`; implementation lives in `data`
- Suspend ops return `Try<T>` — never throw
- Stream ops return `Flow<T>` — never `Flow<Try<T>>`
- No framework types in signatures (`Response<T>`, `Entity`, `Cursor`)

### Use Cases

```kotlin
// domain/usecase/GetDueWordsUseCase.kt
class GetDueWordsUseCase(
    private val repository: IWordRepository,
    private val clock: Clock = Clock.System,
) : FlowUseCase<Unit, List<Word>> {
    override operator fun invoke(params: Unit): Flow<List<Word>> =
        repository.observeWords().map { words ->
            val today = clock.todayIn(TimeZone.currentSystemDefault())
            words.filter { it.isDue(today) }.sortedBy { it.nextReviewDate }
        }
}

// domain/usecase/ReviewWordUseCase.kt
class ReviewWordUseCase(
    private val repository: IWordRepository,
    private val srsService: SpacedRepetitionService,
) : UseCase<ReviewWordUseCase.Params, Word> {
    data class Params(val word: Word, val quality: Int)

    override suspend operator fun invoke(params: Params): Try<Word> {
        val nextDate = srsService.calculateNextReview(params.word, params.quality)
        val updated  = params.word.copy(
            bucket         = params.word.bucket + if (params.quality >= 3) 1 else 0,
            nextReviewDate = nextDate,
        )
        return repository.save(updated)
    }
}
```

### Domain Services

For complex business logic involving multiple models or repositories:

```kotlin
// domain/service/SpacedRepetitionService.kt
// Inject Clock so calculateNextReview() is deterministic in tests
class SpacedRepetitionService(
    private val clock: Clock = Clock.System,
) {
    fun calculateNextReview(word: Word, quality: Int): LocalDate {
        val today = clock.todayIn(TimeZone.currentSystemDefault())
        val interval = when {
            quality < 2      -> 1
            word.bucket == 0 -> 1
            word.bucket == 1 -> 3
            else             -> (word.bucket * 2.5).roundToInt()
        }
        return today.plus(interval, DateTimeUnit.DAY)
    }
}
```

---

## Data Layer

### Data Source Interfaces — live in `data`, not `domain`

Data sources are implementation details of the data layer. Their interfaces use data-layer types (`WordEntity`, `WordDto`) and must **not** be placed in `domain`.

```kotlin
// data/datasource/IWordLocalDataSource.kt
interface IWordLocalDataSource {
    fun observeAll(): Flow<List<WordEntity>>
    suspend fun findById(id: Int): WordEntity?
    suspend fun upsert(entity: WordEntity)
    suspend fun replaceAll(entities: List<WordEntity>)
    suspend fun deleteById(id: Int)
}

// data/datasource/IWordRemoteDataSource.kt
interface IWordRemoteDataSource {
    suspend fun fetchAll(): Try<List<WordDto>>
}

// data/datasource/WordLocalDataSourceImpl.kt
class WordLocalDataSourceImpl(
    private val dao: WordDao,
) : IWordLocalDataSource {
    override fun observeAll(): Flow<List<WordEntity>> = dao.observeAll()
    override suspend fun findById(id: Int): WordEntity? = dao.findById(id)
    override suspend fun upsert(entity: WordEntity) = dao.upsert(entity)
    override suspend fun replaceAll(entities: List<WordEntity>) = dao.replaceAll(entities)
    override suspend fun deleteById(id: Int) = dao.deleteById(id)
}
```

### Repository Implementation

```kotlin
// data/repository/WordRepositoryImpl.kt
class WordRepositoryImpl(
    private val local: IWordLocalDataSource,
    private val remote: IWordRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IWordRepository {

    override fun observeWords(): Flow<List<Word>> =
        local.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun findById(id: Int): Try<Word> = tryOf {
        local.findById(id)?.toDomain() ?: error("Word $id not found")
    }

    override suspend fun save(word: Word): Try<Word> = tryOf {
        local.upsert(word.toEntity())
        word
    }

    override suspend fun syncWithRemote(): Try<Unit> = withContext(ioDispatcher) {
        tryOf {
            val dtos = remote.fetchAll().getOrThrow()
            local.replaceAll(dtos.map { it.toEntity() })
        }
    }
}
```

### Mappers — Extension Functions

```kotlin
// data/mapper/WordMapper.kt

// Entity → Domain
fun WordEntity.toDomain(): Word = Word(
    id             = id,
    original       = originalWord,
    translated     = translatedWord,
    bucket         = srsLevel,
    nextReviewDate = LocalDate.parse(nextReview),
    createdAt      = Instant.parse(createdAt),
)

// Domain → Entity
fun Word.toEntity(): WordEntity = WordEntity(
    id             = id,
    originalWord   = original,
    translatedWord = translated,
    srsLevel       = bucket,
    nextReview     = nextReviewDate.toString(),
    createdAt      = createdAt.toString(),
)

// DTO → Entity (remote → local storage; no domain model involved)
fun WordDto.toEntity(): WordEntity = WordEntity(
    id             = id,
    originalWord   = original,
    translatedWord = translated,
    srsLevel       = bucket ?: 0,
    nextReview     = nextReviewDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault()).toString(),
    createdAt      = createdAt ?: Clock.System.now().toString(),
)
```

Rules:
- Always extension functions on the **source** type: `Entity.toDomain()`, `Domain.toEntity()`, `Dto.toEntity()`
- No business logic in mappers — pure structural transformation
- Mappers live in `data` — domain models never import data types

---

## Presentation Layer

### State and Effects

```kotlin
// feature/words/WordListContract.kt
@Immutable
data class WordListState(
    val words: ImmutableList<Word> = persistentListOf(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface WordListEffect {
    data class ShowUndo(val word: Word) : WordListEffect
    data class ShowError(val message: String) : WordListEffect
}
```

### ViewModel

```kotlin
// feature/words/WordListViewModel.kt
class WordListViewModel(
    private val getWords: GetDueWordsUseCase,
    private val deleteWord: DeleteWordUseCase,
) : BaseViewModel<WordListState, WordListEffect>() {

    override fun initialState() = WordListState()

    init { observeWords() }

    private fun observeWords() {
        viewModelScope.launch {
            getWords(Unit).collect { words ->
                updateState { copy(words = words.toImmutableList(), isLoading = false) }
            }
        }
    }

    fun deleteWord(word: Word) {
        viewModelScope.launch {
            deleteWord(word.id).reduce(
                onSuccess = { emitEffect(WordListEffect.ShowUndo(word)) },
                onFailure = { e -> emitEffect(WordListEffect.ShowError(e.message ?: "Unknown error")) },
            )
        }
    }
}
```

### Screen Composable

```kotlin
// feature/words/WordListScreen.kt
@Composable
fun WordListScreen(
    viewModel: WordListViewModel = koinViewModel(),
) {
    val state by viewModel.collectState()
    val snackbarHostState = remember { SnackbarHostState() }

    // One-shot effects via OnEvents — never LaunchedEffect for navigation/effects
    OnEvents(viewModel.effects) { effect ->
        when (effect) {
            is WordListEffect.ShowUndo  -> snackbarHostState.showSnackbar("Deleted")
            is WordListEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    WordListContent(
        state = state,
        onDeleteWord = viewModel::deleteWord,
    )
}

// Stateless — only receives state and lambdas, no ViewModel reference
@Composable
private fun WordListContent(
    state: WordListState,
    onDeleteWord: (Word) -> Unit,
    modifier: Modifier = Modifier,
) {
    // pure rendering
}
```

Rules:
- `viewModel.collectState()` — never `collectAsStateWithLifecycle()`
- `OnEvents` for one-shot effects — never `LaunchedEffect` for navigation/side effects
- Only the root screen composable knows about the ViewModel; pass lambdas and state down

### Data Flow

```
Screen
  ↓  observes state via viewModel.collectState()
  ↓  calls event sink methods (plain ViewModel functions)
ViewModel
  ↓  invokes use cases
  ↓  updateState {} / emitEffect {}
Use Case
  ↓  calls repository interface methods
  ↓  applies domain business logic
Repository Impl
  ↓  coordinates local + remote data sources
DataSource Impls → Room / Ktor / SQLDelight / Firebase
```

---

## Module Boundaries (Gradle)

```
:app
  → :feature:words  :feature:study  :feature:auth  :feature:profile
      → :domain  :core:*  :resources
      ↛ :feature:*  (features NEVER depend on each other)
:core:design-system → Compose only  (no :domain, no :core:network)
:core:testing → commonTest only     (never on production classpath)
```

Each `:feature:X` must:
- Expose a `NavGraphBuilder` extension — wired in `:app`
- Expose a Koin module — wired in `:app`
- Use convention plugins — no duplicated Gradle boilerplate

### Forbidden Imports

| Module | Must NOT import |
|---|---|
| `:domain` | Room, Ktor, Koin, Hilt, Compose, Android SDK, any framework |
| `:feature:*` | Any other `:feature:*` module |
| `:core:design-system` | `:domain`, `:data`, `:feature:*` |
| `:core:common` | `:feature:*`, `:app` |
| `:core:testing` | Any production module on production classpath |

---

## Error Propagation

```
DataSource    → throws (framework exceptions bubble up naturally)
Repository    → wraps in tryOf {}, never throws outward
Use Case      → passes through / transforms Try<T>
ViewModel     → .reduce(onSuccess, onFailure) → updateState / emitEffect
Screen        → renders error state or shows snackbar via OnEvents
```

No exceptions should escape the repository layer unhandled.

---

## Testing Each Layer

### Fake Repository (fakes over mocks)

```kotlin
// core/testing/fake/FakeWordRepository.kt
class FakeWordRepository : IWordRepository {
    val words = mutableListOf<Word>()
    var shouldFail = false

    override fun observeWords(): Flow<List<Word>> = flowOf(words.toList())

    override suspend fun findById(id: Int): Try<Word> =
        if (shouldFail) Try.Failure(Exception("forced failure"))
        else words.firstOrNull { it.id == id }
            ?.let { Try.Success(it) }
            ?: Try.Failure(Exception("Word $id not found"))

    override suspend fun save(word: Word): Try<Word> = tryOf {
        words.removeAll { it.id == word.id }
        words.add(word)
        word
    }

    override suspend fun delete(id: Int): Try<Unit> = tryOf {
        words.removeAll { it.id == id }
    }

    override suspend fun syncWithRemote(): Try<Unit> = Try.Success(Unit)
}
```

### ViewModel Test (Turbine)

```kotlin
class WordListViewModelTest {
    private val repository = FakeWordRepository()
    private val fakeClock = FakeClock(today = LocalDate(2025, 1, 10))
    private val getWords = GetDueWordsUseCase(repository, fakeClock)
    private val deleteWord = DeleteWordUseCase(repository)
    private val viewModel by lazy { WordListViewModel(getWords, deleteWord) }

    @Test
    fun `loads due words on init`() = runTest {
        val dueWord = wordFixture(nextReviewDate = LocalDate(2025, 1, 9))  // before today
        repository.words.add(dueWord)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(listOf(dueWord), state.words.toList())
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `delete word emits ShowUndo effect`() = runTest {
        val word = wordFixture()
        repository.words.add(word)

        viewModel.effects.test {
            viewModel.deleteWord(word)
            assertEquals(WordListEffect.ShowUndo(word), awaitItem())
        }
    }

    @Test
    fun `delete failure emits ShowError effect`() = runTest {
        val word = wordFixture()
        repository.words.add(word)
        repository.shouldFail = true

        viewModel.effects.test {
            viewModel.deleteWord(word)
            assertIs<WordListEffect.ShowError>(awaitItem())
        }
    }
}
```

### Layer Test Summary

| Layer | Test approach | Tools |
|---|---|---|
| Domain / Use Case | Unit — pure logic, inject `FakeClock` | `kotlin-test`, fakes |
| Repository | Unit — fake data sources | `FakeWordLocalDataSource` |
| Data Source | Unit — fake HTTP / in-memory DB | `MockEngine`, in-memory Room |
| ViewModel | Unit — state & effects | Turbine, fake use cases |
| Integration | Instrumented | Hilt testing, real DB |
| UI | Compose UI testing | `composeTestRule` |