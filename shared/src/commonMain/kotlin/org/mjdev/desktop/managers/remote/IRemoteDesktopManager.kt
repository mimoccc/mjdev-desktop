package org.mjdev.desktop.managers.remote

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import org.mjdev.desktop.managers.base.IDelegate

/**
 * Spravuje Compose Remote **server** (sdílení lokálního UI) a **klientská připojení**
 * pro zobrazení vzdálených Compose UI v tabech včetně audio streamingu.
 */
interface IRemoteDesktopManager : IDelegate {
    // ──────────────────────────────────────────────────────────────
    // SERVER (sdílení lokálního desktopu přes Compose Remote)
    // ──────────────────────────────────────────────────────────────
    val runningState: State<Boolean>
    val isRunning: Boolean get() = runningState.value
    val port: Int
    val addresses: List<String>

    suspend fun start()

    suspend fun stop()

    suspend fun toggle() {
        if (isRunning) stop() else start()
    }

    // ──────────────────────────────────────────────────────────────
    // CLIENT (zobrazení vzdálených Compose UI v tabech)
    // ──────────────────────────────────────────────────────────────

    /** Seznam všech spravovaných klientských připojení. */
    val remoteConnections: State<List<RemoteConnection>>

    /**
     * Vytvoří nové připojení k vzdálenému Compose Remote serveru.
     * @param id Unikátní ID pro tab (např. tab ID).
     * @param host Hostitel serveru (IP nebo hostname).
     * @param port Port serveru (default 8080 pro Compose Remote).
     * @param path WebSocket cesta (default "/compose").
     * @param enableAudio Zapnout audio streaming (default true).
     * @return [RemoteConnection] držící opaque handle klienta pro rendering v tabu.
     */
    suspend fun createConnection(
        id: String,
        host: String,
        port: Int = 8080,
        path: String = "/compose",
        enableAudio: Boolean = true,
    ): RemoteConnection

    /** Uzavře a odebere připojení. */
    suspend fun closeConnection(connection: RemoteConnection)

    /** Uzavře všechna klientská připojení. */
    suspend fun closeAllConnections()

    companion object {
        val EMPTY =
            object : IRemoteDesktopManager {
                override val runningState = mutableStateOf(false)
                override val port = 8080
                override val addresses = emptyList<String>()
                private val _connections = mutableStateListOf<RemoteConnection>()
                override val remoteConnections: State<List<RemoteConnection>> = derivedStateOf { _connections.toList() }

                override suspend fun start() = Unit

                override suspend fun stop() = Unit

                override suspend fun createConnection(
                    id: String,
                    host: String,
                    port: Int,
                    path: String,
                    enableAudio: Boolean,
                ): RemoteConnection = RemoteConnection.Disconnected(id)

                override suspend fun closeConnection(connection: RemoteConnection) {
                    _connections.remove(connection)
                }

                override suspend fun closeAllConnections() {
                    _connections.clear()
                }
            }
    }
}

/**
 * Opaque handle pro klientskou instanci Compose Remote.
 * Skrývá platformě-závislou implementaci (RemoteComposeClient + Audio) za commonMain API.
 * TODO: Implementovat jako expect/actual pro různé platformy
 */
class RemoteClientHandle(
    enableAudio: Boolean,
    host: String,
    port: Int,
    clientScope: CoroutineScope,
) {
    /** Stav připojení klienta. */
    val connectionState: State<RemoteClientConnectionState> =
        mutableStateOf(RemoteClientConnectionState.Disconnected)

    /** Poslední chyba. */
    val lastError: State<String?> = mutableStateOf(null)

    /** Iniciuje připojení k URI (ws://...). */
    suspend fun connect(uri: String) {
        // TODO: Implementace platformě-závislá
    }

    /** Odpojí klienta. */
    fun disconnect() {
        // TODO: Implementace platformě-závislá
    }

    /** Vykreslí vzdálené UI do Compose hierarchie. */
    @Composable
    fun remoteComposeView(
        modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
        placeholder: @Composable () -> Unit = { },
        errorContent: @Composable (Throwable) -> Unit = { },
    ) {
        placeholder()
    }
    // ──────────────────────────────────────────────────────────────
    // AUDIO API
    // ──────────────────────────────────────────────────────────────

    /** Zda je audio dostupné pro toto připojení. */
    val isAudioAvailable: Boolean = enableAudio

    /** Aktuální hlasitost (0.0 - 1.0). */
    val volume: State<Float> = mutableStateOf(1.0f)

    /** Nastaví hlasitost (0.0 - 1.0). */
    fun setVolume(volume: Float) {
        // TODO: Implementace platformě-závislá
    }

    /** Zda je audio ztlumené. */
    val isMuted: State<Boolean> = mutableStateOf(false)

    /** Přepne mute stav. */
    fun setMuted(muted: Boolean) {
        // TODO: Implementace platformě-závislá
    }

    /** Audio stream stav. */
    val audioState: State<AudioStreamState> = mutableStateOf(AudioStreamState.Disconnected)
}

/** Stavy audio streamu. */
enum class AudioStreamState {
    Disconnected,
    Connecting,
    Streaming,
    Error,
    Closed,
}

/** Stavy připojení klienta (platformě-nezávislé). */
enum class RemoteClientConnectionState {
    Disconnected,
    Connecting,
    Connected,
    Error,
    Closed,
}

/**
 * Reprezentuje jedno klientské připojení k Compose Remote serveru.
 * Obsahuje [RemoteClientHandle] pro přímé použití v UI.
 */
sealed interface RemoteConnection {
    val id: String
    val host: String
    val port: Int
    val path: String

    /** Zda je audio povoleno pro toto připojení. */
    val enableAudio: Boolean

    /** Handle klienta pro rendering a ovládání. */
    val clientHandle: RemoteClientHandle?
    val state: State<ConnectionState>
    val lastError: State<String?>

    data class Connected(
        override val id: String,
        override val host: String,
        override val port: Int,
        override val path: String,
        override val enableAudio: Boolean,
        override val clientHandle: RemoteClientHandle,
        override val state: State<ConnectionState> = mutableStateOf(ConnectionState.Connecting),
        override val lastError: State<String?> = mutableStateOf(null),
    ) : RemoteConnection

    data class Disconnected(
        override val id: String,
        override val host: String = "",
        override val port: Int = 8080,
        override val path: String = "/compose",
        override val enableAudio: Boolean = true,
        override val clientHandle: RemoteClientHandle? = null,
        override val state: State<ConnectionState> = mutableStateOf(ConnectionState.Disconnected),
        override val lastError: State<String?> = mutableStateOf(null),
    ) : RemoteConnection

    companion object {
        fun disconnected(id: String) = Disconnected(id)
    }
}

enum class ConnectionState {
    Disconnected,
    Connecting,
    Connected,
    Error,
    Closed,
}
