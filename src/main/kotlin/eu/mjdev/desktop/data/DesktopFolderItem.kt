package eu.mjdev.desktop.data

import androidx.compose.ui.unit.DpOffset
import java.io.File

data class DesktopFolderItem(
    val path: File,
    val customName: String? = null,
    var priority: Int = Int.MIN_VALUE,
    var position: DpOffset = DpOffset.Unspecified
)