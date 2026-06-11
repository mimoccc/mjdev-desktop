---
name: dependency-injection
description: Wire Android/KMP apps with Koin 4.x or Hilt — module declarations, scopes, qualifiers, ViewModel injection, KMP multiplatform modules, and testing with fakes
argument-hint: "<component or feature to wire with DI>"
user-invocable: true
allowed-tools: ["Read", "Write", "Edit", "Glob", "Grep"]
---

# Dependency Injection — Koin 4.x & Hilt

## Koin 4.x (Recommended for KMP)

### Setup

```kotlin
// shared/build.gradle.kts
dependencies {
    implementation(libs.koin.core)
    implementation(libs.koin.compose)              // koin-compose
    implementation(libs.koin.compose.viewmodel)    // koinViewModel()
}

// androidApp/build.gradle.kts
dependencies {
    implementation(libs.koin.android)
}
```

### Module Declaration

```kotlin
// commonMain — shared business logic DI
val domainModule = module {
    // Use Cases — factory (new instance each time)
    factory { GetDueWordsUseCase(get()) }
    factory { ReviewWordUseCase(get(), get()) }
    factory { SaveWordUseCase(get()) }

    // Domain Services — single (shared, stateless)
    single { SpacedRepetitionService() }
}

val dataModule = module {
    // Repository — single (shared state across features)
    // Impl returns Try<T> as mandated by the layer contract
    single<IWordRepository> { WordRepositoryImpl(get(), get()) }
    single<IUserRepository> { UserRepositoryImpl(get(), get()) }

    // DataSources — single
    single<IWordLocalDataSource> { WordLocalDataSourceImpl(get()) }
    single<IWordRemoteDataSource> { WordRemoteDataSourceImpl(get()) }
}

val networkModule = module {
    single { createHttpClient(get()) }
}
```

### ViewModel Injection

```kotlin
// commonMain — declare with viewModel {}
val presentationModule = module {
    viewModel { WordListViewModel(get(), get()) }
    viewModel { StudyViewModel(get(), get(), get()) }
    // SavedStateHandle is injected automatically by Koin
    viewModel { WordDetailViewModel(get(), get<SavedStateHandle>()) }
}

// Screen — inject with koinViewModel()
@Composable
fun WordListScreen() {
    val viewModel = koinViewModel<WordListViewModel>()
    // ...
}

// Screen with navigation argument via SavedStateHandle
@Composable
fun WordDetailScreen() {
    // wordId read from SavedStateHandle inside the ViewModel
    val viewModel = koinViewModel<WordDetailViewModel>()
}
```

### Platform Modules — `expect/actual` Pattern

```kotlin
// commonMain — declare the contract
expect val platformModule: Module

// androidMain
actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single { DatabaseDriverFactory(androidContext()) }
    single<IPlatformNotifications> { AndroidNotificationsImpl(androidContext()) }
    single<ISecureStorage> { AndroidSecureStorageImpl(androidContext()) }
}

// iosMain
actual val platformModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single { DatabaseDriverFactory() }
    single<IPlatformNotifications> { IosNotificationsImpl() }
    single<ISecureStorage> { IosKeychainStorageImpl() }
}
```

### App Initialization

```kotlin
// Android — Application.onCreate()
class LexiconApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LexiconApp)
            androidLogger(Level.ERROR)    // Level.DEBUG in development
            modules(
                domainModule,
                dataModule,
                networkModule,
                presentationModule,
                platformModule,           // resolves to androidMain actual
            )
        }
    }
}

// iOS — Swift entry point (call from Swift AppDelegate / @main)
fun initKoin() {
    startKoin {
        modules(
            domainModule,
            dataModule,
            networkModule,
            presentationModule,
            platformModule,               // resolves to iosMain actual
        )
    }
}
```

```swift
// Swift — AppDelegate.swift
@main
struct LexiconApp: App {
    init() { KoinKt.doInitKoin() }
}
```

---

## Koin Scopes

### Feature Scopes — Lifetime Tied to a Feature

```kotlin
// Define scope
val featureModule = module {
    scope<StudyScope> {
        scoped { StudySessionManager(get()) }
        scoped { StreakTracker(get()) }
        viewModel { StudyViewModel(get(), get()) }
    }
}

// Android — create and close with lifecycle
class StudyActivity : AppCompatActivity() {
    private val scope: Scope by lazy {
        getKoin().createScope<StudyScope>()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.close()
    }
}
```

---

## Module Composition with `includes()`

Use `includes()` to reuse modules inside feature modules without re-declaring bindings.

```kotlin
val studyFeatureModule = module {
    includes(domainModule, dataModule)
    viewModel { StudyViewModel(get()) }
}
```

---

## Qualifiers — Multiple Implementations

```kotlin
// Define qualifier constants
val IoDispatcher      = named("IoDispatcher")
val DefaultDispatcher = named("DefaultDispatcher")
val MainDispatcher    = named("MainDispatcher")

val dispatchersModule = module {
    single(IoDispatcher)      { Dispatchers.IO }
    single(DefaultDispatcher) { Dispatchers.Default }
    single(MainDispatcher)    { Dispatchers.Main }
}

// In module
single<IWordRepository> {
    WordRepositoryImpl(
        ioDispatcher = get(IoDispatcher),
    )
}
```

