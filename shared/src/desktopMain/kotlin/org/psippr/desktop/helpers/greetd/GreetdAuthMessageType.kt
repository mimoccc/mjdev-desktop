package org.psippr.desktop.helpers.greetd

/**
 * Kinds of greetd authentication messages (see greetd-ipc(7)).
 *
 * @property value the JSON `auth_message_type` string
 */
enum class GreetdAuthMessageType(
    val value: String,
) {
    /** A question answered with visible input (e.g. the user name). */
    Visible("visible"),

    /** A question answered with secret input (the password). */
    Secret("secret"),

    /** A note, answered without a response. */
    Info("info"),

    /** An error note, answered without a response. */
    Error("error"),
}
