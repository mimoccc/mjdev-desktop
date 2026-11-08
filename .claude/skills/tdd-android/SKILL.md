---
name: tdd-android
description: Test-Driven Development for Android/KMP — Red-Green-Refactor, ViewModel tests with Turbine, use case tests, repository tests with fakes, MockEngine for DataSource, and JUnit5 setup
argument-hint: "<class or feature to test-drive>"
user-invocable: true
allowed-tools: ["Read", "Write", "Edit", "Glob", "Grep"]
---

# TDD — Android / KMP

## Red → Green → Refactor

1. **Red** — Write a failing test for the behavior you want
2. **Green** — Write the minimum code to make it pass
3. **Refactor** — Clean up without breaking tests

Never write production code before a failing test exists.

---

## Test Pyramid

```
         ┌─────────────────┐
         │    UI Tests      │  Slowest — Compose UI, Screenshot
         ├─────────────────┤
         │ Integration Tests│  Real DB, real network (MockWebServer)
         ├─────────────────┤
         │   Unit Tests     │  Fastest — VM, UseCase, Repository, DataSource
         └─────────────────┘
```

**Focus**: 70% unit, 20% integration, 10% UI.

---

## JUnit5 Setup (Android)

```kotlin
// build.gradle.kts
dependencies {
    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.params)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    // avoid mockk — write fakes instead
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

```toml
# libs.versions.toml
junit5 = "5.11.3"
turbine = "1.2.0"
coroutines-test = "1.9.0"

[libraries]
junit5-api = { module = "org.junit.jupiter:junit-jupiter-api", version.ref = "junit5" }
junit5-params = { module = "org.junit.jupiter:junit-jupiter-params", version.ref = "junit5" }
junit5-engine = { module = "org.junit.jupiter:junit-jupiter-engine", version.ref = "junit5" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines-test" }
```

---

## ViewModel Tests with Turbine

### MainDispatcherRule

`TestWatcher` and `@get:Rule` are JUnit4 APIs — do not use them with JUnit5. Use
`BeforeEachCallback`/`AfterEachCallback` and `@RegisterExtension` instead.

```kotlin
// commonTest or androidTest
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext) { Dispatchers.setMain(dispatcher) }
    override fun afterEach(context: ExtensionContext) { Dispatchers.resetMain() }
}
```

### ViewModel Test Pattern

```kotlin
// No @ExtendWith needed — we use fakes, not mocks
class WordListViewModelTest {

    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    // Always use fakes over mocks for repositories
    private val fakeRepo = FakeWordRepository()
    private val getWords = GetDueWordsUseCase(fakeRepo)
    private val deleteWord = DeleteWordUseCase(fakeRepo)
    private lateinit var vm: WordListViewModel

    @BeforeEach
    fun setUp() {
        vm = WordListViewModel(getWords, deleteWord)
    }

