package org.mjdev.desktop.managers.remote

import androidx.compose.remote.server.RemoteComposeServer
import androidx.compose.remote.client.RemoteComposeClient
import androidx.compose.remote.client.RemoteComposeView as LibraryRemoteComposeView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.log.Log
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URI
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

/**
 * Implementace pro Compose Remote (WebSocket + Compose UI streaming + Audio).
 * Server: [RemoteComposeServer] na portech 8080 (WS) + 8081 (HTTP pro resources).
 * Klient: [RemoteComposeClient] připojující se na ws://host:port/compose.
 * Audio: samostatný WebSocket endpoint na ws://host:port/audio pro PCM stream.
 */
class RemoteDesktopManager(
    private val context: IDesktopContext,
    override val port: Int = DEFAULT_PORT,
) : IRemoteDesktopManager {

    // ──────────────────────────────────────────────────────────────
    // SERVER (sdílení lokálního Compose UI)
    // ──────────────────────────────────────────────────────────────
    private val running = mutableStateOf(false)
    override val runningState: State<Boolean> get() = running

    private var server: RemoteComposeServer? = null

    override val addresses: List<String>
        get() = localAddresses().map { "$it:$port" }

    override suspend fun start() = withContext(Dispatchers.IO) {
        if (running.value) return@withContext
        runCatching {
            server = RemoteComposeServer(port = port, assetPort = port + 1)
            server?.start()
            running.value = true
            Log.i("RemoteDesktopManager: Compose Remote server listening on ${addresses.joinToString()} (WS) / ${localAddresses().map { "$it:${port + 1}" }.joinToString()} (HTTP assets)")
        }.onFailure { e ->
            running.value = false
            Log.e("RemoteDesktopManager: cannot start server on port $port: ${e.message}")
        }
        Unit
    }

    override suspend fun stop() = withContext(Dispatchers.IO) {
        if (!running.value) return@withContext
        running.value = false
        server?.stop()
        server = null
        Log.i("RemoteDesktopManager: Compose Remote server stopped")
    }

    // ──────────────────────────────────────────────────────────────
    // CLIENT (zobrazení vzdálených Compose UI v tabech)
    // ──────────────────────────────────────────────────────────────
    private val clientScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connections = mutableStateListOf<RemoteConnectionImpl>()
    override val remoteConnections: State<List<RemoteConnection>> get() = connections

    override suspend fun createConnection(
        id: String,
        host: String,
        port: Int = 8080,
        path: String = "/compose",
        enableAudio: Boolean = true
    ): RemoteConnection {
        return withContext(Dispatchers.IO) {
            val client = RemoteComposeClient()
            val handle = RemoteClientHandleImpl(client, enableAudio, host, port, clientScope)
            val conn = RemoteConnectionImpl(
                id = id,
                host = host,
                port = port,
                path = path,
                enableAudio = enableAudio,
                clientHandle = handle,
                onClose = { connections.remove(it) }
            )
            connections.add(conn)
            clientScope.launch { conn.connect() }
            conn
        }
    }

    override suspend fun closeConnection(connection: RemoteConnection) = withContext(Dispatchers.IO) {
        (connection as? RemoteConnectionImpl)?.close()
    }

    override suspend fun closeAllConnections() = withContext(Dispatchers.IO) {
        connections.forEach { it.close() }
        connections.clear()
    }

    fun dispose() {
        running.value = false
        clientScope.cancel()
        server?.stop()
        server = null
        connections.forEach { it.close() }
        connections.clear()
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
        private const val DEFAULT_PORT = 8080
    }
}

