---
name: coroutines-flow
description: Write safe Kotlin Coroutines and Flow code — structured concurrency, Flow operators, StateFlow, SharedFlow, error handling, cancellation, and testing with Turbine
argument-hint: "<async operation or stream to implement>"
user-invocable: true
allowed-tools: ["Read", "Write", "Edit", "Glob", "Grep"]
---

# Kotlin Coroutines & Flow

## Structured Concurrency

Always launch coroutines in the correct scope. The scope defines lifetime — if the scope is cancelled, all child coroutines are cancelled.

```kotlin
// ViewModel — viewModelScope cancelled when VM is cleared
class WordViewModel : ViewModel() {
    fun load() {
        viewModelScope.launch {
            // cancelled when ViewModel is destroyed
        }
    }
}

// Fragment/Activity — lifecycleScope cancelled on destroy
lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { ... }
    }
}

// Repository — use coroutineScope / supervisorScope for parallel ops
suspend fun syncAll(): Try<Unit> = tryOf {
    coroutineScope {
        val words  = async { remote.fetchWords() }
        val tags   = async { remote.fetchTags() }
        local.save(words.await().getOrThrow(), tags.await().getOrThrow())
    }
}
```

### supervisorScope vs coroutineScope

```kotlin
// coroutineScope — one child failure cancels ALL siblings
coroutineScope {
    launch { riskyA() }   // fails → riskyB() is also cancelled
    launch { riskyB() }
}

// supervisorScope — children are independent; one failure doesn't cancel siblings
supervisorScope {
    launch { tryA() }     // fails → tryB() continues unaffected
    launch { tryB() }
}
```

### Try<T> Integration

Suspend repository functions return `Try<T>` — they never throw. Use `tryOf {}` to wrap any suspending work:

```kotlin
// Repository contract: suspend fun returns Try<T>, never throws
suspend fun fetchWords(): Try<List<Word>> = tryOf {
    withContext(Dispatchers.IO) { remote.getWords() }
}

// Caller unwraps safely
suspend fun refresh() {
    fetchWords()
        .onSuccess { words -> local.save(words) }
        .onFailure { e -> logError(e) }
}
```

---

## Dispatchers

```kotlin
Dispatchers.Main           // UI thread — Compose state updates, navigation
Dispatchers.Main.immediate // UI thread — no coroutine overhead if already on Main
Dispatchers.IO             // network, file I/O, database
Dispatchers.Default        // CPU-intensive work (sorting, parsing)
Dispatchers.Unconfined     // avoid — unpredictable thread
```

```kotlin
// withContext — switch dispatcher for a block
suspend fun fetchAndParse(url: String): List<Word> = withContext(Dispatchers.IO) {
    val json = httpClient.get(url).bodyAsText()
    withContext(Dispatchers.Default) {
        Json.decodeFromString<List<WordDto>>(json).map { it.toDomain() }
    }
}
```

In ViewModels with `viewModelScope`, the default dispatcher is `Main`. Use `withContext(Dispatchers.IO)` for blocking work.

---

## Flow

### Cold vs Hot

| | Cold Flow | Hot Flow |
|---|---|---|
| **Starts** | On each collector | Regardless of collectors |
| **Examples** | `flow {}`, `callbackFlow` | `StateFlow`, `SharedFlow` |
| **Multiple collectors** | Independent executions | Share the same upstream |

### flow {} Builder

```kotlin
fun observeNetworkStatus(): Flow<Boolean> = flow {
    while (true) {
        ensureActive()           // respect cancellation in tight loops
        emit(checkConnectivity())
        delay(5_000)
    }
}.flowOn(Dispatchers.IO)        // upstream runs on IO, downstream on caller's context
```

### callbackFlow — Wrap Callback APIs

```kotlin
fun observeLocationUpdates(): Flow<Location> = callbackFlow {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { trySend(it) }
        }
    }
    client.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
    awaitClose { client.removeLocationUpdates(callback) }
}
```

### channelFlow — Emit from Multiple Coroutines

Use `channelFlow` when you need to emit from multiple concurrent coroutines (e.g., merge a local cache with a network poll):

```kotlin
fun observeWithRefresh(): Flow<List<Word>> = channelFlow {
    launch { dao.observeAll().collect { send(it.map(WordEntity::toDomain)) } }
    launch {
        while (true) {
            delay(60_000)
            remote.fetchWords().onSuccess { send(it) }
        }
    }
}.flowOn(Dispatchers.IO)
```

### Key Operators

