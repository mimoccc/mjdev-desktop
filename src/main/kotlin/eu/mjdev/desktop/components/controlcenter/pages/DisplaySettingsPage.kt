package eu.mjdev.desktop.components.controlcenter.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ScreenshotMonitor
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.components.controlcenter.ControlCenterPage

@Suppress("FunctionName")
fun DisplaySettingsPage() = ControlCenterPage(
    icon = Icons.Filled.ScreenshotMonitor,
    name = "Display"
) { backgroundColor ->
    Box(
        modifier = Modifier.fillMaxSize().background(backgroundColor)
    )
}
