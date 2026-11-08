---
name: kmp-patterns
description: Build Kotlin Multiplatform (KMP) shared code — source sets, expect/actual, platform bridges, KMP-compatible libraries, and iOS/Android interop patterns
argument-hint: "<KMP feature or platform bridge to implement>"
user-invocable: true
allowed-tools: ["Read", "Write", "Edit", "Glob", "Grep"]
---

# Kotlin Multiplatform (KMP) Patterns

## Source Set Structure

```
composeApp/src/
├── commonMain/kotlin/      ← shared business logic, UI (Compose MP)
├── androidMain/kotlin/     ← Android-specific implementations
├── iosMain/kotlin/         ← iOS-specific implementations (Kotlin/Native)
├── commonTest/kotlin/      ← shared tests (kotlin-test)
├── androidTest/kotlin/     ← Android instrumented tests
└── iosTest/kotlin/         ← iOS-specific tests (rare)
```

### build.gradle.kts Source Sets

```kotlin
kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    listOf(
        iosX64(), iosArm64(), iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)   // viewModel {} DSL in commonMain
            implementation(libs.sqldelight.runtime)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.koin.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}
```

---

## expect / actual Pattern

### Simple Value

```kotlin
// commonMain — declaration
expect val platformName: String

// androidMain — implementation
actual val platformName: String = "Android ${android.os.Build.VERSION.SDK_INT}"

// iosMain — implementation
actual val platformName: String = UIDevice.currentDevice.systemName()
```

### Class with Platform Behaviour

```kotlin
// commonMain
expect class PlatformCrypto() {
    fun hash(input: String): String
    fun generateSecureToken(): String
}

// androidMain
actual class PlatformCrypto actual constructor() {
    actual fun hash(input: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("") { acc, b -> acc + "%02x".format(b) }

    actual fun generateSecureToken(): String =
        java.util.UUID.randomUUID().toString()
}

// iosMain
actual class PlatformCrypto actual constructor() {
    actual fun hash(input: String): String {
        val data = input.encodeToByteArray().toNSData()
        val digest = UByteArray(CC_SHA256_DIGEST_LENGTH.toInt())
        digest.usePinned { pinned ->
            CC_SHA256(data.bytes, data.length.toUInt(), pinned.addressOf(0))
        }
        return digest.joinToString("") { it.toString(16).padStart(2, '0') }
    }
    actual fun generateSecureToken(): String = NSUUID().UUIDString
}
// Note: import platform.CoreCrypto.CC_SHA256, platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
```

### Interface-based Platform Bridge (preferred for testability)

```kotlin
// commonMain/platform/IPlatformNotifications.kt
interface IPlatformNotifications {
    fun scheduleReviewReminder(wordCount: Int, delayMinutes: Int)
    fun cancelAll()
}

expect fun createPlatformNotifications(): IPlatformNotifications

// androidMain
actual fun createPlatformNotifications(): IPlatformNotifications =
    AndroidNotificationsImpl()   // uses WorkManager / AlarmManager

// iosMain
actual fun createPlatformNotifications(): IPlatformNotifications =
    IosNotificationsImpl()       // uses UNUserNotificationCenter
```

### actual typealias — Wrapping Platform Types

The most ergonomic pattern when you want to expose a platform type through a common interface:

```kotlin
// commonMain
expect class PlatformContext

// androidMain
actual typealias PlatformContext = android.content.Context

// iosMain — wrap with a no-arg class (NSObject or custom)
actual typealias PlatformContext = NSObject
```

Use `actual typealias` whenever the platform already has the exact class you need — avoid wrapping it in a new `actual class` unless the API needs to be shaped differently.

> **Note:** `@OptionalExpectation` was deprecated in Kotlin 1.9. For Android-only annotations, prefer the `actual typealias` pattern above or a default no-op `actual` implementation.

---

## SQLDelight — Multiplatform Database

### Driver Factory Pattern

```kotlin
// commonMain
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

// androidMain
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(AppDatabase.Schema, context, "app.db")
}

// iosMain
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(AppDatabase.Schema, "app.db")
}
```

### Schema Definition (.sq files)

```sql
-- commonMain/sqldelight/com/example/db/Word.sq
CREATE TABLE IF NOT EXISTS Word (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    original     TEXT NOT NULL,
    translated   TEXT NOT NULL,
    srs_level    INTEGER NOT NULL DEFAULT 0,
    next_review  TEXT NOT NULL,
    created_at   TEXT NOT NULL
);

getAllWords:
SELECT * FROM Word ORDER BY next_review ASC;

getDueWords:
SELECT * FROM Word WHERE next_review <= :today ORDER BY next_review ASC;

upsertWord:
INSERT OR REPLACE INTO Word(id, original, translated, srs_level, next_review, created_at)
VALUES (?, ?, ?, ?, ?, ?);

deleteById:
DELETE FROM Word WHERE id = :id;
```

---

## Ktor — Multiplatform HTTP Client

```kotlin
// commonMain — shared client setup
fun createHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(HttpTimeout) {
        requestTimeoutMillis  = 30_000
        connectTimeoutMillis  = 15_000
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level  = if (isDebug) LogLevel.HEADERS else LogLevel.NONE  // see expect/actual below
    }
}

// commonMain — isDebug bridge (BuildConfig.DEBUG is Android-only, don't use in commonMain)
expect val isDebug: Boolean
// androidMain: actual val isDebug: Boolean = BuildConfig.DEBUG
// iosMain:     actual val isDebug: Boolean = Platform.isDebugBinary

// DI — provide engine per platform
// androidMain: HttpClient(OkHttp) { ... }
// iosMain: HttpClient(Darwin) { ... }
```

