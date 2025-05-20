import org.gradle.api.Project

class MultiPlatformPlugin : BasePlugin() {
    override fun Project.onConfigure() {
//        buildscript.repositories {
//        }
    }

    override fun Project.registerTasks() {
        registerTask<DeployTask>("deploy")
        registerTask<GenerateChangelogTask>("changelog")
    }

    override fun Project.onBeforeEvaluate() {
    }

    override fun Project.onAfterEvaluate() {
    }
}
