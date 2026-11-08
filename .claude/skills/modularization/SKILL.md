---
name: modularization
description: Modularize Android/KMP projects — feature-based module graph, build-logic convention plugins, module boundary rules, build time optimization, and Gradle multi-module setup for KMP targets
argument-hint: "<module to create or feature to extract>"
user-invocable: true
allowed-tools: ["Read", "Write", "Edit", "Glob", "Grep", "Bash"]
---

# Modularization — Android / KMP

## Why Modularize

- **Build speed**: only changed modules recompile
- **Team scale**: teams own modules, parallel development without conflicts
- **Enforced boundaries**: compiler prevents illegal cross-layer access
- **Reuse**: `:core:design-system` shared across `:feature:study` and `:feature:words`
- **Testability**: modules tested in isolation with fakes

---

## Target Module Graph

```
:app
 ├── :feature:auth
 ├── :feature:study
 ├── :feature:words
 ├── :feature:profile
 └── :feature:import
        ↓ (all features depend on)
 :domain
 :core:common
 :core:network
 :core:database
 :core:design-system
 :core:testing      (testImplementation only)
 :platforms         (expect/actual for platform-specific APIs)
 :resources         (shared strings, assets, MR)
```

### Dependency Direction Rules (STRICT)

- `:feature:*` → `:domain`, `:core:*`, `:resources` ✅
- `:feature:*` → another `:feature:*` ✗
- `:domain` → nothing (pure Kotlin) ✅
- `:core:design-system` → `:domain`, `:core:network`, `:core:database` ✗
- `:app` → all modules (wires DI, navigation) ✅

---

## settings.gradle.kts (root)

The `pluginManagement` block **must** come first so `build-logic` convention plugins resolve before any `include()`.

```kotlin
// settings.gradle.kts (root)
pluginManagement {
    includeBuild("build-logic")          // composite build — makes kmp.* plugins available
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MyApp"

// App
include(":app")

// Feature modules
include(":feature:auth")
include(":feature:study")
include(":feature:words")
include(":feature:profile")
include(":feature:import")

// Domain
include(":domain")

// Core
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:design-system")
include(":core:testing")

// Platform
include(":platforms")
include(":resources")
```

---

## gradle/libs.versions.toml — plugin alias entries

Convention plugin IDs must appear in the version catalog so modules can reference them via `alias(libs.plugins.*)`.

```toml
[plugins]
# Convention plugins (version = "unspecified" because they come from the composite build)
kmp-library  = { id = "kmp.library",  version = "unspecified" }
kmp-feature  = { id = "kmp.feature",  version = "unspecified" }
kmp-compose  = { id = "kmp.compose",  version = "unspecified" }
android-library = { id = "android.library", version = "unspecified" }

# External plugins
kotlin-multiplatform    = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
android-application     = { id = "com.android.application",           version.ref = "agp" }
compose-multiplatform   = { id = "org.jetbrains.compose",             version.ref = "compose-multiplatform" }
kotlin-serialization    = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
sqldelight              = { id = "app.cash.sqldelight",                version.ref = "sqldelight" }
dependency-guard        = { id = "com.dropbox.dependency-guard",       version.ref = "dependency-guard" }
```

---

## build-logic Convention Plugins

Eliminates boilerplate from every module's `build.gradle.kts`.

### Plugin Directory Structure

```
build-logic/
├── settings.gradle.kts
└── convention/
    ├── build.gradle.kts
    └── src/main/kotlin/
        ├── KmpLibraryConventionPlugin.kt
        ├── KmpFeatureConventionPlugin.kt
        ├── AndroidLibraryConventionPlugin.kt
        └── ComposeConventionPlugin.kt
```

