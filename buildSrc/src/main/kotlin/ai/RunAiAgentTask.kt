package ai

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Categories of action the agent can take. The security policy in [SecurityPolicy]
 * decides which are auto-approved.
 */
enum class OpKind {
    DIR_CREATE, FILE_WRITE, FILE_READ, EXEC, LLM_CALL,
    GIT_WRITE, GIT_CREATE, GIT_COMMIT, GIT_PUSH,
    UPLOAD_TO_INTERNET, OTHER,
}

/**
 * Autonomy policy from ai-todo.txt: every operation is approved **except**
 * git write/create/commit/push and transferring project files to the internet.
 */
object SecurityPolicy {
    private val denied = setOf(
        OpKind.GIT_WRITE, OpKind.GIT_CREATE, OpKind.GIT_COMMIT, OpKind.GIT_PUSH,
        OpKind.UPLOAD_TO_INTERNET,
    )
    fun isApproved(op: OpKind): Boolean = op !in denied
    fun describe(op: OpKind): String = if (isApproved(op)) "approved" else "BLOCKED (requires human approval)"
}

/** One parsed entry from ai-todo.txt. */
private data class Block(val kind: Kind, val raw: String) {
    enum class Kind { COMMENT, BLANK, REPEATABLE, ONCE }
    /** Task text with the leading marker stripped and lines joined. */
    val text: String get() = raw.lineSequence()
        .joinToString(" ") { it.trimStart('*', '-', ' ', '\t') }
        .trim()
}

/**
 * Reads ai-todo.txt and performs the tasks it can do safely and deterministically,
 * recording outcomes to ai-done.txt / ai-suggestions.txt and removing finished
 * one-shot tasks from ai-todo.txt. Anything it cannot do deterministically is sent
 * to a pluggable AI provider ([AiProviders.select]); the model's plan is written to
 * ai-suggestions.txt for review rather than blindly applied.
 */
abstract class RunAiAgentTask : DefaultTask() {

    @get:Internal lateinit var rootDir: File

    private val todoFile get() = File(rootDir, "ai-todo.txt")
    private val credsFile get() = File(rootDir, "ai-credentials.txt")
    private val doneFile get() = File(rootDir, "ai-done.txt")
    private val suggestionsFile get() = File(rootDir, "ai-suggestions.txt")
    private val ts get() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    @TaskAction
    fun run() {
        if (!todoFile.exists()) { logger.lifecycle("[ai-agent] no ai-todo.txt — nothing to do"); return }
        val blocks = parse(todoFile.readText())
        val creds = readCreds()
        val provider by lazy { AiProviders.select(creds) { logger.lifecycle("[ai-agent] $it") } }

        val completed = mutableListOf<Block>()
        for (b in blocks) {
            when (b.kind) {
                Block.Kind.REPEATABLE -> handleRepeatable(b.text)
                Block.Kind.ONCE -> if (handleOnce(b.text) { provider }) completed += b
                else -> { /* comments / blanks preserved as-is */ }
            }
        }

        if (completed.isNotEmpty()) {
            rewriteTodo(blocks, completed)
            logger.lifecycle("[ai-agent] completed ${completed.size} one-shot task(s); ai-todo.txt updated")
        } else {
            logger.lifecycle("[ai-agent] no one-shot tasks completed this run")
        }
    }

    // ---- repeatable (every-build) tasks --------------------------------------------------------

    private fun handleRepeatable(text: String) {
        val t = text.lowercase()
        when {
            "update dependencies" in t || ("update" in t && "report in reports" in t) -> {
                val enabled = (project.findProperty("updateDeps") as String?)?.toBoolean() == true
                if (!enabled) {
                    logger.lifecycle("[ai-agent] repeatable: dependency auto-update is available but skipped — run `./gradlew runAiAgent -PupdateDeps=true` to apply non-invasive stable updates (build-verified, reverts on failure).")
                    return
                }
                val verifyTask = (project.findProperty("verifyTask") as String?) ?: ":composeApp:compileKotlinDesktop"
                DependencyUpdater(rootDir, verifyTask, logger::lifecycle, ::suggest).run()
            }
            "collect reports" in t || "clean up code" in t ->
                logger.lifecycle("[ai-agent] repeatable: '${text.lineSequence().first()}' -> handled by the build pipeline task `postBuildCodeCheck` (ktlintFormat + collectKtlintReports + dependency report into /reports)")
            else ->
                logger.lifecycle("[ai-agent] repeatable task not auto-mapped: '${text.lineSequence().first()}'")
        }
    }

    // ---- one-shot tasks: returns true when finished (and should be removed) --------------------

