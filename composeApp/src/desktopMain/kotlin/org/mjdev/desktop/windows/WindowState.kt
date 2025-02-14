package org.mjdev.desktop.windows

import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize

interface WindowState {
    var isMinimized: Boolean
    var position: DpOffset
    var size: DpSize
}