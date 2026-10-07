package org.mjdev.desktop.components.desktop.widgets.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.plugins.music.MusicDefaults

/**
 * Round filled play / pause button in the middle of the music card transport row. A plain
 * clickable circle (not IconButton, which forces a 48 dp minimum) so it keeps the design size.
 */
@Suppress("FunctionName")
@Composable
fun MusicPlayButton(
    isPlaying: Boolean,
    accentColor: Color,
    onAccentColor: Color,
    onClick: () -> Unit,
) = Box(
    modifier =
        Modifier
            .size(MusicDefaults.PLAY_BUTTON_DP.dp)
            .clip(CircleShape)
            .background(accentColor)
            .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
) {
    Icon(
        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
        contentDescription = if (isPlaying) MusicDefaults.PAUSE_LABEL else MusicDefaults.PLAY_LABEL,
        tint = onAccentColor,
    )
}
