import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.SocketChannel
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression: a nested greeter run without any greetd behind `GREETD_SOCK` could only fail the
 * login ("login failed but looks ok"). The mock must answer the whole dialog on a real socket.
 */
class MockGreetdTest {
    @Test
    fun acceptedPasswordLogsInAndStartsTheSession() {
        val session = MockGreetdSession(PASSWORD)
        assertEquals(MockGreetdReply.PasswordPrompt, session.reply(request(MockGreetdRequestType.CreateSession)))
        assertEquals(MockGreetdReply.Success, session.reply(answer(PASSWORD)))
        assertEquals(MockGreetdReply.Success, session.reply(request(MockGreetdRequestType.StartSession)))
    }

    @Test
    fun wrongPasswordIsRefusedAndStartsNothing() {
        val session = MockGreetdSession(PASSWORD)
        session.reply(request(MockGreetdRequestType.CreateSession))
        assertEquals(MockGreetdReply.WrongPassword, session.reply(answer(OTHER_PASSWORD)))
        assertEquals(MockGreetdReply.NotAuthenticated, session.reply(request(MockGreetdRequestType.StartSession)))
    }

    @Test
    fun cancelledLoginHasToAuthenticateAgain() {
        val session = MockGreetdSession(PASSWORD)
        session.reply(request(MockGreetdRequestType.CreateSession))
        session.reply(answer(PASSWORD))
        assertEquals(MockGreetdReply.Success, session.reply(request(MockGreetdRequestType.CancelSession)))
        assertEquals(MockGreetdReply.NotAuthenticated, session.reply(request(MockGreetdRequestType.StartSession)))
    }

    @Test
    fun unknownRequestIsAnError() {
        assertEquals(MockGreetdReply.UnknownRequest, MockGreetdSession(PASSWORD).reply(mapOf("type" to "nonsense")))
    }

    @Test
    fun dialogWorksOverTheSocket() {
        val dir = Files.createTempDirectory(TEMP_PREFIX)
        val socket = MockGreetd.socketPath(dir.toString())
        MockGreetd.start(socket, PASSWORD)
        val replies =
            SocketChannel.open(StandardProtocolFamily.UNIX).use { channel ->
                channel.connect(UnixDomainSocketAddress.of(socket))
                listOf(
                    """{"type":"create_session","username":"anyone"}""",
                    """{"type":"post_auth_message_response","response":"$PASSWORD"}""",
                    """{"type":"start_session","cmd":["psippr-session"]}""",
                ).map { json ->
                    MockGreetdFrame.write(channel, json)
                    MockGreetdFrame.read(channel)
                }
            }
        MockGreetd.stop()
        dir.toFile().deleteRecursively()
        assertEquals(
            listOf(MockGreetdReply.PasswordPrompt.json, MockGreetdReply.Success.json, MockGreetdReply.Success.json),
            replies,
        )
        assertTrue(Files.notExists(dir))
    }

    private fun request(type: MockGreetdRequestType): Map<String, String> = mapOf("type" to type.value)

    private fun answer(password: String): Map<String, String> =
        mapOf("type" to MockGreetdRequestType.PostAuthMessageResponse.value, "response" to password)

    private companion object {
        const val TEMP_PREFIX = "mock-greetd-test"
        const val PASSWORD = "accepted"
        const val OTHER_PASSWORD = "other"
    }
}
