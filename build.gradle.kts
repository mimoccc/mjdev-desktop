import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
//    alias(libs.plugins.devtools.ksp) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.app.icon.generator) apply false
    alias(libs.plugins.qodana)
//    alias(libs.plugins.kover)
    alias(libs.plugins.changelog) apply false
    alias(libs.plugins.gradle.ktlint)
    alias(libs.plugins.manes.versions)
    alias(libs.plugins.kotlin.android) apply false
    id("AiAgentPlugin")
//    id("com.github.gmazzo.buildconfig") version "5.6.5"
}

// ============================================================================
//  Code quality + dependency reporting  ->  /reports
//  - ben-manes: stable-only dependency-update report (rejects alpha/rc/dev/tethys/snapshot)
//  - ktlint:    auto-format after build + report; report-only (never breaks the build)
//  - After every build: auto-format code, ktlint report, and (gated) dependency report.
//    Dependency check is ON by default; disable with  -PdepCheck=false
// ============================================================================

// A version is "non-stable" if it is a milestone/preview build (alpha, beta, rc,
// eap, dev, snapshot, tethys, …) — we never auto-suggest those over a stable current.
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return !(stableKeyword || regex.matches(version))
}

val depCheckEnabled: Boolean = (findProperty("depCheck") as String?)?.toBooleanStrictOrNull() ?: true
val reportsDir: Directory = rootProject.layout.projectDirectory.dir("reports")

allprojects {
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<KtlintExtension> {
        ignoreFailures.set(true) // report only — never fail the build
        android.set(true)
        filter {
            // never lint/format generated or build output (compose resources, KSP, etc.)
            exclude { it.file.path.contains("${File.separator}build${File.separator}") }
            exclude { it.file.path.contains("generated") }
        }
        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.HTML)
            reporter(ReporterType.CHECKSTYLE)
        }
    }

    tasks.withType<DependencyUpdatesTask>().configureEach {
        rejectVersionIf { isNonStable(candidate.version) && !isNonStable(currentVersion) }
        outputDir = reportsDir.dir("dependencies").asFile.absolutePath
        reportfileName = "dependency-updates"
        outputFormatter = "plain,html"
    }
}

// Collect ktlint reports from every module into /reports/ktlint/<module>
val collectKtlintReports by tasks.registering(Copy::class) {
    group = "reporting"
    description = "Collects ktlint reports from all modules into /reports/ktlint"
    allprojects.forEach { p ->
        from(p.layout.buildDirectory.dir("reports/ktlint")) { into(p.name) }
    }
    into(reportsDir.dir("ktlint"))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    // ensure reports are produced before we collect them
    mustRunAfter(allprojects.flatMap { p -> p.tasks.matching { it.name == "ktlintCheck" || it.name == "ktlintFormat" } })
}

// Runs after every build: auto-format, ktlint report, and (gated) dependency-update report -> /reports
val postBuildCodeCheck by tasks.registering {
    group = "verification"
    description = "Auto-formats code (ktlintFormat) and runs the gated dependency-update report into /reports"
    // Auto-format on build. Safe because .editorconfig disables no-unused-imports — the one
    // rule that stripped receiver/extension-member imports and broke compilation.
    dependsOn(":shared:ktlintFormat")
    if (depCheckEnabled) dependsOn(":shared:dependencyUpdates")
    finalizedBy(collectKtlintReports)
}

allprojects {
    tasks.matching { it.name == "build" }.configureEach {
        finalizedBy(rootProject.tasks.named("postBuildCodeCheck"))
    }
}

//buildConfig {
//    generateAtSync = true
//    sourceSets  {
//        main {
//            packageName = "org.mjdev.desktop"
//            useKotlin = true
//            useJava = true
//            useGroovy = true
//            useScala = true
//            useXml = true
//            useProperties = true
//        }
//    }
//    buildConfigField("APP_NAME", project.name)
//    buildConfigField("APP_VERSION", provider { "\"${project.version}\"" })
//    buildConfigField("APP_SECRET", "Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu")
//    buildConfigField<String>("OPTIONAL", null)
//    buildConfigField("BUILD_TIME", System.currentTimeMillis())
//    buildConfigField("FEATURE_ENABLED", true)
//    buildConfigField("MAGIC_NUMBERS", intArrayOf(1, 2, 3, 4))
//    buildConfigField("STRING_LIST", arrayOf("a", "b", "c"))
//    buildConfigField("MAP", mapOf("a" to 1, "b" to 2))
//    buildConfigField("FILE", File("aFile"))
//    buildConfigField("URI", uri("https://example.io"))
//    buildConfigField("com.github.gmazzo.buildconfig.demos.kts.SomeData", "DATA", "SomeData(\"a\", 1)")
//}