```kotlin
// Transform
flow.map { it.toDomain() }
flow.mapNotNull { it?.toDomain() }
flow.filter { it.isActive }
flow.filterNotNull()

// Side effects (don't emit, just observe)
flow.onStart { emit(Loading) }           // runs before first emission
flow.onEach { log(it) }                  // runs on each item without transforming
flow.onCompletion { cause -> cleanup() } // runs when flow ends (normally or via error)

// Flatten (for Flow of Flow)
flow.flatMapLatest { id -> repository.observeById(id) }  // cancel previous on new emission
flow.flatMapConcat { ... }  // sequential
flow.flatMapMerge { ... }   // concurrent

// Combine multiple flows
combine(flowA, flowB) { a, b -> Pair(a, b) }
zip(flowA, flowB) { a, b -> Pair(a, b) }   // waits for both
merge(flowA, flowB)                         // merge emissions from both

// Accumulate
flow.scan(initial) { acc, item -> acc + item }  // emit running total after each item
flow.runningFold(initial) { acc, item -> acc + item }  // alias for scan

// Backpressure
flow.buffer(capacity = 64)   // decouple producer and collector; producer runs ahead
flow.conflate()               // skip intermediate values; collector always gets latest

// Timing
flow.debounce(300)           // wait 300ms of silence before emitting (search box)
flow.sample(1000)            // emit latest every 1s
flow.distinctUntilChanged()  // skip duplicate consecutive emissions

// Collect via operator (alternative to launch { collect {} })
flow
    .onEach { render(it) }
    .launchIn(lifecycleScope)  // returns Job; scope cancels collection

// Terminal
flow.first()                 // get first emission (suspending)
flow.firstOrNull()
flow.toList()                // collect all into list (suspending)
flow.single()                // exactly one emission, else exception
```

### Error Handling

```kotlin
// catch {} — handle errors in upstream flow
flow
    .map { it.toDomain() }
    .catch { e ->
        emit(emptyList())      // emit fallback
        // or: throw e         // rethrow transformed
    }
    .collect { ... }

// retry
flow
    .retry(3) { e -> e is IOException }  // retry up to 3 times on IOException
    .catch { e -> handleFinalFailure(e) }
    .collect { ... }

// retryWhen — exponential backoff
flow.retryWhen { cause, attempt ->
    if (cause is IOException && attempt < 3) {
        delay(2.0.pow(attempt.toDouble()).toLong() * 1000)
        true
    } else false
}
```

Never use `try-catch` wrapping `collect {}` for Flow error handling — use `.catch {}` operator instead.

---

## StateFlow — Hot, Single Value

### Preferred: stateIn (cold → hot conversion)

Convert a cold repository Flow into a hot `StateFlow` in the ViewModel using `stateIn`. This is the recommended pattern — it avoids a separate `MutableStateFlow` and wires lifecycle automatically.

```kotlin
val state: StateFlow<WordListState> = getWordsUseCase()
    .map { words -> WordListState(words = words) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000), // 5s grace on config change
        initialValue = WordListState(),
    )
```

`SharingStarted` options:
- `WhileSubscribed(5_000)` — stops upstream 5s after last collector; **use for production ViewModels**
- `Eagerly` — starts immediately, never stops
- `Lazily` — starts on first collector, never stops

### Manual MutableStateFlow

When state is updated by ViewModel logic rather than derived from a flow:

```kotlin
private val _state = MutableStateFlow(WordListState())
val state: StateFlow<WordListState> = _state.asStateFlow()

// Update — .update {} is thread-safe (atomic CAS); prefer it over .value =
_state.update { current -> current.copy(isLoading = true) }
// .value = is fine only when you are certain you're on a single thread (e.g., Main)
```

### Collect (in screen — use repeatOnLifecycle)

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.state.collect { state -> render(state) }
    }
}
```

### StateFlow Rules
- Always one value — never null
- Conflates: fast producers, slow collectors only see latest value
- `distinctUntilChanged()` built-in — same value doesn't re-emit
- Initial value required

---

## SharedFlow — Hot, Multiple Subscribers, Events

```kotlin
// One-shot events (navigation, snackbars)
private val _effects = MutableSharedFlow<AuthEffect>(
    extraBufferCapacity = 16,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
)
val effects: SharedFlow<AuthEffect> = _effects.asSharedFlow()

// Emit
fun onLoginSuccess(user: User) {
    viewModelScope.launch { _effects.emit(AuthEffect.NavigateToHome) }
}
```

### Channel-Based Effects (Preferred for Single Collector)

When there is exactly one collector (the screen), prefer `Channel` — it guarantees delivery and avoids replay concerns:

```kotlin
private val _effects = Channel<AuthEffect>(Channel.BUFFERED)
val effects: Flow<AuthEffect> = _effects.receiveAsFlow()