/** Desktop implementace opaque handle s audio podporou. */
actual class RemoteClientHandle internal constructor(
    private val client: RemoteComposeClient,
    private val enableAudio: Boolean,
    private val host: String,
    private val port: Int,
    private val clientScope: CoroutineScope
) {
    actual val connectionState: State<RemoteClientConnectionState> = client.state.map { mapClientState(it) }
    actual val lastError: State<String?> = client.lastError

    // ──────────────────────────────────────────────────────────────
    // AUDIO IMPLEMENTATION
    // ──────────────────────────────────────────────────────────────
    private val _volume = mutableStateOf(1.0f)
    private val _isMuted = mutableStateOf(false)
    private val _audioState = mutableStateOf(AudioStreamState.Disconnected)
    private var audioPlayer: AudioPlayer? = null
    private var audioJob: Job? = null

    actual val isAudioAvailable: Boolean = enableAudio
    actual val volume: State<Float> = _volume
    actual val isMuted: State<Boolean> = _isMuted
    actual val audioState: State<AudioStreamState> = _audioState

    actual fun setVolume(volume: Float) {
        _volume.value = volume.coerceIn(0.0f, 1.0f)
        audioPlayer?.setVolume(_volume.value * if (_isMuted.value) 0.0f else 1.0f)
    }

    actual fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        audioPlayer?.setVolume(_volume.value * if (muted) 0.0f else 1.0f)
    }

    actual suspend fun connect(uri: String) {
        client.connect(uri)
        if (enableAudio) {
            startAudioStream()
        }
    }

    private fun startAudioStream() {
        _audioState.value = AudioStreamState.Connecting
        audioJob = clientScope.launch {
            try {
                val audioUri = "ws://$host:$port/audio"
                Log.i("RemoteDesktopManager: Connecting to audio stream: $audioUri")
                
                // WebSocket audio client (simplified - uses Java WebSocket API)
                val webSocket = connectAudioWebSocket(audioUri)
                if (webSocket != null) {
                    val format = AudioFormat(44100.0f, 16, 2, true, false)
                    val info = DataLine.Info(SourceDataLine::class.java, format)
                    val line = AudioSystem.getLine(info) as SourceDataLine
                    line.open(format)
                    line.start()
                    
                    audioPlayer = AudioPlayer(line)
                    _audioState.value = AudioStreamState.Streaming
                    
                    // Receive loop
                    while (_audioState.value == AudioStreamState.Streaming) {
                        val data = webSocket.receiveBinary()
                        if (data != null) {
                            audioPlayer?.write(data)
                        } else {
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RemoteDesktopManager: Audio stream error: ${e.message}")
                _audioState.value = AudioStreamState.Error
                lastError.value = "Audio: ${e.message}"
            } finally {
                stopAudioStream()
            }
        }
    }

    private fun stopAudioStream() {
        _audioState.value = AudioStreamState.Closed
        audioJob?.cancel()
        audioPlayer?.close()
        audioPlayer = null
    }

    private fun connectAudioWebSocket(uri: String): AudioWebSocket? {
        // Simplified WebSocket client for audio
        // In production, use a proper WebSocket library like OkHttp or Java 11+ HttpClient
        try {
            val httpClient = java.net.http.HttpClient.newBuilder().build()
            val webSocket = httpClient.newWebSocketBuilder()
                .buildAsync(URI(uri), object : java.net.http.WebSocket.Listener() {
                    private val buffer = java.util.ArrayDeque<ByteArray>()
                    
                    override fun onBinary(webSocket: java.net.http.WebSocket, data: java.nio.ByteBuffer, last: Boolean) {
                        val bytes = ByteArray(data.remaining())
                        data.get(bytes)
                        buffer.add(bytes)
                        webSocket.request(1)
                    }
                    
                    override fun onError(webSocket: java.net.http.WebSocket, error: Throwable) {
                        Log.e("Audio WebSocket error: ${error.message}")
                    }
                }).join()
            
            return object : AudioWebSocket {
                override fun receiveBinary(): ByteArray? = buffer.poll()
                override fun close() { webSocket.sendClose(java.net.http.WebSocket.NORMAL_CLOSURE, "").join() }
            }
        } catch (e: Exception) {
            Log.e("Failed to connect audio WebSocket: ${e.message}")
            return null
        }
    }

    actual fun disconnect() {
        client.disconnect()
        stopAudioStream()
    }

    @Composable
    actual fun RemoteComposeView(
        modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
        placeholder: @Composable () -> Unit = { },
        errorContent: @Composable (Throwable) -> Unit = { }
    ) {
        LibraryRemoteComposeView(
            client = client,
            modifier = modifier,
            placeholder = placeholder,
            errorContent = errorContent
        )
    }

    private fun mapClientState(state: RemoteComposeClient.ConnectionState): RemoteClientConnectionState = when (state) {
        RemoteComposeClient.ConnectionState.Disconnected -> RemoteClientConnectionState.Disconnected
        RemoteComposeClient.ConnectionState.Connecting -> RemoteClientConnectionState.Connecting
        RemoteComposeClient.ConnectionState.Connected -> RemoteClientConnectionState.Connected
        RemoteComposeClient.ConnectionState.Error -> RemoteClientConnectionState.Error
        RemoteComposeClient.ConnectionState.Closed -> RemoteClientConnectionState.Closed
    }
}

private interface AudioWebSocket {
    fun receiveBinary(): ByteArray?
    fun close()
}

private class AudioPlayer(private val line: SourceDataLine) {
    private var currentVolume = 1.0f
    
    fun write(data: ByteArray) {
        if (currentVolume != 1.0f) {
            // Apply volume to PCM data (16-bit stereo)
            val adjusted = ByteArray(data.size)
            for (i in 0 until data.size step 2) {
                val sample = (data[i + 1].toInt() shl 8) or (data[i].toInt() and 0xFF)
                val adjustedSample = (sample * currentVolume).toInt().coerceIn(-32768, 32767)
                adjusted[i] = (adjustedSample and 0xFF).toByte()
                adjusted[i + 1] = (adjustedSample ushr 8).toByte()
            }
            line.write(adjusted, 0, adjusted.size)
        } else {
            line.write(data, 0, data.size)
        }
    }
    
    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0.0f, 1.0f)
    }
    
    fun close() {
        line.drain()
        line.stop()
        line.close()
    }
}

