package org.psippr.desktop.helpers.greetd

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Regression test: a session started by the greeter leaves its output, also of a failure, in a log. */
class SessionLogTest {
    @Test
    fun sessionOutputAndExitAreLogged() {
        val home = Files.createTempDirectory(TEMP_PREFIX).toFile()
        val cmd = SessionLog.wrap(SESSION_NAME, listOf(SHELL, SHELL_ARG, FAILING_EXEC))
        val process = ProcessBuilder(cmd).apply { environment()[HOME_ENV] = home.absolutePath }.start()
        assertEquals(EXIT_CODE, process.waitFor())
        val log = File(home, LOG_PATH).readText()
        assertTrue(log.contains(OUTPUT), log)
        assertTrue(log.contains(ERROR), log)
        assertTrue(log.contains("start:"), log)
        home.deleteRecursively()
    }

    /** Regression: greetd joins the command with spaces for `/bin/sh -c`, the session must still start and log. */
    @Test
    fun sessionStartedTheGreetdWayRunsAndLogs() {
        val home = Files.createTempDirectory(TEMP_PREFIX).toFile()
        val cmd = SessionLog.greetdCmd(SESSION_NAME, listOf(SHELL, SHELL_ARG, FAILING_EXEC))
        val greetdLine = GREETD_EXEC + cmd.joinToString(GREETD_JOIN)
        val process =
            ProcessBuilder(SHELL, SHELL_ARG, greetdLine).apply { environment()[HOME_ENV] = home.absolutePath }.start()
        assertEquals(EXIT_CODE, process.waitFor())
        val log = File(home, LOG_PATH).readText()
        assertTrue(log.contains(OUTPUT), log)
        assertTrue(log.contains(ERROR), log)
        home.deleteRecursively()
    }

    /** A quote inside an argument survives the shell quoting unchanged. */
    @Test
    fun quotedArgumentKeepsItsQuotes() {
        val process = ProcessBuilder(SHELL, SHELL_ARG, PRINT_ARG + ShellQuote.quote(QUOTED_ARG)).start()
        assertEquals(QUOTED_ARG, process.inputStream.bufferedReader().readText())
    }

    @Test
    fun logNameIsFileSafe() = assertEquals(LOG_NAME, SessionLog.logName(SESSION_NAME))

    private companion object {
        const val TEMP_PREFIX = "session-log"
        const val SESSION_NAME = "Ubuntu on Wayland"
        const val LOG_NAME = "ubuntu-on-wayland"
        const val LOG_PATH = ".cache/psippr-desktop/session-$LOG_NAME.log"
        const val HOME_ENV = "HOME"
        const val SHELL = "/bin/sh"
        const val SHELL_ARG = "-c"
        const val OUTPUT = "hello out"
        const val ERROR = "No session bus running"
        const val EXIT_CODE = 3

        /** Start of the line greetd hands to `/bin/sh -c`, the joined command follows. */
        const val GREETD_EXEC = "exec "

        /** Separator greetd joins the command arguments with. */
        const val GREETD_JOIN = " "

        /** Shell line printing its single argument without a newline. */
        const val PRINT_ARG = "printf %s "

        /** Argument with quotes, spaces and shell characters. */
        const val QUOTED_ARG = "it's \"a\" \$HOME; test"

        /** A session line with its own quoting, printing to stdout and stderr, then failing. */
        const val FAILING_EXEC = "echo '$OUTPUT'; echo \"$ERROR\" >&2; exit $EXIT_CODE"
    }
}
