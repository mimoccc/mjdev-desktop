import groovy.json.JsonSlurper
import java.io.File
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.file.Files
import java.nio.file.Path
import kotlin.concurrent.thread

/**
 * Stand-in for the greetd daemon in a nested test run of the login screen. It speaks the greetd
 * IPC on a unix socket, accepts only [ACCEPTED_PASSWORD] for any user and answers the session
 * start with success WITHOUT starting anything.
 *
 * It shows that the greeter walks the login dialog and exits; it can never prove a real login
 * (no PAM, no session). That needs the real greetd, e.g. the ISO in QEMU.
 */
object MockGreetd {
    /** The only password the mock accepts; a test value, not a secret. */
    const val ACCEPTED_PASSWORD = "test"

    /** Environment variable greetd sets on its greeter, holding the IPC socket path. */
    const val SOCKET_ENV = "GREETD_SOCK"

    /** Socket file name; kept short, a unix socket path is limited to 108 bytes. */
    private const val SOCKET_NAME = "psippr-mock-greetd.sock"

    /** Name of the thread answering the greeter. */
    private const val THREAD_NAME = "mock-greetd"

    /** Prefix of the mock's lines in the Gradle output. */
    private const val LOG_TAG = "mock greetd:"

    /** JSON field holding the request type. */
    private const val FIELD_TYPE = "type"

    /** JSON field holding the session command of a `start_session` request. */
    private const val FIELD_CMD = "cmd"

    /** The listening socket of the running mock, null when stopped. */
    @Volatile
    private var server: ServerSocketChannel? = null

    /** Socket path of the mock under [runtimeDir] (`XDG_RUNTIME_DIR`), else the temp folder. */
    fun socketPath(runtimeDir: String?): Path =
        File(runtimeDir?.takeIf { it.isNotBlank() } ?: System.getProperty("java.io.tmpdir"), SOCKET_NAME).toPath()

    /** Starts the mock on [socket], replacing one left running; it accepts only [password]. */
    fun start(
        socket: Path,
        password: String = ACCEPTED_PASSWORD,
    ) {
        stop()
        Files.deleteIfExists(socket)
        val opened = ServerSocketChannel.open(StandardProtocolFamily.UNIX).bind(UnixDomainSocketAddress.of(socket))
        server = opened
        thread(isDaemon = true, name = THREAD_NAME) { accept(opened, password) }
        println("$LOG_TAG listening on $socket (no PAM, no session is started)")
    }

    /** Stops the mock; safe when it does not run. */
    fun stop() {
        runCatching { server?.close() }
        server = null
    }

    // one greeter connection after another until the socket is closed
    private fun accept(
        opened: ServerSocketChannel,
        password: String,
    ) {
        while (opened.isOpen) {
            runCatching { opened.accept().use { client -> serve(client, MockGreetdSession(password)) } }
        }
    }

    // answers the requests of one connection; request contents (the password) are never printed
    private fun serve(
        client: SocketChannel,
        session: MockGreetdSession,
    ) {
        while (true) {
            val request = MockGreetdFrame.read(client)?.let(::parse) ?: return
            val reply = session.reply(request)
            println("$LOG_TAG ${request[FIELD_TYPE]} -> ${reply.name}${sessionNote(request, reply)}")
            MockGreetdFrame.write(client, reply.json)
        }
    }

    // the session command a real greetd would start now
    private fun sessionNote(
        request: Map<*, *>,
        reply: MockGreetdReply,
    ): String {
        val started = MockGreetdRequestType.of(request[FIELD_TYPE]) == MockGreetdRequestType.StartSession
        return if (started && reply == MockGreetdReply.Success) " (not started: ${request[FIELD_CMD]})" else ""
    }

    // the JSON object of a request, an empty one for anything else
    private fun parse(json: String): Map<*, *> =
        runCatching { JsonSlurper().parseText(json) as? Map<*, *> }.getOrNull() ?: emptyMap<Any, Any>()
}
