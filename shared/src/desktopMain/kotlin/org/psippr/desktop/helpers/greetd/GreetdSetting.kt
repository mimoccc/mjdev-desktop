package org.psippr.desktop.helpers.greetd

/**
 * Fixed names used to talk to greetd.
 *
 * @property value the name / command
 */
enum class GreetdSetting(
    val value: String,
) {
    /** Environment variable greetd sets on the greeter, holding its IPC socket path. */
    SocketEnv("GREETD_SOCK"),

    /** Command greetd runs as the user after a successful login. */
    SessionCommand("psippr-session"),
}
