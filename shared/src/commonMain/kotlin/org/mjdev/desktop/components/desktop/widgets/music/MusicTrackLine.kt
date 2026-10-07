package org.mjdev.desktop.components.desktop.widgets.music

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/** One single-line track title of the music card's inner card (cut with an ellipsis). */
@Suppress("FunctionName")
@Composable
fun MusicTrackLine(
    text: String,
    color: Color,
    size: TextUnit,
    weight: FontWeight = FontWeight.Normal,
) = Text(
    text = text,
    color = color,
    fontSize = size,
    fontWeight = weight,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
)
