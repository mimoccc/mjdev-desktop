import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.getByType

//enum class OS { WINDOWS, LINUX, MAC, OTHER }

//fun getCurrentOs(): OS {
//    val osName = System
//        .getProperty("os.name")
//        .lowercase()
//        .trim()
//    return when {
//        osName.startsWith("win") -> OS.WINDOWS
//        osName.startsWith("mac") -> OS.MAC
//        osName == "linux" -> OS.LINUX
//        else -> OS.OTHER
//    }
//}

val Project.java: JavaPluginExtension
    get() = extensions.getByType()

fun Project.setJavaLanguageVersion(
    version: Provider<String>
): JavaLanguageVersion = version.get().let { vs ->
    JavaLanguageVersion.of(vs)
}.also { jlv ->
    java.toolchain.languageVersion.set(jlv)
}

//fun NamedDomainObjectContainer<KotlinSourceSet>.getOrCreate(
//    name: String
//): KotlinSourceSet = findByName(name) ?: create(name)

//fun Project.kotlinSourceSets(
//    config: Action<NamedDomainObjectContainer<KotlinSourceSet>>
//) = kotlin.sourceSets.configure(config)

//fun NamedDomainObjectContainer<KotlinSourceSet>.getOrCreate(
//    name: String
//): KotlinSourceSet = findByName(name) ?: create(name)

//fun NamedDomainObjectContainer<KotlinSourceSet>.desktopMain(
//    block: KotlinSourceSet.() -> Unit = {}
//): KotlinSourceSet = getOrCreate("desktopMain").apply(block)

// todo
//@Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")
//fun Project.wasm(config: DesktopExtension.() -> Unit) {
//    // todo
////    compose.wasmJs(config)
//}

// todo
//fun Project.kotlinSourceSets(
//    config: Action<NamedDomainObjectContainer<KotlinSourceSet>>
//) = with(kotlin) {
//    sourceSets(config)
//}
