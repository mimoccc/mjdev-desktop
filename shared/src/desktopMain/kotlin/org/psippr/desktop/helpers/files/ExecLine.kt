package org.psippr.desktop.helpers.files

/**
 * Turns the `Exec=` line of a desktop entry into the command that opens a file, following the
 * desktop entry spec: arguments split at spaces, double quotes group one, the file goes where a
 * file field code (`%f %F %u %U`) is, other field codes are dropped and `%%` is a percent sign.
 */
object ExecLine {
    /** Field codes taking the file. */
    private val FILE_CODES = setOf("%f", "%F", "%u", "%U")

    /** Any field code. */
    private val FIELD_CODE = Regex("%[a-zA-Z]")

    /** Escaped percent sign and the sign itself. */
    private const val ESCAPED_PERCENT = "%%"
    private const val PERCENT = "%"

    /** Groups the characters of one argument. */
    private const val QUOTE = '"'

    /** Escapes the next character inside quotes. */
    private const val ESCAPE = '\\'

    /** Separates arguments. */
    private const val SPACE = ' '

    /** Command running [exec] with [path], or empty when [exec] names no program. */
    fun command(
        exec: String,
        path: String,
    ): List<String> {
        val args = split(exec)
        if (args.isEmpty()) return emptyList()
        val hasFileCode = args.any { arg -> arg in FILE_CODES }
        val command =
            args.mapNotNull { arg ->
                when {
                    arg in FILE_CODES -> path
                    FIELD_CODE.matches(arg) -> null
                    else -> arg.replace(FIELD_CODE, "").replace(ESCAPED_PERCENT, PERCENT)
                }
            }
        // an entry without a file code still gets the file, as file managers do
        return if (hasFileCode) command else command + path
    }

    /** Arguments of [exec]; a quoted part stays one argument. */
    fun split(exec: String): List<String> {
        val args = mutableListOf<String>()
        val current = StringBuilder()
        var quoted = false
        var escaped = false
        exec.trim().forEach { char ->
            when {
                escaped -> {
                    current.append(char)
                    escaped = false
                }
                quoted && char == ESCAPE -> escaped = true
                char == QUOTE -> quoted = !quoted
                !quoted && char == SPACE -> {
                    if (current.isNotEmpty()) args += current.toString()
                    current.clear()
                }
                else -> current.append(char)
            }
        }
        if (current.isNotEmpty()) args += current.toString()
        return args
    }
}