    private fun handleOnce(text: String, provider: () -> AiProvider?): Boolean {
        val t = text.lowercase()
        return when {
            // create folder <name> ... root project folder
            Regex("add folder\\s+([\\w.-]+)").find(t) != null && "root project" in t -> {
                val name = Regex("add folder\\s+([\\w.-]+)").find(t)!!.groupValues[1]
                createFolder(name)
            }
            // create the plugin itself
            "create plugin" in t && "buildsrc" in t -> done(
                "Create the AI agent buildSrc plugin",
                "Implemented in buildSrc/src/main/kotlin/ai/ (AiAgentPlugin, RunAiAgentTask, AiProvider). " +
                    "Providers: ollama, claude-code, mistral. Reads ai-todo.txt; writes ai-done.txt / ai-suggestions.txt; " +
                    "credentials from ai-credentials.txt. Task `runAiAgent` runs the file.",
                File(rootDir, "buildSrc/src/main/kotlin/ai/AiAgentPlugin.kt").exists(),
            )
            // attach plugin to project
            "attach plugin" in t -> done(
                "Attach the AI agent plugin to the project",
                "Plugin `AiAgentPlugin` applied on the root project; `runAiAgent` task is registered.",
                project.rootProject.tasks.findByName("runAiAgent") != null,
            )
            // provider/credentials policy directive
            "ollama serve" in t || ("credentials" in t && "cloud" in t) -> done(
                "Provider selection policy (cloud-first, ollama fallback)",
                "Implemented in AiProviders.select(): prefers cloud (claude-code, then mistral); " +
                    "falls back to local ollama and runs `ollama serve` on demand when no cloud credentials are available.",
                true,
            )
            // security/autonomy policy directive
            "approved all the operations" in t || ("git actions" in t && "security" in t) -> done(
                "Autonomy / security policy",
                "Implemented in SecurityPolicy: all operations auto-approved except git write/create/commit/push " +
                    "and project->internet file transfers, which stay gated for human approval.",
                true,
            )
            else -> delegateToAi(text, provider())
        }
    }

    private fun createFolder(name: String): Boolean {
        if (!SecurityPolicy.isApproved(OpKind.DIR_CREATE)) { suggest("create folder '$name' is ${SecurityPolicy.describe(OpKind.DIR_CREATE)}"); return false }
        val dir = File(rootDir, name)
        val created = if (dir.exists()) false else dir.mkdirs()
        return done(
            "Ensure folder '$name' exists at project root",
            if (created) "Created $name/." else "$name/ already existed — no change.",
            dir.isDirectory,
        )
    }

    /** Send an un-handled task to the AI provider and record its plan for review (never auto-applies). */
    private fun delegateToAi(text: String, provider: AiProvider?): Boolean {
        if (!SecurityPolicy.isApproved(OpKind.LLM_CALL)) { suggest("LLM call for '$text' is ${SecurityPolicy.describe(OpKind.LLM_CALL)}"); return false }
        if (provider == null) {
            suggest("No AI provider available for task: \"$text\". Add MISTRAL_API_KEY to ai-credentials.txt, install the `claude` CLI, or run `ollama serve`.")
            return false
        }
        logger.lifecycle("[ai-agent] delegating to ${provider.name}: \"$text\"")
        val plan = runCatching {
            provider.complete(
                "You are a build agent for a Kotlin Multiplatform / Compose Multiplatform Wayland desktop project. " +
                    "Propose concrete steps to accomplish this task. Do not perform git write/commit/push or upload project " +
                    "files to the internet. Task:\n\n$text",
            )
        }.getOrElse { "provider error: ${it.message}" }
        suggest("Task: \"$text\"\nProvider: ${provider.name}\nProposed plan (NOT auto-applied — review before acting):\n$plan")
        // Left in ai-todo.txt: applying arbitrary generated changes is intentionally human-gated.
        return false
    }

    // ---- bookkeeping ---------------------------------------------------------------------------

    private fun done(summary: String, details: String, ok: Boolean): Boolean {
        if (!ok) { suggest("Could not verify completion of: $summary"); return false }
        doneFile.appendText("## $ts — $summary\n$details\n\n")
        logger.lifecycle("[ai-agent] done: $summary")
        return true
    }

    private fun suggest(msg: String) {
        suggestionsFile.appendText("## $ts\n$msg\n\n")
        logger.lifecycle("[ai-agent] suggestion recorded: ${msg.lineSequence().first()}")
    }

    private fun readCreds(): Map<String, String> {
        if (!credsFile.exists()) return emptyMap()
        return credsFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && "=" in it }
            .associate { val (k, v) = it.split("=", limit = 2); k.trim() to v.trim() }
    }

    private fun parse(content: String): List<Block> {
        val out = mutableListOf<Block>()
        val lines = content.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            when {
                line.isBlank() -> { out += Block(Block.Kind.BLANK, line); i++ }
                line.trimStart().startsWith("#") -> { out += Block(Block.Kind.COMMENT, line); i++ }
                line.trimStart().startsWith("*") || line.trimStart().startsWith("-") -> {
                    // both repeatable (*) and one-shot (-) tasks may span multiple lines;
                    // continuation lines are any non-blank line that doesn't start a new marker
                    val kind = if (line.trimStart().startsWith("*")) Block.Kind.REPEATABLE else Block.Kind.ONCE
                    val buf = StringBuilder(line)
                    i++
                    while (i < lines.size && lines[i].isNotBlank() &&
                        !lines[i].trimStart().startsWith("-") && !lines[i].trimStart().startsWith("*") &&
                        !lines[i].trimStart().startsWith("#")
                    ) { buf.append('\n').append(lines[i]); i++ }
                    out += Block(kind, buf.toString())
                }
                else -> { out += Block(Block.Kind.COMMENT, line); i++ }
            }
        }
        return out
    }

    private fun rewriteTodo(all: List<Block>, completed: List<Block>) {
        val keep = all.filter { it !in completed }
        // collapse any doubled blank lines left behind
        val text = keep.joinToString("\n") { it.raw }.replace(Regex("\n{3,}"), "\n\n").trimEnd() + "\n"
        todoFile.writeText(text)
    }
}
