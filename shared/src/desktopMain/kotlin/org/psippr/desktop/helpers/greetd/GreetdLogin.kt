package org.psippr.desktop.helpers.greetd

import org.psippr.desktop.log.Log
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
        Log.i("$LOG_TAG login user=$username session=${session.name}")
        val socket = socketPath() ?: error(NO_SOCKET)
        GreetdConnection(socket).use { connection -> authenticate(connection, username, password, session) }
    }.onSuccess { Log.i("$LOG_TAG session ${session.name} handed to greetd") }
        .onFailure { e -> Log.e("$LOG_TAG login failed: ${e.message}") }

    // walks the auth dialog until greetd reports success, then starts the session
    private fun authenticate(
        connection: GreetdConnection,
        username: String,
        password: String,
        session: LoginSession,
    ) {
        var reply = connection.send(GreetdRequest(GreetdMessageType.CreateSession.value, username = username))
        logReply(reply)
        while (reply.type == GreetdMessageType.AuthMessage.value) {
            reply = connection.send(answer(reply, username, password))
            logReply(reply)
        }
        if (reply.type != GreetdMessageType.Success.value) fail(connection, reply)
        // old: the session's output was dropped by greetd, a failing session left no trace
//        val start = GreetdRequest(GreetdMessageType.StartSession.value, cmd = session.cmd, env = session.env)
        // old: greetd joins the arguments with spaces for /bin/sh -c, the unquoted wrapper script
        // fell apart there and the session ended at once without any output
//        val cmd = SessionLog.wrap(session.name, session.cmd)
        val cmd = SessionLog.greetdCmd(session.name, session.cmd)
        Log.i("$LOG_TAG start_session cmd=$cmd env=${session.env}")
        val start = GreetdRequest(GreetdMessageType.StartSession.value, cmd = cmd, env = session.env)
        val started = connection.send(start)
        logReply(started)
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

    // one greetd reply in the greeter log; it never holds the password (only requests carry it)
    private fun logReply(reply: GreetdResponse) = Log.i(
        "$LOG_TAG greetd ${reply.type}" +
            listOfNotNull(reply.authMessageType, reply.authMessage, reply.errorType, reply.description)
                .joinToString(LOG_FIELD_SEPARATOR, LOG_FIELD_SEPARATOR),
    )

    private fun socketPath(): Path? = System
        .getenv(GreetdSetting.SocketEnv.value)
        ?.takeIf {
            it.isNotBlank()
        }?.let(Path::of)

    private const val NO_SOCKET = "not started by greetd"

    /** Prefix of the greeter's log lines. */
    private const val LOG_TAG = "greeter:"

    /** Separator of the reply fields in a log line. */
    private const val LOG_FIELD_SEPARATOR = " "
}
