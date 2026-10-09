package org.psippr.desktop.helpers.greetd

/**
 * Kinds of desktop sessions the login screen can start.
 *
 * @property dir system dir holding the session `.desktop` files
 * @property sessionType value of `XDG_SESSION_TYPE` for the started session
 * @property launcher command put in front of the session command, empty when run directly
 */
enum class LoginSessionKind(
    val dir: String,
    val sessionType: String,
    val launcher: List<String>,
) {
    /** Wayland sessions, started directly (they bring their own compositor). */
    Wayland("/usr/share/wayland-sessions", "wayland", emptyList()),

    /** X11 sessions, started through startx (only offered when startx is installed). */
    X11("/usr/share/xsessions", "x11", listOf("startx", "/usr/bin/env")),
}
