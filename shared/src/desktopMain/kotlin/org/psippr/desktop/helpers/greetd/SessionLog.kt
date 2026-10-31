package org.psippr.desktop.helpers.greetd

/**
 * Wraps a session command so all its output lands in a log of the logged in user, instead of
 * being dropped by greetd: `~/.cache/psippr-desktop/session-<name>.log`, appended per start with
 * a header line holding the date and the command. A session that dies at once (e.g. a desktop
 * that cannot start) so leaves its error message behind.
 */
object SessionLog {
    /** Shell running the wrapper; its script gets the log name and the command as arguments. */
    private val SHELL = listOf("/bin/sh", "-c")

    /**
     * Wrapper script: `$1` is the log name, the rest the session command. Positional arguments
     * keep the command's own quoting intact; `$HOME` is the user's, greetd runs it as the user.
     */
    private const val SCRIPT =
        "dir=\"\$HOME/.cache/psippr-desktop\"; mkdir -p \"\$dir\"; " +
            "log=\"\$dir/session-\$1.log\"; shift; exec >>\"\$log\" 2>&1; " +
            "echo \"== \$(date '+%F %T') start: \$*\"; exec \"\$@\""

    /** `$0` of the wrapper script, shown in process lists. */
    private const val SCRIPT_NAME = "psippr-session-log"

    /** Characters of a session name not kept in the log file name. */
    private val UNSAFE_CHARS = Regex("[^a-z0-9]+")

    /** Separator replacing [UNSAFE_CHARS]. */
    private const val NAME_SEPARATOR = "-"

    /** [cmd] of the session [name], run with its output appended to the session's log. */
    fun wrap(
        name: String,
        cmd: List<String>,
    ): List<String> = SHELL + SCRIPT + SCRIPT_NAME + logName(name) + cmd

    /**
     * [wrap] as greetd's `start_session` command: greetd joins the arguments with spaces and runs
     * them through `/bin/sh -c`, so each one is shell quoted to reach the wrapper unchanged.
     */
    fun greetdCmd(
        name: String,
        cmd: List<String>,
    ): List<String> = wrap(name, cmd).map(ShellQuote::quote)

    /** Log file name part of the session [name] (`Ubuntu on Wayland` -> `ubuntu-on-wayland`). */
    fun logName(name: String): String =
        name.lowercase().replace(UNSAFE_CHARS, NAME_SEPARATOR).trim(NAME_SEPARATOR.single())
}
