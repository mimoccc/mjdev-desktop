/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.psippr.compositor

/**
 * Own D-Bus session for a nested run. The host's bus belongs to the host desktop: apps it starts
 * (gnome-terminal-server, nautilus, ...) open on the host display, and exporting the nested display
 * into it broke the host's apps. A private bus, told about the nested display, starts them inside
 * the nested desktop instead and lives exactly as long as the shell.
 */
object NestedSessionBus {
    /** Runs a command on a fresh session bus that ends with it (part of dbus). */
    private const val RUN_SESSION = "dbus-run-session --"

    /** Shell running the inner command line. */
    private const val SHELL = "/bin/sh -c"

    /** Tells the private bus's activation the nested display, desktop and session type. */
    private const val EXPORT =
        "dbus-update-activation-environment WAYLAND_DISPLAY DISPLAY XDG_CURRENT_DESKTOP XDG_SESSION_TYPE " +
            "2>/dev/null"

    /** Single quote, and how one is written inside a single quoted word. */
    private const val QUOTE = "'"
    private const val ESCAPED_QUOTE = "'\\''"

    /**
     * [shellCmd] wrapped so it runs on its own bus. `exec` keeps one pid from the compositor's
     * spawn down to the shell's exit status, so a shell crash is still seen and restarted.
     */
    fun wrap(shellCmd: String): String = "exec $RUN_SESSION $SHELL ${quote("$EXPORT; exec $shellCmd")}"

    // POSIX single quoting: the whole inner line stays one argument of sh -c
    private fun quote(text: String): String = QUOTE + text.replace(QUOTE, ESCAPED_QUOTE) + QUOTE
}