### build-logic/settings.gradle.kts

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") { from(files("../gradle/libs.versions.toml")) }
    }
}
rootProject.name = "build-logic"
include(":convention")
```

### build-logic/convention/build.gradle.kts

```kotlin
plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpFeature") {
            id = "kmp.feature"
            implementationClass = "KmpFeatureConventionPlugin"
        }
        register("androidLibrary") {
            id = "android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "kmp.compose"
            implementationClass = "ComposeConventionPlugin"
        }
    }
}
```

### KmpLibraryConventionPlugin — shared KMP module setup

Inside `Plugin<Project>`, the `kotlin {}` / `android {}` DSL shortcuts are not in scope.
Use `extensions.configure<>` to access each extension safely.
`applyDefaultHierarchyTemplate()` is required so that the `iosMain` intermediary source set
exists when modules reference it (e.g. `iosMain.dependencies { }`).

```kotlin
// build-logic/convention/src/main/kotlin/KmpLibraryConventionPlugin.kt
import com.android.build.gradle.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("com.android.library")

            extensions.configure<KotlinMultiplatformExtension> {
                applyDefaultHierarchyTemplate()   // creates iosMain, nativeMain, etc.

                androidTarget()
                iosArm64()
                iosSimulatorArm64()

                // Single toolchain call replaces both compileOptions and kotlinOptions.jvmTarget
                jvmToolchain(17)

                sourceSets {
                    commonMain.dependencies {
                        implementation(libs.findLibrary("koin-core").get())
                        implementation(libs.findLibrary("coroutines-core").get())
                    }
                    androidMain.dependencies {
                        implementation(libs.findLibrary("koin-android").get())
                    }
                }
            }

            extensions.configure<LibraryExtension> {
                compileSdk = 36
                // namespace must be set per-module; derive it from the project path
                namespace = "com.example${target.path.replace(':', '.').replace('-', '_')}"
                defaultConfig { minSdk = 24 }
            }
        }
    }
}
```

### KmpFeatureConventionPlugin — feature module with Compose

```kotlin
// build-logic/convention/src/main/kotlin/KmpFeatureConventionPlugin.kt
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("kmp.library")    // applies KmpLibraryConventionPlugin
            pluginManager.apply("kmp.compose")    // applies ComposeConventionPlugin

            // kmp.library has already applied the KMP plugin; configure the extension directly
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets {
                    commonMain.dependencies {
                        implementation(project(":domain"))
                        implementation(project(":core:common"))
                        implementation(project(":core:design-system"))
                        implementation(project(":resources"))
                    }
                    commonTest.dependencies {
                        implementation(project(":core:testing"))
                    }
                }
            }
        }
    }
}
```

---

## App Module

`:app` wires navigation, DI, and the Android entry point. It is the only module allowed to depend on all other modules.

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kmp.compose)
}

kotlin {
    androidTarget()

    sourceSets {
        androidMain.dependencies {
            implementation(project(":feature:auth"))
            implementation(project(":feature:study"))
            implementation(project(":feature:words"))
            implementation(project(":feature:profile"))
            implementation(project(":feature:import"))
            implementation(project(":domain"))
            implementation(project(":core:common"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(project(":core:design-system"))
            implementation(project(":resources"))
            implementation(project(":platforms"))
        }
    }
}

android {
    namespace = "com.example.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}
```

---

## Feature Module build.gradle.kts (using convention)

```kotlin
// feature/study/build.gradle.kts
plugins {
    alias(libs.plugins.kmp.feature)   // applies KmpFeatureConventionPlugin — everything configured
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Only module-specific deps beyond the convention defaults
            implementation(libs.some.extra.library)
        }
    }
}
```

---

## Domain Module (Pure Kotlin — No Android)

```kotlin
// domain/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    // NO android plugin — domain is pure Kotlin
}

kotlin {
    jvm()       // for unit tests on JVM
    iosArm64()
    iosSimulatorArm64()

    jvmToolchain(17)

    sourceSets {
        commonMain.dependencies {
            implementation(libs.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }
    }
}
```

---

## Core Modules

### core:common

```kotlin
// core/common/build.gradle.kts
plugins { alias(libs.plugins.kmp.library) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.arrow.core)          // Try<T> — api so consumers get it
            api(libs.coroutines.core)
        }
    }
}
```

### core:network

`iosMain` is available because `KmpLibraryConventionPlugin` calls `applyDefaultHierarchyTemplate()`.

```kotlin
// core/network/build.gradle.kts
plugins { alias(libs.plugins.kmp.library) }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {                // exists because of applyDefaultHierarchyTemplate()
            implementation(libs.ktor.client.darwin)
        }
    }
}
```

### core:database

```kotlin
// core/database/build.gradle.kts
plugins {
    alias(libs.plugins.kmp.library)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.db")
            verifyMigrations.set(true)
        }
    }
}
```

### core:design-system

Depends **only** on Compose Multiplatform. No `:domain`, no network, no database.