/** Interní implementace klientského připojení. */
private class RemoteConnectionImpl(
    override val id: String,
    override val host: String,
    override val port: Int,
    override val path: String,
    override val enableAudio: Boolean,
    override val clientHandle: RemoteClientHandle,
    private val onClose: (RemoteConnectionImpl) -> Unit
) : RemoteConnection.Connected(
    id = id,
    host = host,
    port = port,
    path = path,
    enableAudio = enableAudio,
    clientHandle = clientHandle,
    state = mutableStateOf(ConnectionState.Connecting),
    lastError = mutableStateOf(null)
) {

    private var connectJob: Job? = null

    init {
        state.value = ConnectionState.Connecting
    }

    suspend fun connect() {
        val uri = "ws://$host:$port$path"
        Log.i("RemoteDesktopManager: Connecting to Compose Remote server: $uri")
        connectJob = clientScope.launch {
            try {
                clientHandle.connect(uri)
                while (clientHandle.connectionState.value == RemoteClientConnectionState.Connecting) {
                    kotlinx.coroutines.delay(100)
                }
                when (clientHandle.connectionState.value) {
                    RemoteClientConnectionState.Connected -> {
                        state.value = ConnectionState.Connected
                        lastError.value = null
                        Log.i("RemoteDesktopManager: Connected to $uri")
                    }
                    RemoteClientConnectionState.Error -> {
                        state.value = ConnectionState.Error
                        lastError.value = clientHandle.lastError.value ?: "Unknown connection error"
                        Log.e("RemoteDesktopManager: Connection error: ${lastError.value}")
                    }
                    RemoteClientConnectionState.Closed -> {
                        state.value = ConnectionState.Closed
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                state.value = ConnectionState.Error
                lastError.value = e.message
                Log.e("RemoteDesktopManager: Connection exception: ${e.message}")
            }
        }
    }

    override fun close() {
        state.value = ConnectionState.Closed
        connectJob?.cancel()
        clientHandle.disconnect()
        onClose(this)
    }
    
    override fun setViewOnly(viewOnly: Boolean) {
        // TODO: Implement view-only mode if needed by the remote compose library
        // This would typically involve setting a flag on the client or server
    }
}
