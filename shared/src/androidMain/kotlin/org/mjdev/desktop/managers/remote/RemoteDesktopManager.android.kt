package org.mjdev.desktop.managers.remote

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.log.Log

class RemoteDesktopManager(
    private val context: IDesktopContext,
    override val port: Int = DEFAULT_PORT,
) : IRemoteDesktopManager {
    private val running = mutableStateOf(false)
    override val runningState: State<Boolean> get() = running

    override val addresses: List<String>
        get() = listOf("localhost:$port")

    override suspend fun start() = withContext(Dispatchers.IO) {
        if (running.value) return@withContext
        running.value = true
        Log.i("RemoteDesktopManager: Remote Compose not yet implemented on Android")
    }

    override suspend fun stop() = withContext(Dispatchers.IO) {
        if (!running.value) return@withContext
        running.value = false
        Log.i("RemoteDesktopManager: Remote Compose not yet implemented on Android")
    }

    private val clientScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connections = mutableStateListOf<RemoteConnection>()
    override val remoteConnections: State<List<RemoteConnection>> = derivedStateOf { connections.toList() }

    override suspend fun createConnection(
        id: String,
        host: String,
        port: Int,
        path: String,
        enableAudio: Boolean,
    ): RemoteConnection = withContext(Dispatchers.IO) {
        val handle = RemoteClientHandle(enableAudio, host, port, clientScope)
        val conn = RemoteConnection.Disconnected(id)
        connections.add(conn)
        conn
    }

    override suspend fun closeConnection(connection: RemoteConnection) {
        connections.remove(connection)
    }

    override suspend fun closeAllConnections() {
        connections.clear()
    }

    fun dispose() {
        running.value = false
        clientScope.cancel()
        connections.clear()
    }

    companion object {
        private const val DEFAULT_PORT = 8080
    }
}
