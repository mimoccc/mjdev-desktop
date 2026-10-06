package org.mjdev.desktop.helpers.font.guard

import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.FontconfigFileEnv
import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.GuardMarkerEnv
import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.GuardMarkerValue
import java.nio.file.Path

/**
 * Starts this same process again (same executable and arguments) with fontconfig pointed to the
 * guard file. A running JVM cannot change its own environment, and Skia reads fontconfig only
 * once, so a fresh process is the only way to apply the file.
 */
object FontGuardRelauncher {
    /** Prefix of a Java launcher argument file (`@file`), which is gone once the JVM has started. */
    private const val ARG_FILE_PREFIX = "@"

    /**
     * Command line starting this process again with [appArgs], or null when it cannot be known.
     * The original command line is used when it is complete (packaged launcher); when it refers
     * to an `@argfile` (Gradle, IDE) it is rebuilt from the JVM (see [JvmCommandLine]).
     */
    fun currentCommand(appArgs: List<String>): List<String>? =
        originalCommand() ?: runCatching { JvmCommandLine.rebuild(appArgs) }.getOrNull()

    // original command line of this process, or null when unknown or when it uses an @argfile
    private fun originalCommand(): List<String>? = ProcessHandle
        .current()
        .info()
        .let { info ->
            val command = info.command().orElse(null) ?: return null
            val arguments = info.arguments().orElse(null)?.toList() ?: return null
            val usesArgFile = arguments.any { argument -> argument.startsWith(ARG_FILE_PREFIX) }
            if (usesArgFile) null else listOf(command) + arguments
        }

    /**
     * Runs [command] with [guardConfig] and waits for it; returns its exit code. The child shares
     * the terminal and is stopped together with this process (e.g. when the compositor ends it).
     */
    fun relaunch(
        command: List<String>,
        guardConfig: Path,
    ): Int {
        val child =
            ProcessBuilder(command)
                .inheritIO()
                .apply {
                    environment()[FontconfigFileEnv.value] = guardConfig.toString()
                    environment()[GuardMarkerEnv.value] = GuardMarkerValue.value
                }.start()
        Runtime.getRuntime().addShutdownHook(Thread { child.destroy() })
        return child.waitFor()
    }
}