---

## Lazy Injection & Delegation

Use `KoinComponent` only in platform/infrastructure layer classes — never in domain.

```kotlin
// data / platform layer only
class WordNotificationService : KoinComponent {
    private val repository: IWordRepository by inject()   // lazy
    private val settings: AppSettings by inject()
}

// Direct get — resolve immediately
class AppInitializer : KoinComponent {
    fun init() {
        val settings: AppSettings = get()
        settings.migrate()
    }
}
```

---

## Testing with Koin

### Graph Verification

Catch missing bindings at test time — run once per module set.

```kotlin
class KoinGraphTest : KoinTest {
    @Test
    fun `verify full koin graph`() {
        val app = KoinApplication.init()
        app.modules(domainModule, dataModule, networkModule, presentationModule, platformModule)
        app.checkModules()
    }
}
```

### Unit Tests — Prefer Constructor Injection (No Koin)

```kotlin
// Best — direct construction, no Koin overhead
class WordListViewModelTest {
    private val fakeRepo = FakeWordRepository()
    private val useCase  = GetDueWordsUseCase(fakeRepo)
    private val vm       = WordListViewModel(useCase)

    @Test
    fun `loading state transitions correctly`() = runTest { ... }
}
```

### Integration Tests — KoinTest

```kotlin
class WordListIntegrationTest : KoinTest {

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single<IWordRepository> { FakeWordRepository() }
                    single { GetDueWordsUseCase(get()) }
                    viewModel { WordListViewModel(get()) }
                }
            )
        }
    }

    @After
    fun tearDown() = stopKoin()

    @Test
    fun `loading state transitions correctly`() = runTest {
        val viewModel: WordListViewModel by inject()
        // test...
    }
}
```

---

## Hilt (Android-only projects)

### Setup

```kotlin
// app/build.gradle.kts
plugins {
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
}
```

### Module Declaration

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun provideWordApiService(client: OkHttpClient): WordApiService =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(kotlinx.serialization converter)
            .build()
            .create(WordApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindWordRepository(impl: WordRepositoryImpl): IWordRepository
}
```

### ViewModel Injection with Hilt

```kotlin
@HiltViewModel
class WordListViewModel @Inject constructor(
    private val getDueWords: GetDueWordsUseCase,
    private val deleteWord: DeleteWordUseCase,
) : ViewModel() { ... }

// ViewModel with navigation argument via SavedStateHandle
@HiltViewModel
class WordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,       // injected automatically from NavBackStackEntry
    private val getWord: GetWordUseCase,
) : ViewModel() {
    private val wordId: Int = checkNotNull(savedStateHandle["wordId"])
}

// Screen — no factory needed
@Composable
fun WordListScreen(
    viewModel: WordListViewModel = hiltViewModel(),
) { ... }
```

### Scoped Components

```kotlin
// Activity-scoped — destroyed with activity
@Module
@InstallIn(ActivityComponent::class)
object ActivityModule { ... }

// ViewModel-scoped — destroyed with ViewModel
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule { ... }
```

### Testing with Hilt

```kotlin
@HiltAndroidTest
class WordListViewModelTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val mainDispatcherRule = MainDispatcherRule()

    // Field injection is required here — Hilt test infrastructure does not support
    // constructor injection for @HiltAndroidTest classes
    @Inject lateinit var viewModel: WordListViewModel

    @Before
    fun setUp() = hiltRule.inject()
}

// Replace bindings in tests
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class],
)
abstract class FakeRepositoryModule {
    @Binds @Singleton
    abstract fun bindFakeWordRepository(fake: FakeWordRepository): IWordRepository
}
```

---

## Layer Contract Reminder

DI wires implementations to interfaces — the return types must match the layer contract from CLAUDE.md:

| Binding | Interface Return Type |
|---|---|
| `IWordRepository.getWord()` | `Try<Word>` |
| `IWordRepository.observeWords()` | `Flow<List<Word>>` |
| UseCase | `Try<T>` or `Flow<T>` |

Never let the DI module paper over a missing `Try<T>` — the interface must enforce it.

---

## DI Decision Guide

| Need | Choice |
|---|---|
| KMP (Android + iOS) | Koin 4.x + `expect/actual` platform modules |
| Android-only, large team | Hilt (compile-time safety) |
| Android-only, simple app | Koin (less boilerplate) |
| Unit testing | Constructor injection — no DI framework |

## Anti-Patterns

- Never inject `Context` into domain or data layer classes — pass at app boundary
- Never use `GlobalContext.get()` / `KoinComponent` in domain classes — breaks portability
- Never use field injection when constructor injection is possible (Hilt `@HiltAndroidTest` is the sole exception)
- Never declare `single {}` for `ViewModel` — use `viewModel {}` for proper scope
- Avoid circular dependencies — restructure to break cycles
- Never skip `checkModules()` in CI — a missing binding will crash at runtime, not compile time