fun navigate() {
    viewModelScope.launch { _effects.send(AuthEffect.NavigateToHome) }
}

// Screen — collect inside repeatOnLifecycle
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.effects.collect { effect -> handleEffect(effect) }
    }
}
```

### SharedFlow vs Channel

| | `SharedFlow` | `Channel` |
|---|---|---|
| **Subscribers** | Many | One |
| **Replay** | Configurable (default 0) | None |
| **Use for** | Broadcast events | Single-consumer pipeline |

---

## stateIn / shareIn — Convert Cold to Hot

```kotlin
// stateIn — cold Flow → StateFlow (single current value)
val latestUser: StateFlow<User?> = userRepository.observeUser()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

// shareIn — cold Flow → SharedFlow (multicast, configurable replay)
val sharedEvents: SharedFlow<Event> = eventSource()
    .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)
```

---

## Cancellation

```kotlin
// Check cancellation in CPU-bound loops
suspend fun processWords(words: List<Word>): List<Result> {
    return words.map { word ->
        ensureActive()   // throws CancellationException if scope is cancelled
        processWord(word)
    }
}

// withTimeout
val result = withTimeout(10_000) { fetchData() }  // TimeoutCancellationException on timeout
val resultOrNull = withTimeoutOrNull(10_000) { fetchData() }  // null on timeout

// CancellationException must always propagate
try {
    delay(1000)
} catch (e: CancellationException) {
    throw e   // always rethrow — cancellation must propagate
} catch (e: Exception) {
    handleError(e)
}

// NonCancellable — cleanup that must complete even on cancellation
try {
    doWork()
} finally {
    withContext(NonCancellable) { db.close() }
}
```

### CoroutineExceptionHandler

Handles uncaught exceptions from `launch {}` blocks (not `async {}`):

```kotlin
val handler = CoroutineExceptionHandler { _, throwable ->
    logError("Unhandled coroutine exception", throwable)
}

viewModelScope.launch(handler) {
    riskyOperation()   // exception caught by handler instead of crashing
}
```

---

## Testing Coroutines

```kotlin
class WordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()  // replaces Main with TestDispatcher

    @Test
    fun `loading words emits success state`() = runTest {
        val fakeRepo = FakeWordRepository()
        val vm = WordViewModel(GetWordsUseCase(fakeRepo))

        // Turbine for Flow
        vm.state.test {
            val initial = awaitItem()
            assertEquals(WordListState(), initial)

            fakeRepo.emit(listOf(testWord()))
            val loaded = awaitItem()
            assertEquals(1, loaded.words.size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}

// MainDispatcherRule
class MainDispatcherRule : TestWatcher() {
    val testDispatcher = UnconfinedTestDispatcher()
    override fun starting(d: Description) { Dispatchers.setMain(testDispatcher) }
    override fun finished(d: Description) { Dispatchers.resetMain() }
}
```

### UnconfinedTestDispatcher vs StandardTestDispatcher

| | `UnconfinedTestDispatcher` | `StandardTestDispatcher` |
|---|---|---|
| **Execution** | Eager — runs coroutines inline immediately | Lazy — coroutines must be explicitly advanced |
| **Use for** | Simple unit tests; quick state assertion | Time-sensitive tests; coroutines with `delay` |
| **Requires** | Nothing extra | `advanceUntilIdle()` / `advanceTimeBy()` |

Use `UnconfinedTestDispatcher` in `MainDispatcherRule` (simpler ViewModel tests). Use `StandardTestDispatcher` when you need control over virtual time.

### runTest Controls

```kotlin
runTest {
    // advanceTimeBy — virtual time
    advanceTimeBy(5_000)
    runCurrent()

    // advanceUntilIdle — run all pending coroutines
    advanceUntilIdle()

    // TestCoroutineScheduler
    testScheduler.advanceTimeBy(1000)
}
```

---

## Flows in Repository

```kotlin
// observeWords — converts Room Flow to domain Flow
fun observeWords(): Flow<List<Word>> =
    dao.observeAll()
        .map { entities -> entities.map(WordEntity::toDomain) }
        .catch { e -> emit(emptyList()) }  // never let DB errors crash the app
        .flowOn(Dispatchers.IO)            // upstream on IO thread

// Combine local + remote
fun observeWordsWithSync(): Flow<List<Word>> =
    combine(
        dao.observeAll().map { it.map(WordEntity::toDomain) },
        syncTrigger,
    ) { local, _ -> local }
```