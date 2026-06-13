package ai

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * Registers the `runAiAgent` task, which reads ai-todo.txt and performs the tasks
 * it can do safely (see [RunAiAgentTask]). Apply on the root project:
 *
 * ```
 * plugins { id("AiAgentPlugin") }
 * ```
 */
class AiAgentPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val root = project.rootDir

        // Make sure the agent's working files exist (never overwrites real content).
        listOf("ai-todo.txt", "ai-credentials.txt", "ai-suggestions.txt", "ai-done.txt").forEach {
            File(root, it).takeUnless(File::exists)?.createNewFile()
        }

        project.tasks.register<RunAiAgentTask>("runAiAgent") {
            group = "mjdev"
            description = "Reads ai-todo.txt and performs its tasks (pluggable AI: ollama / claude-code / mistral)."
            rootDir = root
        }
    }
}
