package org.psippr.desktop.helpers.greetd

/**
 * One desktop session the user can pick on the login screen.
 *
 * @property name shown name (`Name=` of the session file)
 * @property cmd command line greetd runs as the user
 * @property env extra session environment (`KEY=value`)
 */
data class LoginSession(
    val name: String,
    val cmd: List<String>,
    val env: List<String>,
)
