package eu.mjdev.desktop.helpers.adb.helpers

import eu.mjdev.desktop.helpers.adb.IAdb
import org.jetbrains.annotations.TestOnly
import java.net.InetSocketAddress
import java.net.Socket

@Suppress("unused")
class AdbImpl @Throws(IllegalArgumentException::class) constructor(
    override val host: String,
    override val port: Int,
    private val keyPair: AdbKeyPair? = null,
    private val connectTimeout: Int = 0,
    private val socketTimeout: Int = 0,
) : IAdb {
    init {
        if (port < 0) {
            throw IllegalArgumentException("port must be >= 0")
        }
        if (connectTimeout < 0) {
            throw IllegalArgumentException("connectTimeout must be >= 0")
        }
        if (socketTimeout < 0) {
            throw IllegalArgumentException("socketTimeout must be >= 0")
        }
    }

    private var connection: Pair<AdbConnection, Socket>? = null

    override val deviceQuery: String
        get() = ""

    override val name: String
        get() = ""

    override fun open(destination: String) = connection().open(destination)

    override fun supportsFeature(feature: String): Boolean {
        return connection().supportsFeature(feature)
    }

    override fun close() {
        connection?.first?.close()
    }

    override fun toString() = "$host:$port"

    @TestOnly
    fun closeConnection() {
        connection?.second?.close()
    }

    @Synchronized
    private fun connection(): AdbConnection {
        var connection = connection
        if (connection == null || connection.second.isClosed) {
            connection = newConnection()
            this.connection = connection
        }
        return connection.first
    }

    private fun newConnection(): Pair<AdbConnection, Socket> {
        val socketAddress = InetSocketAddress(host, port)
        val socket = Socket()
        socket.soTimeout = socketTimeout
        socket.connect(socketAddress, connectTimeout)
        val adbConnection = AdbConnection.connect(socket, keyPair)
        return adbConnection to socket
    }
}
