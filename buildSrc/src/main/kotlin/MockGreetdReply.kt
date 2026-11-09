/**
 * Replies of the mock greetd, in the JSON form greetd itself sends (see greetd-ipc(7)).
 *
 * @property json the reply message
 */
enum class MockGreetdReply(
    val json: String,
) {
    /** Asks for the password, as PAM does after `create_session`. */
    PasswordPrompt("""{"type":"auth_message","auth_message_type":"secret","auth_message":"Password: "}"""),

    /** The request was accepted. */
    Success("""{"type":"success"}"""),

    /** The password was not the accepted one. */
    WrongPassword("""{"type":"error","error_type":"auth_error","description":"mock greetd: wrong password"}"""),

    /** A session start was asked for before a successful login. */
    NotAuthenticated("""{"type":"error","error_type":"error","description":"mock greetd: not logged in"}"""),

    /** The request type is not one the mock knows. */
    UnknownRequest("""{"type":"error","error_type":"error","description":"mock greetd: unknown request"}"""),
}
