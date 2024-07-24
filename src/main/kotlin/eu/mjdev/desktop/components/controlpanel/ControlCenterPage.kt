package eu.mjdev.desktop.components.controlpanel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

class ControlCenterPage(
    var icon: ImageVector = Icons.Filled.Settings,
    val name: String = "",
    val content: @Composable (backgroundColor: Color) -> Unit = {}
)