```kotlin
// core/design-system/build.gradle.kts
plugins {
    alias(libs.plugins.kmp.library)
    alias(libs.plugins.kmp.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Compose multiplatform UI primitives only
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
    }
}
```

### core:testing

```kotlin
// core/testing/build.gradle.kts
plugins { alias(libs.plugins.kmp.library) }

kotlin {
    sourceSets {
        commonMain.dependencies {   // test helpers exposed as main (only used in test source sets)
            implementation(project(":domain"))
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
            implementation(libs.turbine)
        }
    }
}
```

### platforms

Houses `expect`/`actual` declarations for APIs that differ per platform (e.g., file system, UUID, clock).

```
platforms/
└── src/
    ├── commonMain/kotlin/platforms/
    │   ├── FileSystem.kt       # expect fun readFile(path: String): String
    │   └── Uuid.kt             # expect fun randomUuid(): String
    ├── androidMain/kotlin/platforms/
    │   ├── FileSystem.android.kt
    │   └── Uuid.android.kt
    └── iosMain/kotlin/platforms/
        ├── FileSystem.ios.kt
        └── Uuid.ios.kt
```

```kotlin
// platforms/build.gradle.kts
plugins { alias(libs.plugins.kmp.library) }
// No extra dependencies — expect/actual only
```

### resources

Centralises shared string resources using Moko Resources (or another MR library).

```kotlin
// resources/build.gradle.kts
plugins {
    alias(libs.plugins.kmp.library)
    alias(libs.plugins.moko.resources)   // or multiplatform-resources
}

multiplatformResources {
    resourcesPackage.set("com.example.resources")
}
```

---

## Feature Module Structure

Each `:feature:X` follows identical internal layout:

```
feature/study/
└── src/
    └── commonMain/kotlin/feature/study/
        ├── di/
        │   └── StudyModule.kt          # Koin module
        ├── domain/                     # Feature-specific use cases (if not in :domain)
        │   └── GetDueWordsUseCase.kt
        ├── presentation/
        │   ├── StudyScreen.kt
        │   ├── StudyViewModel.kt
        │   └── StudyState.kt
        └── navigation/
            └── StudyNavigation.kt      # Route definitions + composable extensions
```

### Feature Navigation (NavGraph extension)

```kotlin
// feature/study/src/commonMain/kotlin/feature/study/navigation/StudyNavigation.kt
@Serializable object StudyRoute

fun NavGraphBuilder.studyGraph(onNavigateToWord: (Int) -> Unit) {
    composable<StudyRoute> {
        StudyScreen(onNavigateToWord = onNavigateToWord)
    }
}
```

Wire in `:app`:

```kotlin
// app/src/commonMain/kotlin/navigation/AppNavGraph.kt
NavHost(navController, startDestination = StudyRoute) {
    studyGraph(onNavigateToWord = { id -> navController.navigate(WordDetailRoute(id)) })
    wordsGraph(onNavigateToStudy = { navController.navigate(StudyRoute) })
    authGraph(onAuthSuccess = { navController.navigate(StudyRoute) { popUpTo(0) } })
}
```

### Feature DI Module

```kotlin
// feature/study/src/commonMain/kotlin/feature/study/di/StudyModule.kt
val studyModule = module {
    viewModel { StudyViewModel(get(), get()) }
    factory { GetDueWordsUseCase(get()) }
}
```

Registered in `:app`:

```kotlin
// app/src/commonMain/kotlin/di/AppModule.kt
startKoin {
    modules(
        domainModule,
        networkModule,
        databaseModule,
        studyModule,
        wordsModule,
        authModule,
    )
}
```

---

## Enforcing Module Boundaries

### Dependency Guard (build-time enforcement)

Check both release and debug classpaths to catch all illegal dependencies.

```kotlin
// build.gradle.kts (root)
plugins {
    alias(libs.plugins.dependency.guard)
}

dependencyGuard {
    configuration("releaseRuntimeClasspath")
    configuration("debugRuntimeClasspath")
}
```

Run `./gradlew dependencyGuard` to generate baselines, then `./gradlew dependencyGuardBaseline` to update them after intentional changes.

### Detekt Module Rule

