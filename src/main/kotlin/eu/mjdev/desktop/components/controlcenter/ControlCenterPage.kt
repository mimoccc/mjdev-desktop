package eu.mjdev.desktop.components.controlcenter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import eu.mjdev.desktop.provider.DesktopProvider

class ControlCenterPage(
    val icon: ImageVector = Icons.Filled.Settings,
    val name: String = "",
    val content: @Composable ControlCenterPageScope.() -> Unit = {}
) {
    class ControlCenterPageScope(
        val backgroundColor: Color,
        val api: DesktopProvider
    )
}