---

## kotlinx.datetime — Multiplatform Dates

```kotlin
import kotlinx.datetime.*

// Current time
val now: Instant     = Clock.System.now()
val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

// Arithmetic
val tomorrow  = today.plus(1, DateTimeUnit.DAY)
val nextWeek  = today.plus(DatePeriod(days = 7))
val daysUntil = today.until(targetDate, DateTimeUnit.DAY)

// Formatting (use kotlinx-datetime 0.6+)
val formatted = today.format(LocalDate.Format {
    monthName(MonthNames.ENGLISH_FULL)
    chars(" ")
    dayOfMonth()
    chars(", ")
    year()
})
```

---

## kotlinx.serialization

```kotlin
@Serializable
data class WordDto(
    val id: Int,
    val original: String,
    val translated: String,
    @SerialName("srs_level") val srsLevel: Int = 0,
    @SerialName("next_review") val nextReview: String? = null,
)

// Polymorphic serialization
@Serializable
sealed interface ApiResponse {
    @Serializable @SerialName("success") data class Success(val data: WordDto) : ApiResponse
    @Serializable @SerialName("error")   data class Error(val message: String) : ApiResponse
}
```

---

## Koin — Multiplatform DI

```kotlin
// commonMain DI module
val commonModule = module {
    single { createHttpClient(get()) }
    single { WordRepositoryImpl(get(), get()) as IWordRepository }
    single { GetDueWordsUseCase(get()) }
    single { ReviewWordUseCase(get(), get()) }
    viewModel { StudyViewModel(get(), get()) }
}

// androidMain DI module
val androidModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single { DatabaseDriverFactory(get()) }
}

// iosMain DI module
val iosModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single { DatabaseDriverFactory() }
}

// iOS entry point — call from Swift: KoinHelperKt.doInitKoin()
@OptIn(KoinExperimentalAPI::class)
fun initKoin(): KoinApplication = startKoin {
    modules(commonModule, iosModule)
}
// Swift: KoinHelperKt.doInitKoin() in AppDelegate / @main App.init
```

---

## iOS Interop Tips

### Coroutines → Swift Async

Use `SKIE` (recommended) or `KMP-NativeCoroutines` to expose suspend functions as Swift async:

```kotlin
// With SKIE — zero boilerplate, check latest version at github.com/touchlab/SKIE
// gradle (libs.versions.toml): skie = "0.10.x"
// plugins { id("co.touchlab.skie") version libs.versions.skie.get() }
suspend fun getWords(): List<Word>   // → async func getWords() -> [Word] in Swift
fun observeWords(): Flow<List<Word>> // → AsyncStream<[Word]> in Swift
```

### @Throws — Surface Typed Errors to Swift

Without `@Throws`, any exception from a suspend function becomes an untyped `KotlinThrowable` in Swift, losing all type information:

```kotlin
// commonMain
@Throws(IOException::class, HttpException::class)
suspend fun fetchUser(id: String): User

// Swift — now catchable by type:
// do { let user = try await repo.fetchUser(id: "123") }
// catch let e as KotlinIOException { ... }
```

### Dispatchers.IO — Does Not Exist in commonMain/iOS

`Dispatchers.IO` is JVM-only. Bridge it with expect/actual:

```kotlin
// commonMain
expect val ioDispatcher: CoroutineDispatcher

// androidMain
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

// iosMain
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
```

### Avoid These in commonMain

- `java.*` imports — use `kotlinx.*` alternatives
- `Thread.sleep` — use `kotlinx.coroutines.delay`
- `System.currentTimeMillis()` — use `Clock.System.now()`
- `java.util.UUID` — use platform-specific `expect`/`actual` or `com.benasher44:uuid`
- `SimpleDateFormat` — use `kotlinx.datetime`
- `BuildConfig.DEBUG` — use `expect val isDebug: Boolean` (see Ktor section)
- `Dispatchers.IO` — JVM-only; use `expect val ioDispatcher` (see iOS Interop section)

---

## KMP-Compatible Library Checklist

| Need | Library |
|---|---|
| HTTP client | `io.ktor:ktor-client-core` |
| JSON | `org.jetbrains.kotlinx:kotlinx-serialization-json` |
| Dates | `org.jetbrains.kotlinx:kotlinx-datetime` |
| Database | `app.cash.sqldelight` |
| DI | `io.insert-koin:koin-core` + `io.insert-koin:koin-compose-viewmodel` |
| Settings | `com.russhwolf:multiplatform-settings` |
| Logging | `co.touchlab:kermit` |
| UUID | `com.benasher44:uuid` |
| Immutable collections | `org.jetbrains.kotlinx:kotlinx-collections-immutable` |
| Coroutines | `org.jetbrains.kotlinx:kotlinx-coroutines-core` |
| File I/O | `com.squareup.okio:okio` |
| Image loading | `io.coil-kt.coil3:coil-compose` (Coil 3.x — KMP native) |
| iOS coroutine bridge | `co.touchlab.skie:gradle-plugin` (SKIE) |
