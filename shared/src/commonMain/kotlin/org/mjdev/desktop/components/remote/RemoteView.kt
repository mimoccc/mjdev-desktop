package org.mjdev.desktop.components.remote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.desktop.managers.remote.AudioStreamState
import org.mjdev.desktop.managers.remote.ConnectionState
import org.mjdev.desktop.managers.remote.RemoteConnection
import org.mjdev.desktop.managers.remote.RemoteClientHandle
import org.mjdev.desktop.managers.remote.RemoteClientConnectionState
import kotlin.math.roundToInt

/**
 * Komponenta pro zobrazení vzdáleného Compose UI v tabu s audio ovládáním.
 * Používá opaque [RemoteClientHandle] pro rendering a audio control.
 *
 * Použití:
 * ```kotlin
 * val connection = remoteManager.createConnection("tab-1", "192.168.1.100")
 * RemoteComposeView(connection = connection, modifier = Modifier.fillMaxSize())
 * ```
 */
@Composable
fun RemoteComposeView(
    connection: RemoteConnection,
    modifier: Modifier = Modifier,
    showConnectionInfo: Boolean = true,
    showAudioControls: Boolean = true
) {
    val state by connection.state
    val error by connection.lastError
    val handle = connection.clientHandle

    Box(modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main content area
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                when (state) {
                    ConnectionState.Disconnected -> {
                        CenteredText("Odpojeno\nKlikněte pro připojení", Color.Gray)
                    }
                    ConnectionState.Connecting -> {
                        CenteredText("Připojuji k ${connection.host}:${connection.port}${connection.path}...", Color.Yellow)
                    }
                    ConnectionState.Connected -> {
                        handle?.let { h ->
                            h.RemoteComposeView(
                                modifier = Modifier.fillMaxSize(),
                                placeholder = { CenteredText("Načítání UI...", Color.White) },
                                errorContent = { throwable ->
                                    CenteredText("Chyba renderování: ${throwable.message}", Color.Red)
                                }
                            )
                        } ?: CenteredText("Klient nenainicializován", Color.Red)
                    }
                    ConnectionState.Error -> {
                        CenteredText("Chyba připojení\n${error ?: "Neznámá chyba"}", Color.Red)
                    }
                    ConnectionState.Closed -> {
                        CenteredText("Připojení uzavřeno", Color.Gray)
                    }
                }

                if (showConnectionInfo && state != ConnectionState.Disconnected) {
                    ConnectionInfoOverlay(connection)
                }
            }

            // Audio controls bar
            if (showAudioControls && handle != null && handle.isAudioAvailable && state == ConnectionState.Connected) {
                AudioControlBar(handle)
            }
        }
    }
}

@Composable
private fun AudioControlBar(handle: RemoteClientHandle) {
    val volume by handle.volume
    val isMuted by handle.isMuted
    val audioState by handle.audioState
    val connectionState by handle.connectionState

    val isAudioConnected = audioState == AudioStreamState.Streaming
    val effectiveVolume = if (isMuted) 0f else volume

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Audio status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(when (audioState) {
                        AudioStreamState.Streaming -> Color.Green
                        AudioStreamState.Connecting -> Color.Yellow
                        AudioStreamState.Error -> Color.Red
                        else -> Color.Gray
                    })
            )
            
            Text(
                text = when (audioState) {
                    AudioStreamState.Streaming -> "Audio: Přehrává"
                    AudioStreamState.Connecting -> "Audio: Připojuji..."
                    AudioStreamState.Error -> "Audio: Chyba"
                    AudioStreamState.Disconnected -> "Audio: Odpojeno"
                    AudioStreamState.Closed -> "Audio: Uzavřeno"
                },
                color = Color.White,
                fontSize = 12.sp
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

            // Mute button
            IconButton(onClick = { handle.setMuted(!isMuted) }) {
                Icon(
                    imageVector = if (isMuted || volume == 0f) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Zapnout zvuk" else "Ztlumit",
                    tint = Color.White
                )
            }

            // Volume slider
            Slider(
                modifier = Modifier.width(120.dp),
                value = effectiveVolume,
                onValueChange = { handle.setVolume(it) },
                enabled = isAudioConnected && !isMuted,
                colors = androidx.compose.material.SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Cyan,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                    disabledThumbColor = Color.White.copy(alpha = 0.5f),
                    disabledActiveTrackColor = Color.Cyan.copy(alpha = 0.5f)
                )
            )

            Text(
                text = "${(effectiveVolume * 100).roundToInt()}%",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun CenteredText(text: String, color: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        )
    }
}

@Composable
private fun ConnectionInfoOverlay(connection: RemoteConnection) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(8.dp)
        ) {
            Text(
                text = "${connection.id} @ ${connection.host}:${connection.port}${connection.path} ${if (connection.enableAudio) "🔊" else "🔇"}",
                color = Color.White,
                fontSize = 12.sp,
                style = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Preview
// ──────────────────────────────────────────────────────────────
@Preview
@Composable
private fun PreviewRemoteComposeView() {
    val fakeConn = RemoteConnection.Connected(
        id = "preview-tab",
        host = "192.168.1.50",
        port = 8080,
        path = "/compose",
        enableAudio = true,
        clientHandle = null,
        state = androidx.compose.runtime.mutableStateOf(ConnectionState.Connected)
    )
    RemoteComposeView(connection = fakeConn, modifier = Modifier.size(800.dp, 600.dp))
}

@Preview
@Composable
private fun PreviewRemoteComposeViewConnecting() {
    val fakeConn = RemoteConnection.Connected(
        id = "preview-tab-2",
        host = "10.0.0.1",
        port = 8080,
        path = "/compose",
        enableAudio = true,
        clientHandle = null,
        state = mutableStateOf(ConnectionState.Connecting)
    )
    RemoteComposeView(connection = fakeConn, modifier = Modifier.size(400.dp, 300.dp))
}
