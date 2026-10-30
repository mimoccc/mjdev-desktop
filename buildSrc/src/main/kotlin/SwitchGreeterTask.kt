import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Makes [choice] the display manager started at the next boot, by running
 * `session/switch-greeter.sh` as root: directly when already root, through a pkexec dialog in a
 * GUI session, else through sudo. pkexec detaches stdio, so the script writes to [logPath] and
 * the task prints that log afterwards. Config-cache safe (String / enum inputs only).
 */
abstract class SwitchGreeterTask : DefaultTask() {
    /** Display manager to switch to. */
    @get:Input abstract val choice: Property<GreeterChoice>

    /** Absolute path of `session/switch-greeter.sh`. */
    @get:Input abstract val scriptPath: Property<String>

    /** Log the root script appends to, created by this task so it stays owned by the user. */
    @get:Input abstract val logPath: Property<String>

    @TaskAction
    fun switch() {
        val log = File(logPath.get()).apply { parentFile.mkdirs(); writeText("") }
        val command = escalation() + listOf(SHELL, scriptPath.get(), choice.get().arg, log.absolutePath)
        val exit = ProcessBuilder(command).inheritIO().start().waitFor()
        logger.lifecycle(log.readText())
        check(exit == 0) { "[switch-greeter] failed (exit $exit), see ${log.absolutePath}" }
    }

    // nothing when root, pkexec in a GUI session, else sudo
    private fun escalation(): List<String> = when {
        isRoot() -> emptyList()
        File(PKEXEC).canExecute() && GUI_ENV.any { !System.getenv(it).isNullOrBlank() } -> listOf(PKEXEC)
        else -> listOf(SUDO)
    }

    private fun isRoot(): Boolean = System.getenv(USER_ENV) == ROOT ||
        runCatching {
            ProcessBuilder(ID, ID_UID).start().inputStream.bufferedReader().readText().trim() == ROOT_UID
        }.getOrDefault(false)

    private companion object {
        /** Shell running the script. */
        const val SHELL = "/bin/sh"

        /** Graphical root dialog (polkit). */
        const val PKEXEC = "/usr/bin/pkexec"

        /** Terminal / CI root escalation. */
        const val SUDO = "sudo"

        /** Variables telling a GUI session (where the pkexec dialog can show). */
        val GUI_ENV = listOf("DISPLAY", "WAYLAND_DISPLAY")

        /** Root detection: user name variable, name and uid. */
        const val USER_ENV = "USER"
        const val ROOT = "root"
        const val ROOT_UID = "0"
        const val ID = "id"
        const val ID_UID = "-u"
    }
}
