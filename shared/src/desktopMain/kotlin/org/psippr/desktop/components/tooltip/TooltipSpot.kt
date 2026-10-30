package org.psippr.desktop.components.tooltip

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpOffset

/**
 * Where the tooltip window goes and where the bubble sits inside it. The window is larger than
 * most bubbles, so on a side flipped towards the pointer the bubble must hug that side, or it is
 * drawn a window width away from what it describes.
 *
 * @property position top left corner of the window
 * @property alignment bubble alignment inside the window, the corner nearest the pointer
 */
data class TooltipSpot(
    val position: DpOffset,
    val alignment: Alignment,
)
