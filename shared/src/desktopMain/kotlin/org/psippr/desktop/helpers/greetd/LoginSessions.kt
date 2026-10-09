package org.psippr.desktop.helpers.greetd

import java.io.File

/**
 * Desktop sessions installed on the machine (wayland-sessions, xsessions when startx exists),
 * psippr first. Without any session file the psippr session is still offered.
 */
object LoginSessions {
    /** Shell running a session `Exec=` line, so its quoting and `env` prefixes keep working. */
    private val SHELL = listOf("/bin/sh", "-c")

    /** Session file suffix. */
    private const val DESKTOP_SUFFIX = ".desktop"

    /** Session file keys read. */
    private const val KEY_NAME = "Name="
    private const val KEY_EXEC = "Exec="
    private const val KEY_DESKTOP_NAMES = "DesktopNames="
    private const val KEY_HIDDEN = "Hidden=true"
    private const val KEY_NO_DISPLAY = "NoDisplay=true"

    /** Separator of `DesktopNames=` entries and of `XDG_CURRENT_DESKTOP`. */
    private const val DESKTOP_NAMES_SEPARATOR = ";"
    private const val CURRENT_DESKTOP_SEPARATOR = ":"

    /** Environment the session gets, besides what PAM sets. */
    private const val ENV_SESSION_TYPE = "XDG_SESSION_TYPE="
    private const val ENV_SESSION_DESKTOP = "XDG_SESSION_DESKTOP="
    private const val ENV_CURRENT_DESKTOP = "XDG_CURRENT_DESKTOP="

    /** Desktop file field codes (%f, %U, ...) that a session Exec may carry. */
    private val FIELD_CODES = Regex("%[a-zA-Z]")

    /** Session id of this desktop (its session file name) and its fallback entry. */
    private const val PSIPPR_ID = "psippr"
    private const val PSIPPR_NAME = "Psippr Desktop"

    /** psippr session used when no session file is installed. */
    val fallback: LoginSession =
        LoginSession(
            name = PSIPPR_NAME,
            cmd = listOf(GreetdSetting.SessionCommand.value),
            env = listOf(ENV_SESSION_TYPE + LoginSessionKind.Wayland.sessionType, ENV_SESSION_DESKTOP + PSIPPR_ID),
        )

    /** Installed sessions, psippr first, one entry per distinct command. */
    fun all(): List<LoginSession> {
        val found =
            LoginSessionKind.entries
                .filter { kind -> kind.launcher.isEmpty() || hasCommand(kind.launcher.first()) }
                .flatMap { kind -> filesOf(kind).mapNotNull { file -> parse(kind, file) } }
                .distinctBy { session -> session.cmd }
        val psippr = found.filter { session -> session.env.contains(ENV_SESSION_DESKTOP + PSIPPR_ID) }
        return psippr.ifEmpty { listOf(fallback) } + (found - psippr.toSet())
    }

    /** Session of one session file, null when hidden or without a command. */
    fun parse(
        kind: LoginSessionKind,
        file: File,
    ): LoginSession? = runCatching { parse(kind, file.nameWithoutExtension, file.readLines()) }.getOrNull()

    /** Session from the [lines] of the session file with id [id], null when hidden or without a command. */
    fun parse(
        kind: LoginSessionKind,
        id: String,
        lines: List<String>,
    ): LoginSession? {
        if (lines.any { line -> line.trim() == KEY_HIDDEN || line.trim() == KEY_NO_DISPLAY }) return null
        val exec = value(lines, KEY_EXEC)?.replace(FIELD_CODES, "")?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val desktops = value(lines, KEY_DESKTOP_NAMES)?.split(DESKTOP_NAMES_SEPARATOR)?.filter { it.isNotBlank() }.orEmpty()
        val env =
            listOf(ENV_SESSION_TYPE + kind.sessionType, ENV_SESSION_DESKTOP + id) +
                if (desktops.isEmpty()) emptyList() else listOf(ENV_CURRENT_DESKTOP + desktops.joinToString(CURRENT_DESKTOP_SEPARATOR))
        return LoginSession(
            name = value(lines, KEY_NAME) ?: id,
            cmd = kind.launcher + SHELL + exec,
            env = env,
        )
    }

    // first value of a key in the [Desktop Entry] lines (session files hold one group)
    private fun value(
        lines: List<String>,
        key: String,
    ): String? = lines.firstOrNull { line -> line.startsWith(key) }?.removePrefix(key)?.trim()

    private fun filesOf(kind: LoginSessionKind): List<File> =
        File(kind.dir).listFiles { file -> file.name.endsWith(DESKTOP_SUFFIX) }?.sortedBy { it.name }.orEmpty()

    private fun hasCommand(name: String): Boolean =
        System.getenv(PATH_ENV).orEmpty().split(File.pathSeparator).any { dir -> File(dir, name).canExecute() }

    /** Environment variable with the command search path. */
    private const val PATH_ENV = "PATH"
}
