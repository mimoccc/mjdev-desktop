import org.gradle.api.NamedDomainObjectContainer
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

fun NamedDomainObjectContainer<KotlinSourceSet>.getOrCreate(
    name: String
): KotlinSourceSet = findByName(name) ?: create(name)

fun NamedDomainObjectContainer<KotlinSourceSet>.desktopMain(
    block: KotlinSourceSet.() -> Unit = {}
): KotlinSourceSet = getOrCreate("desktopMain").apply(block)

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

