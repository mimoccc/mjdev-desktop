package org.mjdev.desktop.components.desktop.widgets.music

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.plugins.music.MusicDefaults

/** One round transport button of the music widget. */
@Suppress("FunctionName")
@Composable
fun MusicControlButton(
    imageVector: ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit,
) = IconButton(
    modifier = Modifier.size(MusicDefaults.BUTTON_DP.dp),
    onClick = onClick,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = description,
        tint = tint,
    )
}
