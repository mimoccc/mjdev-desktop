package org.psippr.desktop.components.greeter

/**
 * Texts of the login screen.
 *
 * @property text the shown text
 */
enum class LoginLabels(
    val text: String,
) {
    /** Shown while greetd checks the password. */
    CheckingPassword("Logging in…"),

    /** Prefix of a failed login message. */
    LoginFailed("Login failed: "),

    /** Shown when the machine has no account that can log in. */
    NoUsers("No user accounts found"),
}
