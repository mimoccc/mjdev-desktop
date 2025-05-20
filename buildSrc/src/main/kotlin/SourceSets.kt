//import com.android.build.gradle.BaseExtension
//import org.gradle.api.Project
//import org.gradle.kotlin.dsl.NamedDomainObjectCollectionDelegateProvider
//import org.gradle.kotlin.dsl.get
//import org.gradle.kotlin.dsl.getByType
//import org.gradle.kotlin.dsl.getting
//import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
//import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformSourceSetConventions
//import co.touchlab.kotlinxcodesync.SyncExtension
//import com.android.build.gradle.BaseExtension
//import org.gradle.api.Task
//import org.gradle.api.plugins.JavaBasePlugin
//import org.gradle.kotlin.dsl.creating
//import org.gradle.kotlin.dsl.getByType
//import org.gradle.kotlin.dsl.getValue
//import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
//import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
//import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
//import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType


//    val Project.libs
//        get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

//    val Project.androidLibs
//        get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("androidLibs")

//fun KotlinMultiplatformExtension.sourceSets(
//    block: NamedDomainObjectContainer<KotlinSourceSet>.() -> Unit
//) {
//    sourceSets.block()
//}

//private fun NamedDomainObjectContainer<KotlinSourceSet>.getOrCreate(
//    name: String
//): KotlinSourceSet = findByName(name) ?: create(name)

// common
//fun NamedDomainObjectContainer<KotlinSourceSet>.commonMain(block: KotlinSourceSet.() -> Unit = {}): KotlinSourceSet =
//    getOrCreate("commonMain").apply(block)

// common test
//fun NamedDomainObjectContainer<KotlinSourceSet>.commonTest(block: KotlinSourceSet.() -> Unit = {}): KotlinSourceSet =
//    getOrCreate("commonTest").apply(block)

// android
//fun NamedDomainObjectContainer<KotlinSourceSet>.androidMain(block: KotlinSourceSet.() -> Unit = {}): KotlinSourceSet =
//    getOrCreate("androidMain").apply(block)

// android test
//fun NamedDomainObjectContainer<KotlinSourceSet>.androidTest(block: KotlinSourceSet.() -> Unit = {}): KotlinSourceSet =
//    getOrCreate("androidTest").apply(block)

// desktop

//fun NamedDomainObjectContainer<KotlinSourceSet>.desktopMain(block: KotlinSourceSet.() -> Unit = {}): KotlinSourceSet =
//    getOrCreate("androidMain").apply(block)

//fun NamedDomainObjectContainer<KotlinSourceSet>.desktopTest(block: KotlinSourceSet.() -> Unit = {}): KotlinSourceSet =
//    getOrCreate("androidTest").apply(block)

//    fun Project.android(block: BaseExtension.() -> Unit) {
//        extensions.getByType<BaseExtension>().apply (block)
//    }

//    fun Project.kotlin(block: KotlinMultiplatformExtension.() -> Unit) {
//        extensions.getByType<KotlinMultiplatformExtension>().apply(block)
//    }

//    val implementation by configurations
//    val compileOnly by configurations
//    val annotationProcessor by configurations
//    val NamedDomainObjectContainer<KotlinSourceSet>.desktopMain by getting

//    fun KotlinMultiplatformSourceSetConventions.desktopMain(
//        block: KotlinSourceSet.() -> Unit
//    ) {
//        val desktopMain by getting
//        block(desktopMain)
//    }

//    fun Project.setupMultiplatform() {
//        plugins.apply("kotlin-multiplatform")
//        plugins.apply("com.android.library")
//        setupAndroidSdkVersions()
//        kotlin {
//            android {
//                publishLibraryVariants("release", "debug")
//            }
//            iosX64()
//            iosArm64()
//            sourceSets {
//                commonMain {
//                    dependencies {
//                        implementation(Deps.Jetbrains.Kotlin.StdLib.Common)
//                    }
//                }
//                commonTest {
//                    dependencies {
//                        implementation(Deps.Jetbrains.Kotlin.Test.Common)
//                        implementation(Deps.Jetbrains.Kotlin.TestAnnotations.Common)
//                    }
//                }
//                androidMain {
//                    dependsOn(commonMain())
//                    dependencies {
//                        implementation(Deps.Jetbrains.Kotlin.StdLib.Jdk7)
//                    }
//                }
//                androidTest {
//                    dependsOn(commonTest())
//                    dependencies {
//                        implementation(Deps.Jetbrains.Kotlin.Test.Junit)
//                    }
//                }
//                iosCommonMain().dependsOn(commonMain())
//                iosCommonTest().dependsOn(commonTest())
//                iosX64Main().dependsOn(iosCommonMain())
//                iosX64Test().dependsOn(iosCommonTest())
//                iosArm64Main().dependsOn(iosCommonMain())
//                iosArm64Test().dependsOn(iosCommonTest())
//            }
//        }
//    }

//    fun Project.setupAndroidSdkVersions() {
//        android {
//            compileSdkVersion(29)
//            defaultConfig {
//                targetSdkVersion(29)
//                minSdkVersion(21)
//            }
//        }
//    }

// Workaround since iosX64() and iosArm64() function are not resolved
// if used in a module with Kotlin 1.3.70
//    fun Project.setupKittensBinaries() {
//        fun KotlinNativeTarget.setupIosBinaries() {
//            binaries {
//                framework {
//                    baseName = "Kittens"
//                    freeCompilerArgs = freeCompilerArgs.plus("-Xobjc-generics").toMutableList()
//
//                    export(project(":shared:mvi"))
//                }
//            }
//        }
//        kotlin {
//            iosX64().setupIosBinaries()
//            iosArm64().setupIosBinaries()
//        }
//    }

//    fun Project.setupXcodeSync() {
//        plugins.apply("co.touchlab.kotlinxcodesync")
//        extensions.getByType<SyncExtension>().run {
//            projectPath = "../todo-app-ios/todo-app-ios.xcodeproj"
//            target = "todo-app-ios"
//        }
//    }
