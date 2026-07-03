package org.mjdev.desktop.managers.remote

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.log.Log
import java.awt.Robot
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

/**
 * RFB (VNC) server for the desktop shell. Accepts viewers on [port] and hands each connection
 * to an [RfbClient] on its own thread. Screen capture + input injection go through a single
 * [Robot], so this works in plain `runDesktop` (JVM/AWT) with no compositor — the Kotlin-first
 * source of truth. Connect from any VNC viewer at one of [addresses].
 */
class RemoteDesktopManager(
    @Suppress("unused") private val context: IDesktopContext,
    override val port: Int = DEFAULT_PORT,
) : IRemoteDesktopManager {
    private val running = mutableStateOf(false)
    override val runningState: State<Boolean> get() = running

    private var serverSocket: ServerSocket? = null
    private var acceptThread: Thread? = null
    private val clients = mutableListOf<RfbClient>()
    private val robot: Robot by lazy { Robot() }

    override val addresses: List<String>
        get() = localAddresses().map { "$it:$port" }

    override suspend fun start() = withContext(Dispatchers.IO) {
        if (running.value) return@withContext
        runCatching {
            val server = ServerSocket(port)
            serverSocket = server
            running.value = true
            Log.i("RemoteDesktopManager: VNC server listening on ${addresses.joinToString()}")
            acceptThread =
                thread(name = "mjdev-vnc-accept", isDaemon = true) {
                    acceptLoop(server)
                }
        }.onFailure { e ->
            running.value = false
            Log.e("RemoteDesktopManager: cannot start on port $port: ${e.message}")
        }
        Unit
    }

    private fun acceptLoop(server: ServerSocket) {
        while (!server.isClosed) {
            val socket =
                runCatching { server.accept() }.getOrElse { break }
            handleClient(socket)
        }
    }

    private fun handleClient(socket: Socket) {
        thread(name = "mjdev-vnc-client", isDaemon = true) {
            val client = RfbClient(socket, robot)
            synchronized(clients) { clients.add(client) }
            Log.i("RemoteDesktopManager: client connected ${socket.remoteSocketAddress}")
            runCatching { client.serve() }
                .onFailure { e -> Log.d("RemoteDesktopManager: client ended: ${e.message}") }
            client.close()
            synchronized(clients) { clients.remove(client) }
        }
    }

    override suspend fun stop() = withContext(Dispatchers.IO) {
        if (!running.value) return@withContext
        running.value = false
        synchronized(clients) {
            clients.forEach { it.close() }
            clients.clear()
        }
        runCatching { serverSocket?.close() }
        serverSocket = null
        acceptThread = null
        Log.i("RemoteDesktopManager: VNC server stopped")
    }

    fun dispose() {
        running.value = false
        synchronized(clients) {
            clients.forEach { it.close() }
            clients.clear()
        }
        runCatching { serverSocket?.close() }
    }

    private fun localAddresses(): List<String> = runCatching {
        NetworkInterface
            .getNetworkInterfaces()
            .toList()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { it.inetAddresses.toList() }
            .filterIsInstance<InetAddress>()
            .filter { !it.isLoopbackAddress && it.address.size == 4 }
            .map { it.hostAddress }
    }.getOrDefault(emptyList())

    companion object {
        // 5900 = VNC display :0, the port every viewer defaults to
        private const val DEFAULT_PORT = 5900
    }
}
