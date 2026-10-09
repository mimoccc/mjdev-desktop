package org.psippr.desktop.helpers.greetd

import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.file.Files
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Regression test: the greetd dialog answers the password prompt and starts psippr-session. */
class GreetdLoginTest {
    @Test
    fun passwordPromptIsAnsweredAndSessionStarted() {
        val dir = Files.createTempDirectory(TEMP_PREFIX)
        val socket = dir.resolve(SOCKET_NAME)
        val received = mutableListOf<String>()
        val server = ServerSocketChannel.open(StandardProtocolFamily.UNIX).bind(UnixDomainSocketAddress.of(socket))
        val greetd =
            thread {
                server.accept().use { client ->
                    received += read(client)
                    write(client, """{"type":"auth_message","auth_message_type":"secret","auth_message":"Password: "}""")
                    received += read(client)
                    write(client, """{"type":"success"}""")
                    received += read(client)
                    write(client, """{"type":"success"}""")
                }
            }
        GreetdConnection(socket).use { connection ->
            assertEquals(GreetdMessageType.AuthMessage.value, connection.send(GreetdRequest(GreetdMessageType.CreateSession.value, username = USER)).type)
            assertEquals(GreetdMessageType.Success.value, connection.send(GreetdRequest(GreetdMessageType.PostAuthMessageResponse.value, response = PASSWORD)).type)
            assertEquals(GreetdMessageType.Success.value, connection.send(GreetdRequest(GreetdMessageType.StartSession.value, cmd = listOf(GreetdSetting.SessionCommand.value))).type)
        }
        greetd.join()
        server.close()
        assertTrue(received[0].contains("\"username\":\"$USER\""))
        assertTrue(received[1].contains("\"response\":\"$PASSWORD\""))
        assertTrue(received[2].contains(GreetdSetting.SessionCommand.value))
        dir.toFile().deleteRecursively()
    }

    private fun read(channel: SocketChannel): String {
        val length = ByteBuffer.allocate(Int.SIZE_BYTES).order(ByteOrder.nativeOrder())
        while (length.hasRemaining()) channel.read(length)
        val payload = ByteBuffer.allocate(length.flip().int)
        while (payload.hasRemaining()) channel.read(payload)
        return String(payload.array(), Charsets.UTF_8)
    }

    private fun write(
        channel: SocketChannel,
        json: String,
    ) {
        val bytes = json.toByteArray(Charsets.UTF_8)
        val buffer = ByteBuffer.allocate(Int.SIZE_BYTES + bytes.size).order(ByteOrder.nativeOrder()).putInt(bytes.size).put(bytes).flip()
        while (buffer.hasRemaining()) channel.write(buffer)
    }

    private companion object {
        const val TEMP_PREFIX = "greetd-test"
        const val SOCKET_NAME = "greetd.sock"
        const val USER = "psippr"
        const val PASSWORD = "secret"
    }
}
