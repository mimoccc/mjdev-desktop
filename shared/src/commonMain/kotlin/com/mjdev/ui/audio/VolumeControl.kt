package com.mjdev.ui.audio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeHigh
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mjdev.audio.VolumeController
import com.mjdev.audio.VolumeControllerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Compose komponenta pro ovládání systémové hlasitosti.
 * Funguje na Linux desktop i Android.
 */
@Composable
fun VolumeControl(
    modifier: Modifier = Modifier,
    showPercentage: Boolean = true,
    step: Float = 0.05f
) {
    val controller = remember { VolumeControllerFactory.getController() }
    
    var volume by remember { mutableStateOf(controller.volume) }
    var isMuted by remember { mutableStateOf(controller.isMuted) }
    
    // Aktualizace stavu z controlleru
    androidx.compose.runtime.LaunchedEffect(controller) {
        controller.onVolumeChanged = { newVolume ->
            volume = newVolume
        }
        controller.onMuteChanged = { newMuted ->
            isMuted = newMuted
        }
    }

    val icon = when {
        isMuted || volume == 0f -> Icons.Default.VolumeMute
        volume < 0.33f -> Icons.Default.VolumeOff
        volume < 0.66f -> Icons.Default.VolumeUp
        else -> Icons.Default.VolumeHigh
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { controller.toggleMute() }) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (isMuted) "Unmute" else "Mute",
                    tint = if (isMuted) Color.Gray else Color.Unspecified
                )
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.padding(16.dp))
            
            Slider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                value = volume,
                onValueChange = { newVolume ->
                    volume = newVolume
                    controller.setVolume(newVolume)
                },
                enabled = !isMuted,
                colors = SliderDefaults.colors(
                    thumbColor = if (isMuted) Color.Gray else Color.Unspecified,
                    activeTrackColor = if (isMuted) Color.Gray else Color.Unspecified,
                    inactiveTrackColor = if (isMuted) Color.Gray.copy(alpha = 0.5f) else Color.Unspecified
                )
            )
            
            if (showPercentage) {
                Text(
                    text = "${(volume * 100).roundToInt()}%",
                    fontSize = 14.sp,
                    color = if (isMuted) Color.Gray else Color.Unspecified
                )
            }
        }
    }
}

/**
 * Kompaktní verze pro toolbar/status bar
 */
@Composable
fun CompactVolumeControl(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val controller = remember { VolumeControllerFactory.getController() }
    
    var volume by remember { mutableStateOf(controller.volume) }
    var isMuted by remember { mutableStateOf(controller.isMuted) }
    
    androidx.compose.runtime.LaunchedEffect(controller) {
        controller.onVolumeChanged = { volume = it }
        controller.onMuteChanged = { isMuted = it }
    }

    val icon = when {
        isMuted || volume == 0f -> Icons.Default.VolumeMute
        volume < 0.33f -> Icons.Default.VolumeOff
        volume < 0.66f -> Icons.Default.VolumeUp
        else -> Icons.Default.VolumeHigh
    }

    IconButton(
        onClick = onClick ?? { controller.toggleMute() },
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (isMuted) "Unmute" else "Mute",
            tint = if (isMuted) Color.Gray else Color.Unspecified
        )
    }
}