```yaml
# detekt.yml
complexity:
  ForbiddenImport:
    active: true
    imports:
      - value: 'androidx.room.*'
        reason: 'Use domain repository interfaces, not Room directly in feature modules'
      - value: 'io.ktor.*'
        reason: 'Network access only via data layer interfaces'
```

### Validate via Gradle

```bash
# Check no illegal cross-feature dependency
./gradlew :feature:study:dependencies | grep ':feature:'
# Should only see :feature:study itself, never another :feature:*

# Check domain has no Android imports
./gradlew :domain:dependencies | grep 'androidx'
# Should return nothing
```

---

## Build Speed — Module Caching

```properties
# gradle.properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
kotlin.incremental.multiplatform=true

# Each module produces its own build cache entry.
# A change in :feature:study does NOT recompile :feature:words.
```

---

## Common Mistakes / Gotchas

| Mistake | Fix |
|---|---|
| `pluginManagement { includeBuild("build-logic") }` missing | Must be the **first block** in root `settings.gradle.kts` before any `include()` |
| `kotlin { }` / `android { }` used directly in `Plugin<Project>` | Use `extensions.configure<KotlinMultiplatformExtension>` / `extensions.configure<LibraryExtension>` |
| `iosMain.dependencies { }` fails to resolve | Call `applyDefaultHierarchyTemplate()` in the convention plugin to create the intermediary source set |
| Missing `namespace` in `LibraryExtension` | AGP 7.3+ requires `namespace`; set it per module, not in the convention plugin |
| `compileOptions { sourceCompatibility = JavaVersion.VERSION_17 }` | Replace with `jvmToolchain(17)` — single call covers both Java and Kotlin toolchains |
| Version catalog missing plugin aliases | Add `kmp-library`, `kmp-feature`, `kmp-compose` entries with `version = "unspecified"` |
| `:core:design-system` importing `:domain` | Design system must depend only on Compose — no business types leak into UI primitives |
| `:core:testing` on production classpath | Only ever use via `commonTest.dependencies { }` or `testImplementation` |

---

## Migrating a Monolith to Modules — Phased Plan

**Phase 1 — Extract core (no feature changes)**
1. Create `:core:common` with `Try<T>`, `BaseViewModel`, shared utilities
2. Create `:core:network` with Ktor client
3. Create `:core:database` with SQLDelight schema
4. Update `:app` to depend on these

**Phase 2 — Extract domain**
1. Move all models, use case interfaces, repository interfaces to `:domain`
2. Keep implementations in `:app` temporarily

**Phase 3 — Extract features one at a time**
1. Start with the most isolated feature (fewest cross-feature dependencies)
2. Move presentation + feature DI module
3. Move feature-specific use cases
4. Wire navigation extension into `:app`

**Phase 4 — Clean up `:app`**
- `:app` should contain only: `MainActivity`, `KoinApplication`, `AppNavGraph`, manifest

---

## Module Checklist

- [ ] `pluginManagement { includeBuild("build-logic") }` is the first block in root `settings.gradle.kts`
- [ ] Convention plugin uses `extensions.configure<KotlinMultiplatformExtension>` — not bare `kotlin { }`
- [ ] `applyDefaultHierarchyTemplate()` called in `KmpLibraryConventionPlugin`
- [ ] `namespace` set for every Android module (AGP 8.x requirement)
- [ ] `jvmToolchain(17)` used — not `compileOptions`
- [ ] Version catalog has `kmp-library`, `kmp-feature`, `kmp-compose` plugin aliases
- [ ] Convention plugin used — no boilerplate repeated in `build.gradle.kts`
- [ ] `:domain` has zero Android/framework imports
- [ ] `:feature:X` depends on `:domain` and `:core:*`, never on another `:feature:*`
- [ ] `:core:design-system` depends on nothing except Compose multiplatform
- [ ] `:platforms` contains only `expect`/`actual` declarations
- [ ] Each feature has its own Koin module registered in `:app`
- [ ] Navigation extension function in each feature, wired in `:app`
- [ ] `:core:testing` only appears as `commonTest.dependencies { }` — never production classpath
- [ ] `dependencyGuard` checks both `releaseRuntimeClasspath` and `debugRuntimeClasspath`
- [ ] `org.gradle.parallel=true` and `org.gradle.caching=true` in `gradle.properties`
- [ ] Build cache verified: changing `:feature:study` does not invalidate `:feature:words` cache