package org.mjdev.desktop.components.aidesktop

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AiDesktopWindowBounds(
    val id: Long,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) {
    fun overlapsBottomStrip(
        screenWidth: Dp,
        screenHeight: Dp,
        stripHeight: Dp,
    ): Boolean {
        val stripTop = (screenHeight - stripHeight).value
        val windowRight = x + width
        val windowBottom = y + height
        return width > 0 &&
            height > 0 &&
            windowRight > 0 &&
            x < screenWidth.value &&
            windowBottom > stripTop &&
            y < screenHeight.value
    }
}