    @Test
    fun `initial state is loading`() = runTest {
        vm.state.test {
            val state = awaitItem()
            assertTrue(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `words loaded successfully updates state`() = runTest {
        val words = listOf(testWord(id = 1), testWord(id = 2))

        vm.state.test {
            skipItems(1) // skip initial loading state

            fakeRepo.emitWords(words)
            val loaded = awaitItem()

            assertFalse(loaded.isLoading)
            assertEquals(2, loaded.words.size)
            assertNull(loaded.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `delete word emits undo effect`() = runTest {
        val word = testWord()
        fakeRepo.emitWords(listOf(word))

        vm.effects.test {
            vm.deleteWord(word)
            val effect = awaitItem()
            assertIs<WordListEffect.ShowUndo>(effect)
            assertEquals(word, (effect as WordListEffect.ShowUndo).word)
        }
    }

    @Test
    fun `delete word failure updates error state`() = runTest {
        val word = testWord()
        fakeRepo.setDeleteError(RuntimeException("DB error"))

        vm.state.test {
            skipItems(1) // skip initial loading state

            vm.deleteWord(word)
            val errorState = awaitItem()

            assertNotNull(errorState.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### Turbine: `cancelAndIgnoreRemainingEvents` vs `cancelAndConsumeRemainingEvents`

- `cancelAndIgnoreRemainingEvents()` — cancels the flow and silently drops any unread items. Use
  when you only care about specific items and the rest are irrelevant.
- `cancelAndConsumeRemainingEvents()` — cancels and returns the remaining events as a list. Use
  when you want to assert that no unexpected events arrived, or inspect what was left.

### Testing Debounce / Time-Based Logic

Use `advanceTimeBy` or `advanceUntilIdle` from `TestCoroutineScheduler` when the ViewModel
uses `delay`, `debounce`, or retry with backoff:

```kotlin
@Test
fun `search is debounced by 300ms`() = runTest {
    vm.state.test {
        skipItems(1) // initial

        vm.onSearchQueryChanged("h")
        vm.onSearchQueryChanged("he")
        vm.onSearchQueryChanged("hel")

        // No emission yet — debounce window not elapsed
        expectNoEvents()

        advanceTimeBy(300)

        val searched = awaitItem()
        assertEquals("hel", searched.query)
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## Try<T> — Custom Result Wrapper

`tryOf {}` is a project-level inline helper that wraps a suspending block in `Try<T>`:

```kotlin
// domain/util/Try.kt
sealed class Try<out T> {
    data class Success<T>(val value: T) : Try<T>()
    data class Failure<T>(val error: Throwable) : Try<T>()
}

inline fun <T> tryOf(block: () -> T): Try<T> = try {
    Try.Success(block())
} catch (e: Throwable) {
    Try.Failure(e)
}
```

All `suspend` repository methods return `Try<T>` — they never throw.

---

## Fake Repository Pattern

```kotlin
// test/fakes/FakeWordRepository.kt
class FakeWordRepository : IWordRepository {

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    private var deleteError: Throwable? = null
    private var saveError: Throwable? = null

    // Test helpers
    fun emitWords(words: List<Word>) { _words.value = words }
    fun setDeleteError(e: Throwable) { deleteError = e }
    fun setSaveError(e: Throwable) { saveError = e }

    // Interface implementation
    override fun observeWords(): Flow<List<Word>> = _words.asStateFlow()

    override suspend fun findById(id: Int): Try<Word> = tryOf {
        _words.value.find { it.id == id } ?: error("Word $id not found")
    }

    override suspend fun save(word: Word): Try<Word> = tryOf {
        saveError?.let { throw it }
        val updated = _words.value.toMutableList().also { list ->
            val idx = list.indexOfFirst { it.id == word.id }
            if (idx >= 0) list[idx] = word else list.add(word)
        }
        _words.value = updated
        word
    }

    override suspend fun delete(id: Int): Try<Unit> = tryOf {
        deleteError?.let { throw it }
        _words.value = _words.value.filter { it.id != id }
    }

    override suspend fun syncWithRemote(): Try<Unit> = Try.Success(Unit)
}
```

---

## Use Case Tests

Each test constructs its own state — avoid `@BeforeEach` seeds that some tests must undo.

```kotlin
class ReviewWordUseCaseTest {

    private val fakeRepo = FakeWordRepository()
    private val srsService = SpacedRepetitionService()
    private val useCase = ReviewWordUseCase(fakeRepo, srsService)

    @Test
    fun `correct review advances bucket and sets future review date`() = runTest {
        val word = testWord(id = 1, bucket = 2)
        fakeRepo.emitWords(listOf(word))

        val result = useCase(ReviewWordUseCase.Params(word, quality = 5))

        assertIs<Try.Success<Word>>(result)
        assertEquals(3, result.value.bucket)
        assertTrue(result.value.nextReviewDate > FIXED_DATE)
    }

    @Test
    fun `incorrect review resets bucket to 0 and sets tomorrow`() = runTest {
        val word = testWord(id = 1, bucket = 5)
        fakeRepo.emitWords(listOf(word))

        val result = useCase(ReviewWordUseCase.Params(word, quality = 1))

        assertIs<Try.Success<Word>>(result)
        assertEquals(0, result.value.bucket)
        assertEquals(FIXED_DATE.plus(1, DateTimeUnit.DAY), result.value.nextReviewDate)
    }

    @Test
    fun `repository failure propagates as Try Failure`() = runTest {
        val word = testWord()
        fakeRepo.emitWords(listOf(word))
        fakeRepo.setSaveError(RuntimeException("DB locked"))

        val result = useCase(ReviewWordUseCase.Params(word, quality = 5))

        assertIs<Try.Failure<Word>>(result)
    }
}
```

### Nested Tests for Grouping

Use `@Nested` to group related scenarios — keeps test output readable:

```kotlin
class WordListViewModelTest {

    @RegisterExtension val mainDispatcherRule = MainDispatcherRule()

    @Nested inner class `given empty repository` {
        @Test fun `state shows empty list`() = runTest { ... }
    }

    @Nested inner class `given words loaded` {
        @Test fun `state shows word count`() = runTest { ... }
        @Test fun `delete emits undo effect`() = runTest { ... }
    }

    @Nested inner class `given repository error` {
        @Test fun `state shows error message`() = runTest { ... }
    }
}
```

---

## DataSource Tests with MockEngine (Ktor)

```kotlin
class WordRemoteDataSourceTest {

    @Test
    fun `fetchAll returns mapped words on 200`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/words", request.url.encodedPath)
            respond(
                content = ByteReadChannel("""[{"id":1,"original":"hello","translated":"hola"}]"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = createHttpClient(mockEngine)
        val dataSource = WordRemoteDataSourceImpl(client)

        val result = dataSource.fetchAll()

        assertIs<Try.Success<List<WordDto>>>(result)
        assertEquals(1, result.value.size)
        assertEquals("hello", result.value[0].original)
    }

    @Test
    fun `fetchAll returns failure on 401`() = runTest {
        val mockEngine = MockEngine {
            respond(content = ByteReadChannel(""), status = HttpStatusCode.Unauthorized)
        }
        val client = createHttpClient(mockEngine)
        val dataSource = WordRemoteDataSourceImpl(client)

        val result = dataSource.fetchAll()

        assertIs<Try.Failure<List<WordDto>>>(result)
    }

    @Test
    fun `fetchAll returns failure on network error`() = runTest {
        val mockEngine = MockEngine { throw IOException("No route to host") }
        val client = createHttpClient(mockEngine)
        val dataSource = WordRemoteDataSourceImpl(client)

        val result = dataSource.fetchAll()

        assertIs<Try.Failure<List<WordDto>>>(result)
    }
}
```

---

## Repository Tests (Fake DataSources)

Fake DataSources mirror the Fake Repository pattern — expose test helpers, implement the interface.

```kotlin
// test/fakes/FakeWordLocalDataSource.kt
class FakeWordLocalDataSource : IWordLocalDataSource {

    private val _entities = MutableStateFlow<List<WordEntity>>(emptyList())
    val savedEntities: List<WordEntity> get() = _entities.value

    fun emit(entities: List<WordEntity>) { _entities.value = entities }

    override fun observeAll(): Flow<List<WordEntity>> = _entities.asStateFlow()
    override suspend fun replaceAll(entities: List<WordEntity>) { _entities.value = entities }
    override suspend fun deleteById(id: Int) {
        _entities.value = _entities.value.filter { it.id != id }
    }
}

// test/fakes/FakeWordRemoteDataSource.kt
class FakeWordRemoteDataSource : IWordRemoteDataSource {

    private var words: List<WordDto> = emptyList()
    private var fetchError: Throwable? = null

    fun setWords(words: List<WordDto>) { this.words = words }
    fun setFetchError(e: Throwable) { fetchError = e }

    override suspend fun fetchAll(): Try<List<WordDto>> = tryOf {
        fetchError?.let { throw it }
        words
    }
}
```

```kotlin
class WordRepositoryTest {

    private val fakeLocal = FakeWordLocalDataSource()
    private val fakeRemote = FakeWordRemoteDataSource()
    private val repo = WordRepositoryImpl(fakeLocal, fakeRemote)

    @Test
    fun `observeWords maps entities to domain`() = runTest {
        val entity = wordEntity(id = 1, original = "hello")
        fakeLocal.emit(listOf(entity))

        repo.observeWords().test {
            val words = awaitItem()
            assertEquals(1, words.size)
            assertEquals("hello", words[0].original)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `syncWithRemote replaces local data`() = runTest {
        fakeRemote.setWords(listOf(wordDto(id = 1), wordDto(id = 2)))

        val result = repo.syncWithRemote()

        assertIs<Try.Success<Unit>>(result)
        assertEquals(2, fakeLocal.savedEntities.size)
    }

    @Test
    fun `syncWithRemote returns failure when remote fetch fails`() = runTest {
        fakeRemote.setFetchError(IOException("timeout"))

        val result = repo.syncWithRemote()

        assertIs<Try.Failure<Unit>>(result)
        assertEquals(0, fakeLocal.savedEntities.size) // local data untouched
    }
}
```

---

## Parameterized Tests (JUnit5)

```kotlin
class SpacedRepetitionServiceTest {

    private val service = SpacedRepetitionService()

    @ParameterizedTest
    @CsvSource(
        "0, 5, 1",   // bucket=0, quality=5 → interval=1 day
        "1, 5, 3",   // bucket=1, quality=5 → interval=3 days
        "2, 5, 5",   // bucket=2, quality=5 → interval=5 days
        "5, 1, 1",   // quality<2 → reset to 1 day
    )
    fun `calculateNextReview returns correct interval`(
        bucket: Int,
        quality: Int,
        expectedDays: Long,
    ) {
        val word = testWord(bucket = bucket)
        val result = service.calculateNextReview(word, quality)
        assertEquals(FIXED_DATE.plus(expectedDays, DateTimeUnit.DAY), result)
    }
}
```

---

## Test Builders / Factories

Use a **fixed date** — never `LocalDate.now()` or `Clock.System.now()` in builders. Tests that
depend on the current date are fragile and can fail at midnight.

```kotlin
// test/builders/TestBuilders.kt — shared across all test modules

val FIXED_DATE: LocalDate = LocalDate(2025, 1, 1)
val FIXED_INSTANT: Instant = Instant.parse("2025-01-01T00:00:00Z")

fun testWord(
    id: Int = 1,
    original: String = "hello",
    translated: String = "hola",
    bucket: Int = 0,
    nextReviewDate: LocalDate = FIXED_DATE,
    createdAt: Instant = FIXED_INSTANT,
) = Word(id, original, translated, bucket, nextReviewDate, createdAt)

fun wordEntity(
    id: Int = 1,
    original: String = "hello",
    translated: String = "hola",
    srsLevel: Int = 0,
    nextReview: String = FIXED_DATE.toString(),
    createdAt: String = FIXED_INSTANT.toString(),
) = WordEntity(id, original, translated, srsLevel, nextReview, createdAt)

fun wordDto(
    id: Int = 1,
    original: String = "hello",
    translated: String = "hola",
) = WordDto(id, original, translated)
```

---

## Dispatcher Injection

Never hardcode `Dispatchers.IO` in production code — inject it so tests can replace it:

```kotlin
// Production
class WordRepositoryImpl(
    private val local: IWordLocalDataSource,
    private val remote: IWordRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IWordRepository {
    override suspend fun syncWithRemote(): Try<Unit> = withContext(ioDispatcher) { ... }
}

// In tests — use UnconfinedTestDispatcher (already set via MainDispatcherRule)
private val repo = WordRepositoryImpl(fakeLocal, fakeRemote, UnconfinedTestDispatcher())
```

---

## TDD Checklist

- [ ] Write the test BEFORE writing production code
- [ ] Test name describes behavior: `` `given X when Y then Z` ``
- [ ] One assertion concept per test
- [ ] Use fakes, not mocks — fakes produce real behavior
- [ ] Tests run fast (<100ms each) — no real network, no real disk I/O
- [ ] Test the contract, not the implementation
- [ ] All new ViewModels and UseCases have tests
- [ ] Cover happy path + failure + edge cases
- [ ] Parameterized tests for data-driven scenarios
- [ ] `MainDispatcherRule` uses JUnit5 `@RegisterExtension`, not JUnit4 `@get:Rule`
- [ ] No `LocalDate.now()` / `Clock.System.now()` in test builders — use fixed constants
- [ ] Dispatchers injected, not hardcoded — replaceable in tests
- [ ] `@Nested` used to group happy / failure / edge scenarios