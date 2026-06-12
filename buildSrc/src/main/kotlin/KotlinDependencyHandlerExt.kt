import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

//fun KotlinDependencyHandler.kapt(
//    dependency: String
//) = configurations["kapt"].dependencies.add(dependency)

//fun DependencyHandlerScope.kapt(
//    dependency: Provider<MinimalExternalModuleDependency>
//) {
//    configurations["kapt"].dependencies.add(dependency.get())
//}

//fun Project.kotlinSourceSets(
//    config: Action<NamedDomainObjectContainer<KotlinSourceSet>>
//) = with(kotlin) {
//    sourceSets(config)
//}

fun KotlinDependencyHandler.implementationPlatform(dependencyNotation: Any): Dependency? =
    implementation(project.dependencies.platform(dependencyNotation))
