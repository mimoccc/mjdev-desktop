package org.mjdev.desktop.components.blur

import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize

/** Where the window hosting a composable sits on the screen, in dp. */
data class WindowBounds(
    /** Top left corner of the window in screen coordinates. */
    val position: DpOffset = DpOffset.Zero,
    /** Size of the window. */
    val size: DpSize = DpSize.Zero,
)
