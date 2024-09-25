package eu.mjdev.desktop.helpers.adb.adbserver

import eu.mjdev.desktop.helpers.adb.IAdb
import eu.mjdev.desktop.helpers.adb.helpers.IAdbStream
import okio.buffer
import okio.sink
import okio.source
import java.io.DataInputStream
import java.net.Socket

class AdbServerAdb(
    override val host: String,
    override val port: Int,
    override val deviceQuery: String,
    override val name: String,
) : IAdb {
    private val supportedFeatures: Set<String>

    init {
        supportedFeatures = open("host:features").use {
            val features = AdbServer.readString(DataInputStream(it.source.inputStream()))
            features.split(",").toSet()
        }
    }

    override fun open(destination: String): IAdbStream {
        AdbBinary.ensureServerRunning(host, port)
        val socket = Socket(host, port)
        AdbServer.send(socket, deviceQuery)
        AdbServer.send(socket, destination)
        return object : IAdbStream {
            override val source = socket.source().buffer()
            override val sink = socket.sink().buffer()
            override fun close() = socket.close()
        }
    }

    override fun supportsFeature(feature: String): Boolean {
        return feature in supportedFeatures
    }

    override fun close() {}

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean = when (other) {
        null -> false
        !is IAdb -> false
        else -> other.name.contentEquals(this.name) == true
    }

    override fun hashCode(): Int {
        var result = host.hashCode()
        result = 31 * result + port
        result = 31 * result + deviceQuery.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + supportedFeatures.hashCode()
        return result
    }
}