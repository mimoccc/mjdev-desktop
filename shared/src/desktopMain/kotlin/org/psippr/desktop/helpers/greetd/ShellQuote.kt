package org.psippr.desktop.helpers.greetd

/**
 * POSIX shell quoting of single arguments. greetd joins the `start_session` command with spaces
 * and runs it through `/bin/sh -c`, so every argument must be quoted to survive as one argument.
 */
object ShellQuote {
    /** Quote character wrapping an argument; nothing inside it is special to the shell. */
    private const val QUOTE = "'"

    /** A quote inside an argument: close the quoting, an escaped quote, reopen the quoting. */
    private const val ESCAPED_QUOTE = "'\\''"

    /** [arg] as one shell word that the shell turns back into exactly [arg]. */
    fun quote(arg: String): String = QUOTE + arg.replace(QUOTE, ESCAPED_QUOTE) + QUOTE
}
