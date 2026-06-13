package org.mjdev.desktop.data

import androidx.compose.ui.unit.DpOffset
import okio.Path

data class DesktopFolderItem(
    val path: Path,
    val customName: String? = null,
    var priority: Int = Int.MIN_VALUE,
    var position: DpOffset = DpOffset.Zero,
)
