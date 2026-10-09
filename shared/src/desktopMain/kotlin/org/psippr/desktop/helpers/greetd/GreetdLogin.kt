package org.psippr.desktop.helpers.greetd

import java.nio.file.Path

/**
 * Logs a user in through greetd: answers the PAM questions (password for secret ones, the user
 * name for visible ones), then asks greetd to start the chosen [LoginSession]. The session starts
 * once the greeter process exits.
 */
object GreetdLogin {
    /** True when this process runs as the greetd greeter (greetd set its socket variable). */
    val isGreeter: Boolean
        get() = socketPath() != null

    /** Logs [username] in with [password] into [session]; failure carries the greetd / PAM message. */
    fun login(
        username: String,
        password: String,
        session: LoginSession = LoginSessions.fallback,
    ): Result<Unit> = runCatching {
        val socket = socketPath() ?: error(NO_SOCKET)
        GreetdConnection(socket).use { connection -> authenticate(connection, username, password, session) }
    }

    // walks the auth dialog until greetd reports success, then starts the session
    private fun authenticate(
        connection: GreetdConnection,
        username: String,
        password: String,
        session: LoginSession,
    ) {
        var reply = connection.send(GreetdRequest(GreetdMessageType.CreateSession.value, username = username))
        while (reply.type == GreetdMessageType.AuthMessage.value) {
            reply = connection.send(answer(reply, username, password))
        }
        if (reply.type != GreetdMessageType.Success.value) fail(connection, reply)
        val start = GreetdRequest(GreetdMessageType.StartSession.value, cmd = session.cmd, env = session.env)
        val started = connection.send(start)
        if (started.type != GreetdMessageType.Success.value) fail(connection, started)
    }

    // the answer greetd expects for one auth message
    private fun answer(
        message: GreetdResponse,
        username: String,
        password: String,
    ): GreetdRequest {
        val response =
            when (message.authMessageType) {
                GreetdAuthMessageType.Secret.value -> password
                GreetdAuthMessageType.Visible.value -> username
                else -> null
            }
        return GreetdRequest(GreetdMessageType.PostAuthMessageResponse.value, response = response)
    }

    // drops the half configured session so the next attempt starts clean, then reports why
    private fun fail(
        connection: GreetdConnection,
        reply: GreetdResponse,
    ): Nothing {
        runCatching { connection.send(GreetdRequest(GreetdMessageType.CancelSession.value)) }
        error(reply.description ?: reply.authMessage ?: reply.errorType ?: reply.type)
    }

    private fun socketPath(): Path? = System.getenv(GreetdSetting.SocketEnv.value)?.takeIf { it.isNotBlank() }?.let(Path::of)

    private const val NO_SOCKET = "not started by greetd"
}
