package org.psippr.desktop.helpers.greetd

/**
 * `type` values of the greetd IPC messages (see greetd-ipc(7)).
 *
 * @property value the JSON `type` string
 */
enum class GreetdMessageType(
    val value: String,
) {
    /** Request: starts a login attempt for a user. */
    CreateSession("create_session"),

    /** Request: answers an authentication message. */
    PostAuthMessageResponse("post_auth_message_response"),

    /** Request: starts the authenticated session once the greeter exits. */
    StartSession("start_session"),

    /** Request: drops the session under configuration. */
    CancelSession("cancel_session"),

    /** Response: the request succeeded. */
    Success("success"),

    /** Response: the request failed. */
    Error("error"),

    /** Response: an authentication question or note to answer. */
    AuthMessage("auth_message"),
}