//dependencies {
//    kover(project(":composeApp"))
//}

//kover { reports { total { xml { onCheck = true } } } }

// Aggregate task: build every distributable that this host can produce.
//  - desktop: Deb + AppImage + Rpm (per the targetFormats in composeApp), release mode
//  - android: release APK (assembleRelease)
//  - ios:     only when an iOS Kotlin target is configured AND the host is macOS
//             (Kotlin/Native for iOS cannot build on Linux/Windows) — wired below if present.
// captured at configuration time -> all the tasks below stay configuration-cache safe
val appNameV = libs.versions.app.name.get()
val versionV = libs.versions.app.pkg.version.get()
val pkgDirV = rootDir.resolve("packages/main-release")
val appImageToolPath = rootDir.resolve(".gradle/tools/appimagetool-x86_64.AppImage").absolutePath

// Downloads appimagetool before the build (only if not already present / on PATH).
val ensureAppImageTool = tasks.register<EnsureAppImageToolTask>("ensureAppImageTool") {
    group = "mjdev"
    description = "Downloads appimagetool (single-file .AppImage builder) if not already present."
    toolPath.set(appImageToolPath)
}

// Wraps the jpackage app-image directory into a single, CLI-runnable .AppImage file.
val packageAppImageFile = tasks.register<PackageAppImageTask>("packageAppImageFile") {
    group = "mjdev"
    description = "Builds a single-file .AppImage from the jpackage app-image (skips if appimagetool unavailable)."
    dependsOn(ensureAppImageTool, ":desktopApp:packageReleaseAppImage")
    appName.set(appNameV)
    appImagePath.set(pkgDirV.resolve("app/$appNameV").absolutePath)
    outputPath.set(pkgDirV.resolve("appimage/$appNameV.AppImage").absolutePath)
    toolPath.set(appImageToolPath)
}

// Copies all distributables into releases/ with version in the filename
// (mjdev-desktop-<version>.<ext>). Copy = configuration-cache safe.
val collectReleases = tasks.register<Copy>("collectReleases") {
    group = "mjdev"
    description = "Copies the built distributables into releases/ (versioned filenames)."
    dependsOn(
        ":desktopApp:packageReleaseDistributionForCurrentOS",
        ":androidApp:assembleRelease",
        packageAppImageFile,
    )
    // local copies so the rename closures capture plain Strings (configuration-cache safe)
    val base = "$appNameV-$versionV"
    into(layout.projectDirectory.dir("releases"))
    from(pkgDirV.resolve("deb")) { include("*.deb"); rename { "$base.deb" } }
    from(pkgDirV.resolve("rpm")) { include("*.rpm"); rename { "$base.rpm" } }
    from(pkgDirV.resolve("exe")) { include("*.exe"); rename { "$base.exe" } } // present only on a Windows host
    from(pkgDirV.resolve("appimage")) { include("*.AppImage"); rename { "$base.AppImage" } }
    from(rootDir.resolve("androidApp/build/outputs/apk/release")) {
        include("*.apk"); rename { "$base.apk" }
    }
}

val buildAll = tasks.register("buildAll") {
    group = "mjdev"
    description = "Builds all distributables this host can produce, collects them into releases/ (stable names), and generates reports into reports/ — like every build."
    dependsOn(collectReleases, postBuildCodeCheck)
}

// Attach iOS framework build only when an iOS target actually exists in composeApp
// (it never does on a Linux/Windows host — Kotlin/Native iOS requires macOS + Xcode).
project(":shared").afterEvaluate {
    val iosTask = tasks.names.firstOrNull {
        it.startsWith("linkReleaseFrameworkIos") || it == "embedAndSignAppleFrameworkForXcode"
    }
    if (iosTask != null) {
        buildAll.configure { dependsOn("${this@afterEvaluate.path}:$iosTask") }
        logger.lifecycle("buildAll: iOS target found — wiring ${this@afterEvaluate.path}:$iosTask")
    } else {
        logger.info("buildAll: no iOS target configured (needs macOS + Xcode + an ios* target in composeApp) — iOS skipped")
    }
}
