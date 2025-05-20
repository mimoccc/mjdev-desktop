import org.gradle.api.Project
import org.gradle.api.Task

const val GROUP = "mjdev"

inline fun <reified T : Task> Project.registerTask(
    name: String,
    group: String = GROUP
) = tasks.register(name, T::class.java) {
    this.group = group
}
