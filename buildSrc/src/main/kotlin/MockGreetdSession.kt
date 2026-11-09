/**
 * Login dialog of one greeter connection to the mock greetd: password prompt, check against the
 * single [acceptedPassword], then the session start. Nothing is started and no PAM is involved.
 */
class MockGreetdSession(
    private val acceptedPassword: String,
) {
    /** True once the accepted password was given in this attempt. */
    private var authenticated = false

    /** The reply to one parsed greeter [request] (its JSON object). */
    fun reply(request: Map<*, *>): MockGreetdReply =
        when (MockGreetdRequestType.of(request[FIELD_TYPE])) {
            MockGreetdRequestType.CreateSession -> restart(MockGreetdReply.PasswordPrompt)
            MockGreetdRequestType.PostAuthMessageResponse -> check(request[FIELD_RESPONSE])
            MockGreetdRequestType.StartSession -> start()
            MockGreetdRequestType.CancelSession -> restart(MockGreetdReply.Success)
            null -> MockGreetdReply.UnknownRequest
        }

    // a new or cancelled attempt is never logged in
    private fun restart(reply: MockGreetdReply): MockGreetdReply {
        authenticated = false
        return reply
    }

    private fun check(password: Any?): MockGreetdReply {
        authenticated = password == acceptedPassword
        return if (authenticated) MockGreetdReply.Success else MockGreetdReply.WrongPassword
    }

    private fun start(): MockGreetdReply =
        if (authenticated) MockGreetdReply.Success else MockGreetdReply.NotAuthenticated

    private companion object {
        /** JSON field holding the request type. */
        const val FIELD_TYPE = "type"

        /** JSON field holding the answer to the password prompt. */
        const val FIELD_RESPONSE = "response"
    }
}
