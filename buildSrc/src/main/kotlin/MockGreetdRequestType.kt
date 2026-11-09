/**
 * Requests a greeter sends to greetd that the mock answers (see greetd-ipc(7)).
 *
 * @property value the JSON `type` string
 */
enum class MockGreetdRequestType(
    val value: String,
) {
    /** Starts a login attempt for a user. */
    CreateSession("create_session"),

    /** Answers the password prompt. */
    PostAuthMessageResponse("post_auth_message_response"),

    /** Starts the session of the logged-in user. */
    StartSession("start_session"),

    /** Drops the login attempt. */
    CancelSession("cancel_session"),
    ;

    companion object {
        /** The request type with the JSON `type` [value], null for an unknown one. */
        fun of(value: Any?): MockGreetdRequestType? = entries.firstOrNull { type -> type.value == value }
    }
}
