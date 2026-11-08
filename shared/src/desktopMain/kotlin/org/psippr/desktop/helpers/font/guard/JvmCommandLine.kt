package org.psippr.desktop.helpers.font.guard

import java.lang.management.ManagementFactory
import java.nio.file.Paths

/**
 * Rebuilds the command line of this JVM from the JVM itself: own `java` binary, JVM options,
 * class path, main class and the given app arguments. Needed when the JVM was started from an
 * `@argfile` (Gradle, IDE) - that file is deleted right after start, so the original command
 * line cannot be reused.
 *
 * Uses `java.management`, which a trimmed (jlink) runtime may not contain; kept in its own
 * object so a missing module only fails this call (callers catch it).
 */
object JvmCommandLine {
    /** System property of the Java runtime directory. */
    private const val JAVA_HOME_PROPERTY = "java.home"

    /** System property of the class path. */
    private const val CLASS_PATH_PROPERTY = "java.class.path"

    /** System property holding the main class followed by the app arguments. */
    private const val JAVA_COMMAND_PROPERTY = "sun.java.command"

    /** Java launcher inside [JAVA_HOME_PROPERTY]. */
    private const val JAVA_BINARY_DIR = "bin"
    private const val JAVA_BINARY = "java"

    /** Class path option of the Java launcher. */
    private const val CLASS_PATH_OPTION = "-cp"

    /** Separator between the main class and the arguments in [JAVA_COMMAND_PROPERTY]. */
    private const val COMMAND_SEPARATOR = " "

    /** Command line starting this JVM again with [appArgs], or null when something is unknown. */
    fun rebuild(appArgs: List<String>): List<String>? {
        val javaHome = System.getProperty(JAVA_HOME_PROPERTY) ?: return null
        val classPath = System.getProperty(CLASS_PATH_PROPERTY) ?: return null
        val mainClass =
            System
                .getProperty(JAVA_COMMAND_PROPERTY)
                ?.substringBefore(COMMAND_SEPARATOR)
                ?.takeIf { it.isNotBlank() } ?: return null
        val java = Paths.get(javaHome, JAVA_BINARY_DIR, JAVA_BINARY).toString()
        val jvmOptions = ManagementFactory.getRuntimeMXBean().inputArguments
        return listOf(java) + jvmOptions + listOf(CLASS_PATH_OPTION, classPath, mainClass) + appArgs
    }
}
