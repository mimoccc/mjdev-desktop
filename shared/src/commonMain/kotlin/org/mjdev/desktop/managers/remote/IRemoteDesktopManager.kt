package org.mjdev.desktop.managers.remote

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
    suspend fun toggle() { if (isRunning) stop() else start() }

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
        enableAudio: Boolean = true
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
                override val remoteConnections = mutableStateListOf<RemoteConnection>()

                override suspend fun start() = Unit
                override suspend fun stop() = Unit
                override suspend fun createConnection(
                    id: String, host: String, port: Int, path: String, enableAudio: Boolean
                ): RemoteConnection = RemoteConnection.Disconnected(id)
                override suspend fun closeConnection(connection: RemoteConnection) = Unit
                override suspend fun closeAllConnections() = Unit
            }
    }
}

/**
 * Opaque handle pro klientskou instanci Compose Remote.
 * Skrývá platformě-závislou implementaci (RemoteComposeClient + Audio) za commonMain API.
 */
expect class RemoteClientHandle internal constructor() {
    /** Stav připojení klienta. */
    val connectionState: State<RemoteClientConnectionState>
    /** Poslední chyba. */
    val lastError: State<String?>
    /** Iniciuje připojení k URI (ws://...). */
    suspend fun connect(uri: String)
    /** Odpojí klienta. */
    fun disconnect()
    /** Vykreslí vzdálené UI do Compose hierarchie. */
    @Composable
    fun RemoteComposeView(
        modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
        placeholder: @Composable () -> Unit = { },
        errorContent: @Composable (Throwable) -> Unit = { }
    )
    // ──────────────────────────────────────────────────────────────
    // AUDIO API
    // ──────────────────────────────────────────────────────────────
    /** Zda je audio dostupné pro toto připojení. */
    val isAudioAvailable: Boolean
    /** Aktuální hlasitost (0.0 - 1.0). */
    val volume: State<Float>
    /** Nastaví hlasitost (0.0 - 1.0). */
    fun setVolume(volume: Float)
    /** Zda je audio ztlumené. */
    val isMuted: State<Boolean>
    /** Přepne mute stav. */
    fun setMuted(muted: Boolean)
    /** Audio stream stav. */
    val audioState: State<AudioStreamState>
}

/** Stavy audio streamu. */
enum class AudioStreamState {
    Disconnected,
    Connecting,
    Streaming,
    Error,
    Closed
}

/** Stavy připojení klienta (platformě-nezávislé). */
enum class RemoteClientConnectionState {
    Disconnected,
    Connecting,
    Connected,
    Error,
    Closed
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
        override val lastError: State<String?> = mutableStateOf(null)
    ) : RemoteConnection

    data class Disconnected(
        override val id: String,
        override val host: String = "",
        override val port: Int = 8080,
        override val path: String = "/compose",
        override val enableAudio: Boolean = true,
        override val clientHandle: RemoteClientHandle? = null,
        override val state: State<ConnectionState> = mutableStateOf(ConnectionState.Disconnected),
        override val lastError: State<String?> = mutableStateOf(null)
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
    Closed
}
